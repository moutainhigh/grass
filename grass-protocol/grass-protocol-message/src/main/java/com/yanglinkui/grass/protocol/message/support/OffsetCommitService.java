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

import com.yanglinkui.grass.common.logger.Logger;
import com.yanglinkui.grass.common.logger.LoggerFactory;
import com.yanglinkui.grass.common.thread.NamedThreadFactory;
import com.yanglinkui.grass.common.timer.HashedWheelTimer;
import com.yanglinkui.grass.common.timer.Timeout;
import com.yanglinkui.grass.common.timer.TimerTask;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class OffsetCommitService<T> {

    static Logger logger = LoggerFactory.getLogger(OffsetCommitService.class);

    private final Map<T/* Partition or MessageQueue */, OffsetCommitQueue> offsetCommitQueueMap;

    public OffsetCommitService() {
        this.offsetCommitQueueMap = new ConcurrentHashMap<>();
    }

    public OffsetCommitService(int poolSize) {
        this.offsetCommitQueueMap = new ConcurrentHashMap<>(poolSize, 0.75F);
    }

    public void add(CommittedStatus<T> entry) {
        T key = entry.getKey();
        OffsetCommitQueue queue = this.offsetCommitQueueMap.get(key);
        if (queue == null) {
            queue = new OffsetCommitQueue(key);
            if (this.offsetCommitQueueMap.putIfAbsent(key, queue) == null) {
                logger.info("Start a time task for committing offset: {}", key);
                Timer.newTimeout(
                        new CommitTask(this.offsetCommitQueueMap.get(key)),
                        TASK_DELAY, TimeUnit.MILLISECONDS
                );
            }
        }

        this.offsetCommitQueueMap.get(key).put(entry);
    }


    public static long TASK_DELAY = 10_000;

    class CommitTask implements TimerTask {

        private final OffsetCommitQueue queue;

        CommitTask(OffsetCommitQueue queue) {
            this.queue = queue;
        }

        @Override
        public void run(Timeout timeout) throws Exception {
            CommittedStatus status = this.queue.moveToLastDone();
            if (status != null) {
                try {
                    status.commit();
                    this.queue.poll();
                } catch (Throwable ex) {
                    logger.error("Failed to flush offset", ex);
                }
            }

            Timer.newTimeout(this, TASK_DELAY, TimeUnit.MILLISECONDS);
        }
    }
}
