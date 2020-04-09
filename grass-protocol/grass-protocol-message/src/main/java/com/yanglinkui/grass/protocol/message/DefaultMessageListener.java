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

package com.yanglinkui.grass.protocol.message;

import com.yanglinkui.grass.*;
import com.yanglinkui.grass.common.Utils;
import com.yanglinkui.grass.common.logger.Logger;
import com.yanglinkui.grass.common.logger.LoggerFactory;
import com.yanglinkui.grass.common.timer.Timeout;
import com.yanglinkui.grass.common.timer.TimerTask;
import com.yanglinkui.grass.exception.ConnectionException;
import com.yanglinkui.grass.protocol.message.api.Message;
import com.yanglinkui.grass.protocol.message.api.MessageContext;
import com.yanglinkui.grass.protocol.message.api.MessageListener;
import com.yanglinkui.grass.protocol.message.api.SendCallback;
import com.yanglinkui.grass.protocol.message.support.Timer;
import com.yanglinkui.grass.provider.ServiceRepository;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class DefaultMessageListener implements MessageListener {
    static Logger logger = LoggerFactory.getLogger(DefaultMessageListener.class);

    private final ServiceRepository serviceRepository;
    private final AtomicInteger concurrentRequestCounter;

    public DefaultMessageListener(ServiceRepository serviceRepository, AtomicInteger concurrentRequestCounter) {
        this.serviceRepository = serviceRepository;
        this.concurrentRequestCounter = concurrentRequestCounter;
    }

    @Override
    public void onMessage(MessageContext context, Message message) throws Exception {
        concurrentRequestCounter.incrementAndGet();
        try {
            logger.debug("Receive a message: {}", message.getId());
            doProcess(context, message);
        } finally {
            concurrentRequestCounter.decrementAndGet();
        }
    }

    private void doProcess(MessageContext context, Message message) throws Exception {
        Map<String, String> properties = message.getProperties();

        //构建Request
        DefaultGrassRequest request = new DefaultGrassRequest();
        request.setId(message.getCorrelationId());
        request.setContentType(message.getContentType());
        request.setApplicationName(properties.get(HeaderConstants.PUBLISHER_APPLICATION));
        request.setServiceId(properties.get(HeaderConstants.SERVICE_ID));
        request.setAction(properties.get(HeaderConstants.ACTION));
        request.setVersion(properties.get(HeaderConstants.SERVICE_VERSION));
        request.setZone(properties.get(HeaderConstants.ZONE));
        request.setRemoteUser(properties.get(HeaderConstants.REMOTE_USER));
        if (properties.get(HeaderConstants.REQUEST_TIME) != null) {
            request.setRequestTime(Long.valueOf(properties.get(HeaderConstants.REQUEST_TIME)));
        }

        request.setInputStream(new DefaultRawInputStream(message.getContentType(),
                message.getContentEncoding(), new ByteArrayInputStream(message.getBody())));

        properties.entrySet().stream()
                .filter(e -> !HeaderConstants.RESERVED_KEYWORDS.contains(e.getKey()))
                .forEach(e -> request.setHeader(e.getKey(), e.getValue()));

        //调用invoker
        Executor<GrassResponse> executor = GrassContextManager.getContext().getServiceExecutorBuilder()
                .setServiceRepository(this.serviceRepository)
                .build();
        GrassResponse response = executor.execute(request);

        //如果不是请求模式，就不要回复了
        if (message.getType() == Message.Type.EVENT) { //TODO: 支持下一个事件
            context.commitAck();
        } else {
            reply(context, response, 0);
        }
    }

    private void reply(MessageContext context, GrassResponse response, int retryCount) throws Exception {
        Map<String, String> responseProperties = new HashMap<>();
        responseProperties.put(HeaderConstants.RESPONSE_STATUS, String.valueOf(response.getStatus()));
        responseProperties.put(HeaderConstants.SERVICE_ID, response.getServiceId());
        responseProperties.put(HeaderConstants.ACTION, response.getAction());
        responseProperties.put(HeaderConstants.SERVICE_VERSION, response.getVersion());
        responseProperties.put(HeaderConstants.RESPONSE_INSTANCE, response.getInstanceId());
        if (response.getInvokedTime() != null) {
            responseProperties.put(HeaderConstants.INVOKED_TIME, String.valueOf(response.getInvokedTime()));
        }
        if (response.getReturnedTime() != null) {
            responseProperties.put(HeaderConstants.RETURNED_TIME, String.valueOf(response.getReturnedTime()));
        }

        Stream.of(response.getHeaderNames())
                .filter(name -> !HeaderConstants.RESERVED_KEYWORDS.contains(name)).
                forEach(name -> responseProperties.put(name, response.getHeader(name)));

        Object result = response.getBody();
        byte[] responseBody = null;
        if (result != null) {
            try {
                responseBody = GrassContextManager.getContext().getSerializer(response.getContentType()).serialize(result);
            } catch (IOException e) {
                logger.error("Failed to serialized response: " + response.getCorrelationId(), e);
                responseProperties.put(HeaderConstants.RESPONSE_STATUS, String.valueOf(GrassResponse.INTERNAL_ERROR));
            }
        }

        Message responseMessage = new Message(responseBody)
                .setCorrelationId(response.getCorrelationId())
                .setContentType(response.getContentType())
                .setContentEncoding("utf-8")
                .setProperties(responseProperties)
                .setId(UUID.randomUUID().toString());

        context.reply(responseMessage, new SendCallback() {
            @Override
            public void onSuccess() {
                try {
                    context.commitAck();
                } catch (Exception e) {
                    logger.error("Failed to commit ack: " + response.getCorrelationId(), e);
                }
            }

            @Override
            public void onException(Throwable ex) {
                logger.error("Failed to send response: " + response.getCorrelationId(), ex);
                if (retryCount < 1) {
                    logger.info("Attempt({}) to resend response: {}", retryCount + 1, response.getCorrelationId());
                    //一秒后重试
                    Timer.newTimeout(timeout -> {
                        try {
                            reply(context, response, retryCount + 1);
                        } catch (Exception e) {
                            logger.error("Failed to invoke reply to resend response: " + response.getCorrelationId(), e);
                        }
                    }, 1_000, TimeUnit.MILLISECONDS);
                } else {
                    try {
                        context.commitAck(); //消息ACK
                    } catch (Exception e) {
                        logger.error("Failed to commit ack: " + response.getCorrelationId(), e);
                    }
                    logger.error("No way to recovery to resend response: " + response.getCorrelationId());
                }
            }
        });
    }
}
