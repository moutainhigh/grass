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

package com.yanglinkui.grass.protocol.message.support;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

public class LocalOffsetCommitter<T> implements OffsetCommitter, CommittedStatus<T> {

    private final T key;
    private final long offset;

    private final AtomicInteger commitCount = new AtomicInteger(0);
    private final int messageAmount;

    private final Map<String, CommittedStatus.Type> statusMap;
    private final  BiConsumer<T, Long> commitFunction;

    private final long timeout;
    private final long startedTime;

    public LocalOffsetCommitter(T key, long offset, int messageAmount, BiConsumer<T, Long> commitFunction, long timeout) {
        this.key = key;
        this.messageAmount = messageAmount;
        this.statusMap = new ConcurrentHashMap<>(messageAmount, 1F);
        this.offset = offset;
        this.commitFunction = commitFunction;
        this.timeout = timeout;
        this.startedTime = System.currentTimeMillis();
    }

    public long getOffset() {
        return offset;
    }

    public Map<String, CommittedStatus.Type> getStatusMap() {
        return Collections.unmodifiableMap(this.statusMap);
    }

    public void commitAsync(String messageId, CommittedStatus.Type status) {
        if (this.statusMap.putIfAbsent(messageId, status) == null) {
            this.commitCount.getAndIncrement();
        }
    }

    @Override
    public void commit() throws Exception {
        this.commitFunction.accept(key, this.offset);
    }

    public boolean isDone() {
        return (messageAmount <= commitCount.get() || (System.currentTimeMillis() - startedTime >= timeout));
    }

    @Override
    public T getKey() {
        return this.key;
    }

    @Override
    public String toString() {
        return "LocalOffsetCommitter{" +
                "offset=" + offset +
                ", commitCount=" + commitCount +
                ", messageAmount=" + messageAmount +
                '}';
    }

}
