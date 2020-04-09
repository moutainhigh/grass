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

import com.yanglinkui.grass.common.Utils;
import com.yanglinkui.grass.protocol.message.HeaderConstants;
import org.apache.rocketmq.common.message.Message;

public class RocketMQUtils {

    public static String getGroup(String prefix, String applicationName) {
        String group = applicationName;
        if (Utils.isNotEmpty(prefix)) {
            group = prefix + "." + group;
        }

        return group;
    }


    public static Message mapToRocketMessage(com.yanglinkui.grass.protocol.message.api.Message message,
                                             String topic, String routingKey) {
        org.apache.rocketmq.common.message.Message sendMsg = new org.apache.rocketmq.common.message.Message();
        sendMsg.setBody(message.getBody());
        sendMsg.setTopic(topic);
        sendMsg.setTags(routingKey);
        sendMsg.setKeys(message.getId());

        message.getProperties().entrySet().stream().forEach(e -> sendMsg.putUserProperty(e.getKey(), e.getValue()));
        sendMsg.putUserProperty(HeaderConstants.CONTENT_TYPE, message.getContentType());
        sendMsg.putUserProperty(HeaderConstants.CONTENT_ENCODING, message.getContentEncoding());
        sendMsg.putUserProperty(HeaderConstants.REQUEST_ID, message.getCorrelationId());
        return sendMsg;
    }

}
