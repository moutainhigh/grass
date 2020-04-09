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

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.ConfirmListener;
import com.rabbitmq.client.ReturnListener;
import com.yanglinkui.grass.common.Utils;
import com.yanglinkui.grass.common.logger.Logger;
import com.yanglinkui.grass.common.logger.LoggerFactory;
import com.yanglinkui.grass.exception.ConnectionException;
import com.yanglinkui.grass.protocol.message.api.RequestListener;
import com.yanglinkui.grass.protocol.message.api.SendCallback;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

public class SendCallbackListener implements ConfirmListener, ReturnListener {
    static Logger logger = LoggerFactory.getLogger(SendCallbackListener.class);

    private final ConcurrentSkipListMap<Long/* deliveryTag */, String /* messageId */> confirmTagList;
    private final Map<String/* messageId */, String/* correlationId */> correlationIdList;
    private final Map<String/* correlationId */, RequestListener> requestListenerList;
    private final Map<String/* messageId */, SendCallback> sendCallbackList;

    public SendCallbackListener(Map<String, SendCallback> sendCallbackList,
                                Map<String, String> correlationIdList,
                                Map<String, RequestListener> requestListenerList,
                                ConcurrentSkipListMap<Long, String> confirmTagList) {
        this.confirmTagList = confirmTagList;
        this.correlationIdList = correlationIdList == null ? Collections.EMPTY_MAP : correlationIdList;
        this.requestListenerList = requestListenerList == null ? Collections.EMPTY_MAP : requestListenerList;
        this.sendCallbackList =  sendCallbackList == null ? Collections.EMPTY_MAP : sendCallbackList;
    }

    @Override
    public void handleAck(long deliveryTag, boolean multiple) throws IOException {
        logger.debug("Success to confirm: {}", deliveryTag);
        if (multiple) {
            Map<Long, String> head = this.confirmTagList.headMap(deliveryTag + 1);
            head.forEach((tag, messageId) -> {
                SendCallback sendCallback = sendCallbackList.remove(messageId);
                if (sendCallback != null) {
                    sendCallback.onSuccess();
                }
            });
            head.clear();
        } else {
            this.confirmTagList.remove(deliveryTag);
        }
    }

    @Override
    public void handleNack(long deliveryTag, boolean multiple) throws IOException {
        logger.debug("Fail to confirm: {}", deliveryTag);

        if (multiple) {
            Map<Long, String> head = this.confirmTagList.headMap(deliveryTag + 1);
            head.forEach((tag, messageId) -> callOnException(messageId));
            head.clear();
        } else {
            String messageId = this.confirmTagList.remove(deliveryTag);
            callOnException(messageId);
        }
    }

    /**
     * ReturnListener
     */
    @Override
    public void handleReturn(int replyCode, String replyText, String exchange, String routingKey,
                             AMQP.BasicProperties properties, byte[] body) throws IOException {
        logger.error("Failed to route. Message id: {}, reply code: {}, routing key: {}", properties.getMessageId(), replyCode, routingKey);
    }

    private void callOnException(String messageId) {
        ConnectionException exception = new ConnectionException("Request broken on sending: " + messageId);
        //调用sendCallback
        SendCallback sendCallback = this.sendCallbackList.remove(messageId);
        if (sendCallback != null) {
            sendCallback.onException(exception);
        }

        //调用requestListener
        String correlationId = correlationIdList.remove(messageId);
        if (Utils.isNotEmpty(correlationId)) {
            RequestListener listener = requestListenerList.remove(correlationId);
            if (listener != null) {
                listener.onException(exception);
            }
        }
    }
}
