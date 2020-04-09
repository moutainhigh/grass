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

import com.yanglinkui.grass.protocol.message.api.*;

import java.util.function.BiConsumer;

public class RabbitMQMessageContext implements MessageContext {

    Runnable commitAck;
    BiConsumer<Message, SendCallback> reply;

    public RabbitMQMessageContext(BiConsumer<Message, SendCallback> reply, Runnable commitAck) {
        this.commitAck = commitAck;
        this.reply = reply;
    }

    @Override
    public void reply(Message message, SendCallback callback) throws Exception {
        this.reply.accept(message, callback);
    }

    @Override
    public void commitAck() throws Exception {
        commitAck.run();
    }
}
