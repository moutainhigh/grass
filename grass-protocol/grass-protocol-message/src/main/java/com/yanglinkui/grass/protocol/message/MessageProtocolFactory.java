package com.yanglinkui.grass.protocol.message;

import com.yanglinkui.grass.GrassContext;
import com.yanglinkui.grass.GrassContextManager;
import com.yanglinkui.grass.common.Utils;
import com.yanglinkui.grass.protocol.Client;
import com.yanglinkui.grass.protocol.ProtocolFactory;
import com.yanglinkui.grass.protocol.Server;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class MessageProtocolFactory implements ProtocolFactory {

    private static final int DEFAULT_NUM_THREADS = Runtime.getRuntime().availableProcessors() * 2;

    public static final String ID = "message";

    private final ExecutorService executorService;

    private final MessageClient client;

    private final MessageServer server;

    private final String prefix;

    public MessageProtocolFactory(String type, String addresses, Map<String, String> config, int concurrent, String prefix) {
        prefix = Utils.getOrDefault(prefix, System.getProperty("grass.message.prefix"));
        prefix = Utils.getOrDefault(prefix, GrassContextManager.getContext().getZone());

        this.prefix = prefix;
        this.executorService = Executors.newFixedThreadPool(concurrent < DEFAULT_NUM_THREADS ? DEFAULT_NUM_THREADS : concurrent);
        this.client = new MessageClient(executorService);
        this.server = new MessageServer(type, prefix, addresses, config, this.executorService);
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public Client getClient() {
        return this.client;
    }

    @Override
    public Server getServer() {
        return this.server;
    }
}
