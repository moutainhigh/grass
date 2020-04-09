package com.yanglinkui.grass.protocol.message;

import com.yanglinkui.grass.*;
import com.yanglinkui.grass.common.logger.Logger;
import com.yanglinkui.grass.common.logger.LoggerFactory;
import com.yanglinkui.grass.protocol.Server;
import com.yanglinkui.grass.protocol.ServerInfo;
import com.yanglinkui.grass.protocol.message.api.*;
import com.yanglinkui.grass.provider.Service;
import com.yanglinkui.grass.provider.ServiceRepository;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class MessageServer implements Server {
    private static Logger logger = LoggerFactory.getLogger(MessageServer.class);

    private static final String PUBLIC_SERVICE_TOPIC_NAME = "public.services";
    private static final String MANAGEMENT_SERVICE_TOPIC_NAME = "management.services";

    private static final int READY = 0;
    private static final int RUNNING = 1;
    private static final int SHUTDOWN = 3;

    private final AtomicReference<Consumer> publicServiceConsumerHolder;

    private final AtomicReference<Consumer> managementServiceConsumerHolder;
    private final String type;
    private final String addresses;
    private final String prefix;
    private final Map<String, String> config;
    private final ExecutorService executorService;
    private AtomicInteger status = new AtomicInteger(READY);

    private final String publicServiceTopicName;
    private final String managementServiceTopicName;
    private volatile ServiceRepository publicServiceRepository;
    private volatile ServiceRepository managementServiceRepository;
    private final AtomicInteger concurrentRequestCounter = new AtomicInteger(0);

    private AtomicReference<ServerInfo> serverInfoHolder = new AtomicReference<>();

    public MessageServer(String type, String prefix, String addresses, Map<String, String> config, ExecutorService executorService) {
        this.type = type;
        this.addresses = addresses;
        this.config = config;
        this.prefix = prefix;
        this.executorService = executorService;
        this.publicServiceTopicName = MessageUtils.getTopicName(this.prefix, PUBLIC_SERVICE_TOPIC_NAME);
        this.managementServiceTopicName = MessageUtils.getTopicName(this.prefix, MANAGEMENT_SERVICE_TOPIC_NAME);
        this.publicServiceConsumerHolder = new AtomicReference<>();
        this.managementServiceConsumerHolder = new AtomicReference<>();
    }


    @Override
    public synchronized void exportPublic(ServiceRepository serviceRepository) {
        if (serviceRepository == null) {
            throw new IllegalArgumentException("serviceRepository cannot be null");
        }

        this.publicServiceRepository = serviceRepository;
    }

    @Override
    public void exportManagement(ServiceRepository serviceRepository) {
        if (serviceRepository == null) {
            throw new IllegalArgumentException("serviceRepository cannot be null");
        }

        this.managementServiceRepository = serviceRepository;
    }

    @Override
    public ServerInfo getInfo() {
        return this.serverInfoHolder.get();
    }

    @Override
    public void start() {
        if (this.status.get() == RUNNING) {
            return;
        }

        if (this.publicServiceRepository == null && this.managementServiceRepository == null) {
            logger.warn("No service be exported");
            return;
        }

        if (this.status.compareAndSet(READY, RUNNING)) {
            try {
                String applicationName = GrassContextManager.getContext().getApplicationName();
                publishPublicServices(this.publicServiceRepository,
                        MessageUtils.getGroup(this.prefix, applicationName),
                        this.publicServiceTopicName);

                publishManagementServices(this.managementServiceRepository,
                        MessageUtils.getGroup(this.prefix, GrassContextManager.getContext().getApplicationInstanceId()),
                        this.managementServiceTopicName);

                DefaultServerProperties publicServiceProperties = new DefaultServerProperties(MessageProtocolFactory.ID, this.addresses, null);
                publicServiceProperties.addMetadata(MessageConstants.APPLICATION_METADATA_TOPIC, this.managementServiceTopicName);

                DefaultServerProperties managementServiceProperties = new DefaultServerProperties(MessageProtocolFactory.ID, this.addresses, UUID.randomUUID().toString());
                managementServiceProperties.addMetadata(MessageConstants.APPLICATION_METADATA_TOPIC, this.managementServiceTopicName);
                DefaultServerInfo serverInfo = new DefaultServerInfo(publicServiceProperties, managementServiceProperties);

                this.serverInfoHolder.set(serverInfo);
            } catch (Exception e) {
                throw new IllegalStateException("Failed to start message server", e);
            }
        }
    }

    public Consumer createContainer(String group) {
        MessagingFactory factory = MessagingFactoryManager.create(this.type);
        Consumer consumer = factory.createConsumer(group, addresses, config, executorService);

        return consumer;
    }

    private void publishPublicServices(ServiceRepository repository, String group, String topic) throws Exception {
        if (repository == null) {
            return;
        }

        List<String> tags = new LinkedList<>();
        for (Service service : repository.getList()) {
            for (Invoker invoker : service.getInvokerList()) {
                tags.add(invoker.getAttributes().getId());
                logger.info("exported {} service", invoker.getAttributes().getId());
            }
        }

        Consumer consumer = createAndStartConsumer(repository, group, topic, tags);
        this.publicServiceConsumerHolder.set(consumer);
    }

    private void publishManagementServices(ServiceRepository repository, String group, String topic) throws Exception {
        if (repository == null) {
            return;
        }

        List<String> tags = new LinkedList<>();
        tags.add(GrassContextManager.getContext().getApplicationInstanceId());
        Consumer consumer = createAndStartConsumer(repository, group, topic, tags);
        this.managementServiceConsumerHolder.set(consumer);

        logger.info("exported management services");
    }

    private Consumer createAndStartConsumer(ServiceRepository repository, String group, String topic, List<String> tagList) throws Exception {
        if (tagList == null) {
            tagList = Collections.EMPTY_LIST;
        }
        Consumer consumer = createContainer(group);
        consumer.subscribe(topic, tagList.toArray(new String[0]));
        consumer.setMessageListener(new DefaultMessageListener(repository, this.concurrentRequestCounter));
        consumer.start();

        return consumer;
    }

    @Override
    public void shutdown() {
        if (this.status.compareAndSet(RUNNING, SHUTDOWN)) {
            try {
                this.publicServiceConsumerHolder.get().shutdown();
            } catch (Exception e) {
                logger.error("Failed to shutdown public service consumer");
            }
            try {
                this.managementServiceConsumerHolder.get().shutdown();
            } catch (Exception e) {
                logger.error("Failed to shutdown management service consumer");
            }
        }
    }

}
