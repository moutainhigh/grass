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

import com.yanglinkui.grass.common.StringOptional;
import com.yanglinkui.grass.common.logger.Logger;
import com.yanglinkui.grass.common.logger.LoggerFactory;
import com.yanglinkui.grass.protocol.message.api.Consumer;
import com.yanglinkui.grass.protocol.message.api.MessageListener;
import com.yanglinkui.grass.protocol.message.support.LocalOffsetCommitter;
import com.yanglinkui.grass.protocol.message.support.OffsetCommitService;
import org.apache.rocketmq.client.consumer.DefaultLitePullConsumer;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.message.MessageQueue;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

public class RocketMQConsumer implements Consumer {
    static Logger logger = LoggerFactory.getLogger(RocketMQConsumer.class);


    private static final long DEFAULT_ACK_COMMIT_TIMEOUT = 180_000;

    private static final int READY = 0;
    private static final int RUNNING = 1;
    private static final int SHUTDOWN = 3;

    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    private List<DefaultLitePullConsumer> consumerList = new LinkedList<>();

    private final String group;
    private final String addresses;
    private final Map<String, String> config;
    private final ExecutorService executorService;
    private final OffsetCommitService<MessageQueue> offsetCommitService;
    private final long ackCommitTimeout;
    private final AtomicInteger status = new AtomicInteger(READY);
    private final DefaultMQProducer producer;

    private MessageListener listener;
    private Map<String/* topic */, String/* tags */> topicMap = new HashMap<>();


    public RocketMQConsumer(String group, String addresses, Map<String, String> config, ExecutorService executorService) {
        this.group = group;
        this.addresses = addresses;
        this.config = config;
        this.executorService = executorService;
        this.offsetCommitService = new OffsetCommitService<>();
        this.ackCommitTimeout = DEFAULT_ACK_COMMIT_TIMEOUT;

        this.producer = new DefaultMQProducer(group);
        this.producer.setNamesrvAddr(addresses);
    }

    @Override
    public Consumer subscribe(String topic, String... routingKeys) {
        if (routingKeys == null) {
            routingKeys = EMPTY_STRING_ARRAY;
        }
        StringBuilder tags = new StringBuilder("___");
        Arrays.stream(routingKeys).forEach(tag -> tags.append("||").append(tag));
        topicMap.put(topic, tags.toString());

        return this;
    }

    @Override
    public Consumer setMessageListener(MessageListener listener) {
        this.listener = listener;
        return this;
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
        return this.config;
    }

    @Override
    public void start() throws Exception {
        for (Map.Entry<String, String> entry : topicMap.entrySet()) {
            String topic = entry.getKey();
            String tags = entry.getValue();
            createConsumerAndStartup(topic, tags);
        }
    }

    private void createConsumerAndStartup(String topic, String tags) throws MQClientException {
        this.producer.start();

        tags = StringOptional.of(tags).orElse("*");
        DefaultLitePullConsumer consumer = new DefaultLitePullConsumer(this.group);
        consumer.setNamesrvAddr(this.addresses);
        consumer.setAutoCommit(false);
        consumer.setConsumerPullTimeoutMillis(50L);
        consumer.subscribe(topic, tags);
        consumer.start();

        this.consumerList.add(consumer);
        executorService.submit(() -> {
            while (status.get() == RUNNING)
                try {
                    List<MessageExt> list = consumer.poll();
                    for (MessageExt msg : list) {
                        MessageQueue messageQueue = new MessageQueue(msg.getTopic(), msg.getBrokerName(), msg.getQueueId());
                        LocalOffsetCommitter<MessageQueue> committer = new LocalOffsetCommitter<>(messageQueue,
                                msg.getQueueOffset(), list.size(),
                                (queue, offset) -> consumer.getOffsetStore().updateOffset(queue, offset, false), ackCommitTimeout);
                        offsetCommitService.add(committer);
                        executorService.submit(new MessageListenerTask(producer, msg, listener, committer));
                    }
                } catch (Throwable ex) {
                    logger.error("Failed to pull message", ex);
                }
        });
    }

    @Override
    public void shutdown() throws Exception {
        this.consumerList.stream().forEach(consumer -> consumer.shutdown());
        this.consumerList.clear();
        this.producer.shutdown();
    }
}
