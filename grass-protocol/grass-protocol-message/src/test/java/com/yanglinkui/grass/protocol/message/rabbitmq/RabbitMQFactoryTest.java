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

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import com.yanglinkui.grass.protocol.message.api.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import static org.mockito.Mockito.*;

import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.*;

@ExtendWith(MockitoExtension.class)
public class RabbitMQFactoryTest {

    @Mock
    private ConnectionFactory connectionFactory;

    private CachingConnectionFactory factory;

    @Captor
    private ArgumentCaptor<MessageConsumer> messageConsumerArgumentCaptor;
    @Captor
    private ArgumentCaptor<String> stringArgumentCaptor;
    @Captor
    private ArgumentCaptor<Boolean> booleanArgumentCaptor;

    @BeforeEach
    public void init() {
        factory = spy(new CachingConnectionFactory(connectionFactory, 5));
    }

//    @Test
//    public void testInitExchange(@Mock Channel channel) throws IOException {
//        doReturn(channel).when(factory).getChannel();
//
//        RabbitMQFactory factory = new RabbitMQContainer("test", factory);
//        container.initExchange();
//        verify(channel).exchangeDeclare("test", BuiltinExchangeType.DIRECT, true);
//    }
//
//    @Test
//    public void testConsumer(@Mock Channel channel, @Mock MessageListener messageListener) throws IOException {
//        doReturn(channel).when(factory).getChannel(ChannelType.SUBSCRIBE);
//        List<String> routingKeys = new LinkedList<>();
//        routingKeys.add("routingKey");
//
//        RabbitMQContainer container = new RabbitMQContainer("test", factory);
//        Consumer consumer = container.getConsumer();
//        consumer.subscribe("topic", routingKeys);
//        consumer.setMessageListener(messageListener);
//        ((RabbitMQContainer.RabbitMQConsumer) consumer).start();
//
//        verify(channel).queueDeclare("topic.routingKey", false, false, false, null);
//        verify(channel).queueBind("topic.routingKey", "test", "topic.routingKey");
//        verify(channel).basicQos(1);
//
//        verify(channel).basicConsume(stringArgumentCaptor.capture(), booleanArgumentCaptor.capture(), messageConsumerArgumentCaptor.capture());
//        assertEquals("topic.routingKey", stringArgumentCaptor.getValue());
//        assertEquals(false, booleanArgumentCaptor.getValue());
//        assertEquals(messageListener, messageConsumerArgumentCaptor.getValue().getListener());
//        assertEquals(container, messageConsumerArgumentCaptor.getValue().getContainer());
//        assertEquals(channel, messageConsumerArgumentCaptor.getValue().getChannel());
//
//    }

    /*
    @Test
    public void testConsumer() throws Exception {
        Map<String, String> config = new HashMap<>();
        config.put("rabbitmq.username", "guest");
        config.put("rabbitmq.password", "guest");
        config.put("rabbitmq.vhost", "/");

        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory("localhost:5672", config, executorService);
        RabbitMQContainer container = new RabbitMQContainer("test", connectionFactory);
        Consumer consumer = container.getConsumer();
        consumer.subscribe("grass-test-topic", null);
        consumer.setMessageListener(((context, message) -> {
            System.out.println("Receive: " + new String(message.getBody()));
            context.commitAck();
        }));
        container.start();

        CountDownLatch countDownLatch = new CountDownLatch(1);
        executorService.submit(() -> {
            for (int i = 0; i < 1000; i++) {
                try {
                    System.out.println("Send: " + ("Jonas: " + i));
                    Producer producer = container.getProducer();
                    String id = UUID.randomUUID().toString();
                    Message message = new Message(("Jonas: " + i).getBytes())
                            .setId(id);
                    producer.send("grass-test-topic", null, message, new SendCallback() {
                        @Override
                        public void onSuccess() {
                            System.out.println("Success: " + id);
                        }

                        @Override
                        public void onException(Throwable ex) {
                            System.out.println("Fail: " + id);
                        }
                    });
                    Thread.currentThread().sleep(1_000);
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
