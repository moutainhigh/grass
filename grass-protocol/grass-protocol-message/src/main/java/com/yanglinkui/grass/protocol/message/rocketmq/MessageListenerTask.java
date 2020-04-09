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
import com.yanglinkui.grass.protocol.message.HeaderConstants;
import com.yanglinkui.grass.protocol.message.api.*;
import com.yanglinkui.grass.protocol.message.support.CommittedStatus;
import com.yanglinkui.grass.protocol.message.support.OffsetCommitter;
import org.apache.kafka.common.internals.Topic;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.utils.MessageUtil;
import org.apache.rocketmq.common.message.MessageExt;

import java.util.HashMap;
import java.util.Map;

public class MessageListenerTask implements Runnable {

    static Logger logger = LoggerFactory.getLogger(MessageListenerTask.class);

    private final DefaultMQProducer producer;
    private final OffsetCommitter committer;
    private final MessageExt msg;
    private final MessageListener listener;

    public MessageListenerTask(DefaultMQProducer producer, MessageExt msg, MessageListener listener, OffsetCommitter committer) {
        this.producer = producer;
        this.committer = committer;
        this.msg = msg;
        this.listener = listener;
    }

    @Override
    public void run() {
        try {
            Map<String, String> msgProperties = this.msg.getProperties();
            Map<String, String> properties = new HashMap<>(msgProperties.size(), 1F);
            msgProperties.entrySet().stream()
                    .filter(e -> !HeaderConstants.RESERVED_KEYWORDS.contains(e.getKey()))
                    .forEach(e -> properties.put(e.getKey(), e.getValue()));

            Message message = new Message(msg.getBody())
                    .setId(msgProperties.get(HeaderConstants.MESSAGE_ID))
                    .setCorrelationId(msgProperties.get(HeaderConstants.REQUEST_ID))
                    .setContentType(msgProperties.get(HeaderConstants.CONTENT_TYPE))
                    .setContentEncoding(msgProperties.get(HeaderConstants.CONTENT_ENCODING))
                    .setProperties(properties);

            this.listener.onMessage(new RocketMQMessageContext(this.committer, this.producer, this.msg), message);
        } catch (Throwable ex) {
            logger.error("Failed to process message: " + msg.getKeys() + " of topic: " + msg.getTopic(), ex);
            committer.commitAsync(this.msg.getMsgId(), CommittedStatus.Type.FAIL);
        }
    }
}
