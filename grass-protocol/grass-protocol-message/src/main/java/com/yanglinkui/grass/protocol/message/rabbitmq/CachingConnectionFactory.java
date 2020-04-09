package com.yanglinkui.grass.protocol.message.rabbitmq;

import com.rabbitmq.client.*;
import com.yanglinkui.grass.common.Utils;
import com.yanglinkui.grass.exception.TimeoutException;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class CachingConnectionFactory {

    public final static int DEFAULT_CACHE_SIZE = 10;

    private final Address[] addresses;
    private final ConnectionFactory factory;
    private volatile Connection connection;

    private final Object lock = new Object();
    private final Semaphore permits;

    private final int cacheSize;
    private final LinkedList<ChannelProxy> channelList = new LinkedList<>();

    private volatile ConfirmListener confirmListener;
    private volatile ReturnListener returnListener;

    public CachingConnectionFactory(String addresses, Map<String, String> config, ExecutorService executorService) {
        this(addresses, config, null, null, executorService);
    }

    public CachingConnectionFactory(String addresses, Map<String, String> config, ConfirmListener confirmListener, ReturnListener returnListener, ExecutorService executorService) {
        this.addresses = resolveAddresses(addresses);
        String username = config.get("rabbitmq.username");
        String password = config.get("rabbitmq.password");
        String vhost = config.get("rabbitmq.vhost");
        int cacheSize = config.get("rabbitmq.cache-size") == null ? DEFAULT_CACHE_SIZE : Integer.valueOf(config.get("rabbitmq.cache-size"));

        this.factory = createConnectionFactory(username, password, vhost, executorService);
        this.factory.setNetworkRecoveryInterval(1000L);
        this.factory.setAutomaticRecoveryEnabled(true);
        this.factory.setTopologyRecoveryEnabled(true);
        this.permits = new Semaphore(cacheSize);
        this.cacheSize = cacheSize;
        this.confirmListener = confirmListener;
        this.returnListener = returnListener;
    }


    public CachingConnectionFactory(ConnectionFactory factory, int cacheSize) {
        this.addresses = null;
        this.factory = factory;
        this.factory.setNetworkRecoveryInterval(1000L);
        this.factory.setAutomaticRecoveryEnabled(true);
        this.factory.setTopologyRecoveryEnabled(true);
        this.permits = new Semaphore(cacheSize);
        this.cacheSize = cacheSize;
        this.confirmListener = null;
        this.returnListener = null;
    }

    public static ConnectionFactory createConnectionFactory(String username, String password, String vhost, ExecutorService executorService) {
        ConnectionFactory factory = new ConnectionFactory();

        if (executorService != null) {
            factory.setSharedExecutor(executorService);
        }

        if (username != null && username.length() > 0) {
            factory.setUsername(username);
        }

        if (password != null && password.length() > 0) {
            factory.setPassword(password);
        }

        if (vhost != null) {
            factory.setVirtualHost(vhost);
        }

        return factory;
    }

    public ConfirmListener getConfirmListener() {
        return confirmListener;
    }

    public ReturnListener getReturnListener() {
        return returnListener;
    }

    public void setConfirmListener(ConfirmListener confirmListener) {
        this.confirmListener = confirmListener;
    }

    public void setReturnListener(ReturnListener returnListener) {
        this.returnListener = returnListener;
    }

    /**
     * 格式 host:port,host:port
     * @param addressesStr
     * @return
     */
    static Address[] resolveAddresses(String addressesStr) {
        if (Utils.isEmpty(addressesStr)) {
            throw new IllegalArgumentException("addresses cannot be null");
        }

        String[] hosts = addressesStr.split(",");

        return Arrays.stream(hosts).map(hostStr -> {
                    String[] hostAndPort = hostStr.split(":");
                    if (hostAndPort.length != 2) {
                        throw new IllegalArgumentException("It is an invalid addresses string: " + addressesStr);
                    }

                    return new Address(hostAndPort[0], Integer.valueOf(hostAndPort[1])); })
                .collect(Collectors.toList())
                .toArray(new Address[0]);
    }

    public Channel getChannel() {
        return getChannel(ChannelType.PUBLISH);
    }

    public Channel getChannel(ChannelType type) {
        if (type == null) {
            throw new IllegalArgumentException("type cannot be null");
        }
        return getChannel(1_000, type);
    }

    public Channel getChannel(long timeout, ChannelType type) {
        if (timeout <= 0) {
            throw new IllegalArgumentException("The timer cannot be less than or equal to 0");
        }

        Semaphore permits = this.permits;
        ChannelProxy channelProxy = null;
        try {
            if (type == ChannelType.PUBLISH && !permits.tryAcquire(timeout, TimeUnit.MILLISECONDS)) {
                throw new TimeoutException("No available channels");
            }

            channelProxy = getCachedChannelProxy();
            if (channelProxy == null) {
                Connection connection = getConnection();
                Channel channel = connection.createChannel();
                channel.confirmSelect();

                if (this.confirmListener != null) {
                    channel.addConfirmListener(this.confirmListener);
                }

                if (this.returnListener != null) {
                    channel.addReturnListener(this.returnListener);
                }

                channelProxy = (ChannelProxy) Proxy.newProxyInstance(ChannelProxy.class.getClassLoader(),
                        new Class<?>[]{ChannelProxy.class},
                        new ChannelProxyInvocationHandler(type, connection, channel, this.channelList,
                                type == ChannelType.PUBLISH ? this.permits : null));
            }
        } catch (Throwable e) {
            if (type == ChannelType.PUBLISH) {
                permits.release();
            }

            throw new RuntimeException(e);
        }

        return channelProxy;
    }

    private ChannelProxy getCachedChannelProxy() {
        ChannelProxy channelProxy = null;
        synchronized (this.channelList) {
            while (!this.channelList.isEmpty()) {
                channelProxy = this.channelList.removeFirst();
                if (channelProxy.getTargetChannel().isOpen()) {
                    channelProxy.active();
                    break;
                } else {
                    cleanUpClosedChannel(channelProxy);
                }
            }
        }

        return channelProxy;
    }

    private Connection getConnection() throws IOException, java.util.concurrent.TimeoutException {
        if (this.connection != null && this.connection.isOpen()) {
            return this.connection;
        }

        synchronized (this.lock) {
            if (this.connection != null && this.connection.isOpen()) {
                return this.connection;
            }

            Connection connection = null;
            if (this.addresses != null) {
                connection = this.factory.newConnection(this.addresses);
            } else {
                connection = this.factory.newConnection();
            }
            this.connection = connection;

            return connection;
        }
    }

    private void cleanUpClosedChannel(ChannelProxy channel) {
        try {
            Channel target = channel.getTargetChannel();
            if (target != null) {
                target.close();
            }
        } catch (AlreadyClosedException e) {
            //The channel is already closed.
        } catch (IOException e) {
            //Unexpected Exception closing channel + e.getMessage();
        } catch (java.util.concurrent.TimeoutException e) {
            //TimeoutException closing channel.
        }
    }

    public int getCacheSize() {
        return this.cacheSize;
    }

    public void destroy() {
        if (this.connection != null) {
            try {
                this.connection.close();
            } catch (Exception e) {
                //log
            }
        }
    }

    /**
     * 难点在于channel本身带有一些非线程安全的方法，例如close被多次调用后的状态（其实已经返回channel池了）
     */
    private final class ChannelProxyInvocationHandler implements InvocationHandler {

        private ChannelType type;

        private final Connection connection;
        private final LinkedList<ChannelProxy> channelList;

        private volatile Channel target;
        private final Semaphore permits;

        private volatile boolean isActive = true;
        private final Object targetMonitor = new Object();

        ChannelProxyInvocationHandler(ChannelType type, Connection connection, Channel target, LinkedList<ChannelProxy> channelList, Semaphore permits) {
            this.type = type;
            this.connection = connection;
            this.target = target;
            this.channelList = channelList;
            this.permits = permits;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String methodName = method.getName();
            if (methodName.equals("equals")) {
                return (proxy == args[0]);
            } else if (methodName.equals("hashCode")) {
                return System.identityHashCode(proxy);
            } else if (methodName.equals("toString")) {
                return "Cached Rabbit Channel: " + this.target + ", conn: " + this.connection;
            } else  if (methodName.equals("isOpen")) {
                return this.isActive && this.target != null && this.target.isOpen();
            } else if (methodName.equals("close")) {
                if (this.type == ChannelType.PUBLISH) {
                    synchronized (this.channelList) {
                        //因为可能close会被多次调用，所以要检查包含这个状态
                        if (this.channelList.size() < getCacheSize() || this.channelList.contains(proxy)) {
                            logicalClose((ChannelProxy) proxy);
                            return null;
                        }
                    }
                }

                //如果是CONSUME类型就直接关闭
                physicalClose(proxy);
                return null;
            } else if (methodName.equals("getTargetChannel")) {
                return this.target;
            } else if (methodName.equals("active")) {
                this.isActive = true;
                return null;
            }

            if (this.isActive == false) {
                throw new IllegalStateException("Failed to invoke: " + methodName + ". Because the channel proxy is closed.");
            }

            try {
                if (this.target == null || !this.target.isOpen()) {
                    this.target = null;
                }

                synchronized (targetMonitor) {
                    if (this.target == null || !this.target.isOpen()) {
                        this.target = getConnection().createChannel();
                    }

                    Object object = method.invoke(this.target, args);
                    return object;
                }

            } catch (InvocationTargetException ex) {
                if (this.target == null || !this.target.isOpen()) {
                    this.target = null;
                    synchronized (this.targetMonitor) {
                        if (this.target == null) {
                            this.target = getConnection().createChannel();
                        }
                    }
                }
                throw ex.getTargetException();
            }
        }

        private void logicalClose(Channel proxy) {
            //必须同步，要不然释放了锁，另外一边会立即获得锁后，
            //发现并没有channel，就会创建新的channel
            synchronized (this.channelList) {
                if (!this.channelList.contains(proxy)) {
                    releasePermitIfNecessary(proxy);
                    this.isActive = false;
                    this.channelList.addLast((ChannelProxy) proxy);
                }
            }
        }

        private void releasePermitIfNecessary(Object proxy) {
            synchronized (this.channelList) {
                if (this.channelList.contains(proxy)) {
                    return;
                }
            }

            if (this.permits != null) {
                this.permits.release();
            }
        }

        private void physicalClose(Object proxy) throws IOException, java.util.concurrent.TimeoutException {
            try {
                this.target.close();
            } catch (AlreadyClosedException e) {
                //log
            }
            finally {
                releasePermitIfNecessary(proxy);
            }
        }

    }
}
