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

package com.yanglinkui.grass.protocol.message.rocketmq;

import com.yanglinkui.grass.common.logger.Logger;
import com.yanglinkui.grass.common.logger.LoggerFactory;
import com.yanglinkui.grass.exception.ConnectionException;
import com.yanglinkui.grass.protocol.message.HeaderConstants;
import com.yanglinkui.grass.protocol.message.api.Message;
import com.yanglinkui.grass.protocol.message.api.Producer;
import com.yanglinkui.grass.protocol.message.api.RequestListener;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.RequestCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.remoting.exception.RemotingException;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class RocketMQProducer implements Producer {

    static Logger logger = LoggerFactory.getLogger(RocketMQConsumer.class);


    private static final long DEFAULT_ACK_COMMIT_TIMEOUT = 180_000;

    private static final int READY = 0;
    private static final int RUNNING = 1;
    private static final int SHUTDOWN = 3;

    private final String group;
    private final String addresses;
    private final Map<String, String> config;
    private final AtomicInteger status = new AtomicInteger(READY);
    private final DefaultMQProducer producer;

    public RocketMQProducer(String group, String addresses, Map<String, String> config) {
        this.group = group;
        this.addresses = addresses;
        this.config = config;
        this.producer = new DefaultMQProducer(group);
        this.producer.setNamesrvAddr(addresses);
    }

    @Override
    public void send(String topic, String routingKey, Message message, com.yanglinkui.grass.protocol.message.api.SendCallback  callback) throws IOException {
        checkStatus(RUNNING);

        org.apache.rocketmq.common.message.Message sendMsg = RocketMQUtils.mapToRocketMessage(message, topic, routingKey);
        try {
            producer.send(sendMsg, new org.apache.rocketmq.client.producer.SendCallback() {
                @Override
                public void onSuccess(SendResult sendResult) {
                    callback.onSuccess();
                }

                @Override
                public void onException(Throwable ex) {
                    callback.onException(new ConnectionException(ex.getMessage(), ex));
                }
            });
        } catch (MQClientException | InterruptedException e) {
            throw new IOException("Failed to send message: " + message.getId(), e);
        } catch (RemotingException e) {
            throw new IOException("Failed to send message: " + message.getId(), e);
        }
    }

    @Override
    public void send(String topic, String routingKey, Message message, RequestListener listener, long timeout) throws IOException {
        checkStatus(RUNNING);

        org.apache.rocketmq.common.message.Message sendMsg = RocketMQUtils.mapToRocketMessage(message, topic, routingKey);
        try {
            producer.request(sendMsg, new RequestCallback() {
                @Override
                public void onSuccess(org.apache.rocketmq.common.message.Message msg) {
                    Map<String, String> properties = msg.getProperties();
                    Message message = new Message(msg.getBody())
                            .setId(msg.getKeys())
                            .setContentType(properties.get(HeaderConstants.CONTENT_TYPE))
                            .setCorrelationId(HeaderConstants.REQUEST_ID)
                            .setContentEncoding(HeaderConstants.CONTENT_ENCODING)
                            .setProperties(properties);

                    listener.onSuccess(message);
                }

                @Override
                public void onException(Throwable ex) {
                    listener.onException(new ConnectionException(ex.getMessage(), ex));
                }
            }, timeout);
        } catch (MQClientException | RemotingException | InterruptedException | MQBrokerException e) {
            throw new IOException("Failed to send message: " + message.getId(), e);
        }
    }

    public void checkStatus(int checkedStatus) {
        if (status.get() != checkedStatus) {
            throw new IllegalStateException("Producer is not " + checkedStatus);
        }

    }

    @Override
    public String getGroup() {
        return this.group;
    }

    @Override
    public String getAddresses() {
        return this.addresses;
    }

    @Override
    public Map<String, String> getConfig() {
        return Collections.unmodifiableMap(this.config);
    }

    @Override
    public void start() throws Exception {
        this.producer.start();
    }

    @Override
    public void shutdown() throws Exception {
        this.producer.shutdown();
    }
}
