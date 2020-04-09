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

import com.yanglinkui.grass.common.timer.Timeout;
import com.yanglinkui.grass.common.timer.TimerTask;
import com.yanglinkui.grass.exception.TimeoutException;
import com.yanglinkui.grass.protocol.message.api.RequestListener;

import java.util.Map;

public class RequestTimeoutTask implements TimerTask {

    private final String messageId;
    private final String correlationId;
    private final long timeout;
    private final Map<String/* correlationId */, RequestListener> requestListenerList;

    public RequestTimeoutTask(String messageId, String correlationId, long timeout, Map<String, RequestListener> requestListenerList) {
        this.messageId = messageId;
        this.correlationId = correlationId;
        this.timeout = timeout;
        this.requestListenerList = requestListenerList;
    }

    @Override
    public void run(Timeout timeout) throws Exception {
        RequestListener listener = this.requestListenerList.remove(this.correlationId);
        if (listener != null) {
            listener.onException(new TimeoutException("The time of response exceeded " + this.timeout));
        }
    }
}
