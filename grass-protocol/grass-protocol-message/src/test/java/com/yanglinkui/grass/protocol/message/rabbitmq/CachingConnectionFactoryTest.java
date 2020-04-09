/*
 * Copyright 2019 Jonas Yang
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.yanglinkui.grass.protocol.message.rabbitmq;

import com.rabbitmq.client.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.*;

import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

@ExtendWith(MockitoExtension.class)
public class CachingConnectionFactoryTest {

    @Test
    public void testResolveAddresses_validString() {
        String addressesStr = "localhost:5672";
        Address[] addresses = CachingConnectionFactory.resolveAddresses(addressesStr);
        assertEquals(1, addresses.length);
        assertEquals("localhost", addresses[0].getHost());
        assertEquals(5672, addresses[0].getPort());

        addressesStr = "localhost:5672,127.0.0.1:5673";
        addresses = CachingConnectionFactory.resolveAddresses(addressesStr);
        assertEquals(2, addresses.length);
        assertEquals("localhost", addresses[0].getHost());
        assertEquals(5672, addresses[0].getPort());
        assertEquals("127.0.0.1", addresses[1].getHost());
        assertEquals(5673, addresses[1].getPort());

        addressesStr = "localhost:5711,";
        addresses = CachingConnectionFactory.resolveAddresses(addressesStr);
        assertEquals(1, addresses.length);
    }

    @Test
    public void testResolveAddresses_invalidString() {
        String addressesStr = "localhost:";
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> CachingConnectionFactory.resolveAddresses(addressesStr));
        assertEquals("It is an invalid addresses string: localhost:", e.getMessage());

        String addressesStr1 = "localhost:abc";
        NumberFormatException nfe = assertThrows(NumberFormatException.class, () -> CachingConnectionFactory.resolveAddresses(addressesStr1));
        assertEquals("For input string: \"abc\"", nfe.getMessage());
    }

    @Test
    public void testGetChannel_publish(@Mock ConnectionFactory connectionFactory, @Mock Connection connection, @Mock Channel channel) throws IOException, TimeoutException {
        when(connectionFactory.newConnection()).thenReturn(connection);
        when(connection.createChannel()).thenReturn(channel);
        CachingConnectionFactory factory = new CachingConnectionFactory(connectionFactory, 1);
        Channel channel1 = factory.getChannel();
        assertNotNull(channel1);
        RuntimeException exception = assertThrows(RuntimeException.class, () -> factory.getChannel());
        assertEquals(com.yanglinkui.grass.exception.TimeoutException.class, exception.getCause().getClass());
        channel.close();
        assertNotNull(factory.getChannel());
    }

    @Test
    public void testGetChannel_subscribe(@Mock ConnectionFactory connectionFactory, @Mock Connection connection, @Mock Channel channel) throws IOException, TimeoutException {
        when(connectionFactory.newConnection()).thenReturn(connection);
        when(connection.createChannel()).thenReturn(channel);
        CachingConnectionFactory factory = new CachingConnectionFactory(connectionFactory, 1);
        for (int i = 0; i < 10; i++) {
            assertNotNull(factory.getChannel(ChannelType.SUBSCRIBE));
        }
    }


    /*
    @Test
    public void testIntegration() throws IOException, InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);

        Map<String, String> config = new HashMap<>();
        config.put("rabbitmq.username", "guest");
        config.put("rabbitmq.password", "guest");
        config.put("rabbitmq.vhost", "/");
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CachingConnectionFactory factory = new CachingConnectionFactory("localhost:5672", config, null, null, executorService);
        Channel channel = factory.getChannel(ChannelType.SUBSCRIBE);
        channel.exchangeDeclare("test", BuiltinExchangeType.DIRECT, true);
        channel.queueDeclare("grass-test-topic", false, false, false, null);
        channel.queueBind("grass-test-topic", "test", "");
        channel.basicConsume("grass-test-topic", new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                System.out.println("Receive: " + new String(body));
                channel.basicAck(envelope.getDeliveryTag(), false);
            }
        });

        executorService.submit(() -> {
            for (int i = 0; i < 1000; i++) {
                try {
                    System.out.println("Send: " + ("Jonas: " + i));
                    Channel ch = factory.getChannel();
                    ch.basicPublish("test", "", null, ("Jonas: " + i).getBytes("UTF-8"));
                    ch.close();
                    Thread.currentThread().sleep(10);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            countDownLatch.countDown();
        });

        countDownLatch.await();

    }
    */
}
