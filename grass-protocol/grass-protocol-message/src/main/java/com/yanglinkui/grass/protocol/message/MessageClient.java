package com.yanglinkui.grass.protocol.message;

import com.yanglinkui.grass.*;
import com.yanglinkui.grass.common.logger.Logger;
import com.yanglinkui.grass.common.logger.LoggerFactory;
import com.yanglinkui.grass.exception.InvokedException;
import com.yanglinkui.grass.protocol.Client;
import com.yanglinkui.grass.protocol.message.api.*;
import com.yanglinkui.grass.serialize.Serializer;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

public class MessageClient implements Client {

    static Logger logger = LoggerFactory.getLogger(MessageClient.class);

    private final Map<String/* group */, Producer> producerMap = new ConcurrentHashMap<>();
    private final ExecutorService executorService;

    public MessageClient(ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public CompletableFuture<GrassResponse> invoke(ApplicationInstance applicationInstance, GrassRequest request, long timeout) {
        String correlationId = request.getId();
        if (correlationId == null) {
            correlationId = UUID.randomUUID().toString();
        }

        try {
            Map<String, String> properties = new HashMap<>();
            Arrays.stream(request.getHeaderNames())
                    .filter(name -> !HeaderConstants.RESERVED_KEYWORDS.contains(name))
                    .forEach(name -> properties.put(name, request.getHeader(name)));

            properties.put(HeaderConstants.REMOTE_USER, request.getRemoteUser());
            properties.put(HeaderConstants.SERVICE_ID, request.getServiceId());
            properties.put(HeaderConstants.ACTION, request.getAction());
            properties.put(HeaderConstants.SERVICE_VERSION, request.getServiceVersion());
            properties.put(HeaderConstants.ZONE, request.getZone());
            properties.put(HeaderConstants.REQUEST_TIME, String.valueOf(request.getRequestTime()));

            properties.put(HeaderConstants.PUBLISHER_APPLICATION, GrassContextManager.getContext().getApplicationName());
            properties.put(HeaderConstants.PUBLISHER_ZONE, GrassContextManager.getContext().getZone());

            Map<String, Object> parameterMap = request.getBody();
            Serializer serializer = GrassContextManager.getContext().getSerializer(request.getContentType());
            byte[] body = serializer.serialize(parameterMap);

            String messageId = UUID.randomUUID().toString();
            Message message = new Message(body)
                    .setId(messageId)
                    .setCorrelationId(correlationId)
                    .setContentType(request.getContentType())
                    .setContentType("utf-8")
                    .setProperties(properties);

            CompletableFuture future = new CompletableFuture();

            //request
            String topic = MessageUtils.getGroup(applicationInstance);
            Producer producer = getProducer(applicationInstance);
            producer.send(topic, request.getServiceId(), message, new DefaultRequestListener(future), timeout);

            return future;
        } catch (Exception e) {
            throw new InvokedException(e);
        }

    }

    Producer getProducer(ApplicationInstance applicationInstance) throws Exception {
        Map<String, String> metadata = applicationInstance.getMetadata();
        String group = MessageUtils.getGroup(applicationInstance);
        Producer producer = this.producerMap.get(group);
        if (producer != null) {
            return producer;
        }

        synchronized (this) {
            if (producer == null) {
                Map<String, String> config = new HashMap<>(metadata.size(), 1F);
                metadata.entrySet().stream()
                        .filter(e -> isMessageConfig(e.getKey()))
                        .forEach(e -> config.put(e.getKey().replace("message\\.", ""), e.getValue()));

                String type = MessageUtils.getContainerType(applicationInstance);
                MessagingFactory factory = MessagingFactoryManager.create(type);
                producer = factory.createProducer(group, applicationInstance.getAddresses(), config);
                producer.start();

                this.producerMap.put(group, producer);
            }
        }

        return producer;
    }

    private boolean isMessageConfig(String key) {
        return !("message.group".equals(key) || "message.type".equals(key)) && key.startsWith("message.");
    }

    public void shutdown() {
        for (Producer producer : this.producerMap.values()) {
            try {
                producer.shutdown();
            } catch (Exception e) {
                logger.error("Failed to shutdown", e);
            }
        }
    }

}
