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

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class OffsetCommitQueue<T> {
    static Logger logger = LoggerFactory.getLogger(OffsetCommitQueue.class);

    private final AtomicInteger count = new AtomicInteger(0);

    private transient Node head;

    private transient Node last;

    private final ReentrantLock takeLock = new ReentrantLock();

    private final ReentrantLock putLock = new ReentrantLock();

    private final T key;

    public OffsetCommitQueue(T key) {
        this.key = key;
        this.head = this.last = new Node(null);
    }

    public T getKey() {
        return this.key;
    }

    public void put(CommittedStatus entry){
        logger.debug("Add a committer: {}", entry);

        final ReentrantLock putLock = this.putLock;
        final AtomicInteger count = this.count;

        putLock.lock();

        try {
            this.last = this.last.next = new Node(entry);
            count.getAndIncrement();
        } finally {
            putLock.unlock();
        }
    }

    public CommittedStatus moveToLastDone() {
        final AtomicInteger count = this.count;
        if (count.get() == 0) {
            return null;
        }

        final ReentrantLock takeLock = this.takeLock;
        takeLock.lock();

        Node h = this.head;
        Node node = h.next;
        if (node == null) {
            return null;
        }

        try {
            do {
                //如果当前完成了，后面的也完成了，那么就递归检查，直到没有后面元素或者后面的元素没有完成
                if (node.item.isDone() && node.next != null && node.next.item.isDone()) {
                    h.next = h; //help gc
                    h = node;
                    h.item = null;
                    node = h.next;
                    count.getAndDecrement(); // -1
                } else {
                    break;
                }
            } while (true);

            this.head = h;
            return this.head.next.item;
        } finally {
            takeLock.unlock();
        }
    }

    public CommittedStatus poll() {
        final AtomicInteger count = this.count;
        if (count.get() == 0) {
            return null;
        }

        CommittedStatus element = null;

        final ReentrantLock takeLock = this.takeLock;
        takeLock.lock();
        if (count.get() > 0) {
            count.getAndDecrement();

            Node h = this.head;
            Node node = h.next;
            element = node.item;

            h.next = h; //help gc;
            this.head = node;
            this.head.item = null;
        }

        return element;
    }

    public int size() {
        return this.count.get();
    }

    static class Node {

        public Node(CommittedStatus item) {
            this.item = item;
        }

        CommittedStatus item;

        Node next;
    }
}
