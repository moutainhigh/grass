package com.yanglinkui.grass;

import com.yanglinkui.grass.client.*;
import com.yanglinkui.grass.common.Utils;
import com.yanglinkui.grass.protocol.DefaultProtocolFactoryManager;
import com.yanglinkui.grass.protocol.ProtocolFactory;
import com.yanglinkui.grass.protocol.Server;
import com.yanglinkui.grass.provider.*;
import com.yanglinkui.grass.registry.DefaultApplicationInstance;
import com.yanglinkui.grass.registry.Registry;
import com.yanglinkui.grass.serialize.DefaultSerializerFactoryManager;
import com.yanglinkui.grass.serialize.SerializerFactory;
import org.springframework.core.OrderComparator;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class Grass {

    private final GrassSetting setting;

    private volatile boolean isStarted = false;

    private final DefaultClientRepository clientRepository = new DefaultClientRepository();

    private final DefaultServiceRepository serviceRepository = new DefaultServiceRepository();

    private final DefaultProtocolFactoryManager protocolFactoryManager = new DefaultProtocolFactoryManager();

    private final DefaultSerializerFactoryManager serializerFactoryManager = new DefaultSerializerFactoryManager();

    private final List<ObjectFactory> objectFactoryList = new LinkedList<>();

    private Registry registry;

    private DefaultGrassContext context;

    private SpiRouter defaultSpiRouter;

    private LoadBalance defaultLoadBalance;

    //临时存储
    private WeakReference<List<ClientBuilder>> clientBuilderList = new WeakReference<>(new LinkedList<>());
    private WeakReference<List<ServiceBuilder>> serviceBuilderList = new WeakReference<>(new LinkedList<>());
    private WeakReference<List<Router>> routerList = new WeakReference<>(new LinkedList<>());
    private WeakReference<List<ClientProcessor>> clientProcessorList = new WeakReference<>(new LinkedList<>());
    private WeakReference<List<ServiceProcessor>> serviceProcessorList = new WeakReference<>(new LinkedList<>());

    public Grass(GrassSetting setting) {
        this.setting = setting;
        this.objectFactoryList.add(this.clientRepository);
    }

    public GrassSetting getSetting() {
        return this.setting;
    }

    public Registry getRegistry() {
        return registry;
    }

    public Grass setRegistry(Registry registry) {
        if (this.isStarted) {
            throw new IllegalStateException("The grass application was started!");
        }

        this.registry = registry;

        return this;
    }

    public GrassContext getContext() {
        if (this.isStarted == false) {
            throw new IllegalStateException("The grass application is not started!");
        }

        return this.context;
    }

    public Grass setDefaultLoadBalance(LoadBalance defaultLoadBalance) {
        this.defaultLoadBalance = defaultLoadBalance;
        return this;
    }

    public Grass setDefaultSpiRouter(SpiRouter defaultSpiRouter) {
        this.defaultSpiRouter = defaultSpiRouter;
        return this;
    }

    public Grass addObject(String id, Object bean) {
        if (this.isStarted) {
            throw new IllegalStateException("The grass application was started!");
        }

        if (bean == null) {
            return this;
        }

        if (bean instanceof ClientProcessor) {
            this.clientProcessorList.get().add((ClientProcessor) bean);
        } else if (bean instanceof ServiceProcessor) {
            this.serviceProcessorList.get().add((ServiceProcessor) bean);
        } else if (bean instanceof Router) {
            this.routerList.get().add((Router) bean);
        } else if (bean instanceof ProtocolFactory) {
            this.protocolFactoryManager.addProtocolFactory((ProtocolFactory) bean);
        } else if (bean instanceof SerializerFactory) {
            this.serializerFactoryManager.addSerializerFactory((SerializerFactory) bean);
        } else {
            id = Optional.ofNullable(id).orElseGet(() -> bean.getClass().getCanonicalName());
            this.context.addInstance(id, bean);
        }

        return this;
    }

    public Grass addObject(Object bean) {
        if (this.isStarted) {
            throw new IllegalStateException("The grass application was started!");
        }

        return addObject(null, bean);
    }

    public Grass addProtocol(ProtocolFactory factory) {
        if (this.isStarted) {
            throw new IllegalStateException("The grass application was started!");
        }

        if (factory == null) {
            throw new IllegalArgumentException("factory cannot be null");
        }
        this.protocolFactoryManager.addProtocolFactory(factory);
        return this;
    }

    public Grass addSerializer(SerializerFactory factory) {
        if (this.isStarted) {
            throw new IllegalStateException("The grass application was started!");
        }

        if (factory == null) {
            throw new IllegalArgumentException("factory cannot be null");
        }

        this.serializerFactoryManager.addSerializerFactory(factory);
        return this;
    }

    public Grass addClient(Class<?> clazz) {
        if (this.isStarted) {
            throw new IllegalStateException("The grass application was started!");
        }

        if (clazz == null || !clazz.isInterface()) {
            throw new IllegalArgumentException("The client class must be an interface");
        }

        ClientBuilder builder = new ClientBuilder();
        builder.setInterface(clazz);

        this.clientBuilderList.get().add(builder);
        return this;
    }

    public Grass addService(Object service) {
        if (this.isStarted) {
            throw new IllegalStateException("The grass application was started!");
        }

        if (service == null) {
            throw new IllegalArgumentException("The service cannot be null");
        }

        ServiceBuilder builder = new ServiceBuilder();
        builder.setHandler(service);
        this.serviceBuilderList.get().add(builder);

        return this;
    }

    public Grass addObjectFactory(ObjectFactory factory) {
        if (this.isStarted) {
            throw new IllegalStateException("The grass application was started!");
        }

        if (factory == null) {
            throw new IllegalArgumentException("The factory cannot be null");
        }

        this.objectFactoryList.add(factory);

        return this;
    }

    public synchronized void start() {
        if (this.isStarted == true) {
            return;
        }

        initRouters();
        initClientProcesses();
        initServiceProcesses();

        this.context = new DefaultGrassContext(this.setting, this.objectFactoryList,
                this.protocolFactoryManager, this.serializerFactoryManager,
                Collections.unmodifiableList(this.clientProcessorList.get()),
                Collections.unmodifiableList(this.serviceProcessorList.get()),
                Collections.unmodifiableList(this.routerList.get()),
                this.defaultLoadBalance,
                this.defaultSpiRouter,
                this.serviceRepository,
                this.registry
        );

        initClients();
        initServices();


        exportServices();
        register();
        clean();

        this.isStarted = true;
    }

    private void initRouters() {
        Collections.sort(this.routerList.get(), OrderComparator.INSTANCE);
        this.routerList.get().stream().forEach(obj -> {
        });

        this.routerList.get().add(0, new DefaultRouter(this.registry));
    }

    private void initClientProcesses() {
        Collections.sort(this.clientProcessorList.get(), OrderComparator.INSTANCE);
    }

    private void initServiceProcesses() {
        Collections.sort(this.serviceProcessorList.get(), OrderComparator.INSTANCE);
    }

    private void register() {
        this.registry.register(this.setting);
    }

    private void exportServices() {
        ProtocolFactory factory = null;
        if (Utils.isEmpty(this.setting.getProtocol())) {
            if (this.protocolFactoryManager.getProtocolFactoryList().size() == 1) {
                factory = this.protocolFactoryManager.getProtocolFactoryList().get(0);
            } else {
                factory = this.protocolFactoryManager.getProtocolFactory("message");
                if (factory == null) {
                    factory = this.protocolFactoryManager.getProtocolFactory("http");
                }
            }
        }

        this.setting.setProtocol(factory.getId());
        Server server = factory.getServer();
        server.exportPublic(this.serviceRepository);
        server.start();
    }

    private void initClients() {
        this.clientBuilderList.get().stream().forEach(builder -> {
            Object obj = builder.build();
            clientRepository.add((Client) obj);
        });
    }

    private void initServices() {
        this.serviceBuilderList.get().stream().forEach(builder -> {
            Service obj = builder.build();
            serviceRepository.add(obj);
        });
    }


    private void clean() {
        this.routerList = null;
        this.clientProcessorList = null;
        this.serviceProcessorList = null;
        this.clientBuilderList = null;
        this.serviceBuilderList = null;
    }
}
