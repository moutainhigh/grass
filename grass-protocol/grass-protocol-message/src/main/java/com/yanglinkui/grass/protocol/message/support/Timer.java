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

import com.yanglinkui.grass.common.thread.NamedThreadFactory;
import com.yanglinkui.grass.common.timer.HashedWheelTimer;
import com.yanglinkui.grass.common.timer.TimerTask;

import java.util.concurrent.TimeUnit;

public class Timer {

    private static final HashedWheelTimer TIMER = new HashedWheelTimer(new NamedThreadFactory("Grass-Message-"));

    public static void newTimeout(TimerTask task, long delay, TimeUnit unit) {
        TIMER.newTimeout(task, delay, unit);
    }

    public static void start() {
        TIMER.start();
    }

    public static void stop() {
        TIMER.stop();
    }
}
