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

import com.yanglinkui.grass.DefaultGrassResponse;
import com.yanglinkui.grass.DefaultRawInputStream;
import com.yanglinkui.grass.common.logger.Logger;
import com.yanglinkui.grass.common.logger.LoggerFactory;
import com.yanglinkui.grass.protocol.message.api.Message;
import com.yanglinkui.grass.protocol.message.api.MessageContext;
import com.yanglinkui.grass.protocol.message.api.MessageListener;
import com.yanglinkui.grass.protocol.message.api.RequestListener;

import java.io.ByteArrayInputStream;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class DefaultRequestListener implements RequestListener {

    static Logger logger = LoggerFactory.getLogger(DefaultRequestListener.class);

    private final CompletableFuture future;

    public DefaultRequestListener(CompletableFuture future) {
        this.future = future;
    }

    @Override
    public void onSuccess(Message message) {
        logger.debug("Receive a response: {}", message.getCorrelationId());

        Map<String, String> properties = message.getProperties();

        DefaultGrassResponse response = new DefaultGrassResponse();
        response.setCorrelationId(message.getCorrelationId());
        response.setContentType(message.getContentType());
        response.setApplicationName(properties.get(HeaderConstants.PUBLISHER_APPLICATION));
        response.setStatus(Integer.valueOf(properties.get(HeaderConstants.RESPONSE_STATUS)));
        response.setInstanceId(properties.get(HeaderConstants.REMOTE_USER));

        response.setServiceId(properties.get(HeaderConstants.SERVICE_ID));
        response.setAction(properties.get(HeaderConstants.ACTION));
        response.setVersion(properties.get(HeaderConstants.SERVICE_VERSION));
        response.setZone(properties.get(HeaderConstants.ZONE));
        response.setInvokedTime(Long.valueOf(properties.get(HeaderConstants.INVOKED_TIME)));
        response.setReturnedTime(Long.valueOf(properties.get(HeaderConstants.RETURNED_TIME)));

        response.setInputStream(new DefaultRawInputStream(message.getContentType(),
                message.getContentEncoding(), new ByteArrayInputStream(message.getBody())));

        properties.entrySet().stream()
                .filter(e -> !HeaderConstants.RESERVED_KEYWORDS.contains(e.getKey()))
                .forEach(e -> response.setHeader(e.getKey(), e.getKey()));

        this.future.complete(response);
    }

    @Override
    public void onException(Throwable ex) {
        future.completeExceptionally(ex);
    }
}
