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

import com.yanglinkui.grass.exception.ConnectionException;
import com.yanglinkui.grass.protocol.message.HeaderConstants;
import com.yanglinkui.grass.protocol.message.api.Message;
import com.yanglinkui.grass.protocol.message.api.MessageContext;
import com.yanglinkui.grass.protocol.message.api.SendCallback;
import com.yanglinkui.grass.protocol.message.support.CommittedStatus;
import com.yanglinkui.grass.protocol.message.support.OffsetCommitter;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.utils.MessageUtil;
import org.apache.rocketmq.common.message.MessageExt;

public class RocketMQMessageContext implements MessageContext {

    private final OffsetCommitter committer;
    private final DefaultMQProducer producer;
    private final MessageExt messageExt;

    public RocketMQMessageContext(OffsetCommitter committer, DefaultMQProducer producer, MessageExt messageExt) {
        this.committer = committer;
        this.producer = producer;
        this.messageExt = messageExt;
    }

    @Override
    public void reply(Message message, SendCallback callback) throws Exception {
        org.apache.rocketmq.common.message.Message sendMsg = MessageUtil.createReplyMessage(messageExt, message.getBody());
        sendMsg.setKeys(message.getId());

        message.getProperties().entrySet().stream().forEach(e -> sendMsg.putUserProperty(e.getKey(), e.getValue()));
        sendMsg.putUserProperty(HeaderConstants.CONTENT_TYPE, message.getContentType());
        sendMsg.putUserProperty(HeaderConstants.CONTENT_ENCODING, message.getContentEncoding());
        sendMsg.putUserProperty(HeaderConstants.REQUEST_ID, message.getCorrelationId());

        this.producer.send(sendMsg, new org.apache.rocketmq.client.producer.SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                callback.onSuccess();
            }

            @Override
            public void onException(Throwable ex) {
                callback.onException(new ConnectionException("Failed to send message: " + message.getId(), ex));
            }
        });
    }

    @Override
    public void commitAck() throws Exception {
        this.committer.commitAsync(messageExt.getMsgId(), CommittedStatus.Type.SUCCESS);

    }
}
