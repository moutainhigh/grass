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

package com.yanglinkui.grass.common.logger;

import com.yanglinkui.grass.common.logger.slf4j.Slf4jFactory;

/**
 * 切换起来方便
 */
public abstract class LoggerFactory {

    private static volatile LoggerFactory INSTANCE = new Slf4jFactory();

    public static Logger getLogger(Class<?> type) {
        return INSTANCE.getLoggerImpl(type);
    }

    public static void setInstance(LoggerFactory factory) {
        INSTANCE = factory;
    }

    protected abstract Logger getLoggerImpl(Class<?> type);

}
