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

package com.yanglinkui.grass.common.logger.slf4j;

import com.yanglinkui.grass.common.logger.Logger;

public class Slf4jLogger implements Logger {

    private final org.slf4j.Logger logger;

    public Slf4jLogger(org.slf4j.Logger logger) {
        this.logger = logger;
    }

    @Override
    public void debug(String msg, Object... args) {
        if (this.logger.isDebugEnabled()) {
            this.logger.debug(msg, args);
        }
    }

    @Override
    public void debug(String msg, Throwable e) {
        if (this.logger.isDebugEnabled()) {
            this.logger.debug(msg, e);
        }
    }

    @Override
    public void info(String msg, Object... args) {
        if (this.logger.isInfoEnabled()) {
            this.logger.info(msg, args);
        }
    }

    @Override
    public void info(String msg, Throwable e) {
        if (this.logger.isInfoEnabled()) {
            this.logger.info(msg, e);
        }
    }

    @Override
    public void warn(String msg, Object... args) {
        if (this.logger.isWarnEnabled()) {
            this.logger.warn(msg, args);
        }
    }

    @Override
    public void warn(String msg, Throwable e) {
        if (this.logger.isWarnEnabled()) {
            this.logger.warn(msg, e);
        }
    }

    @Override
    public void error(String msg, Object... args) {
        if (this.logger.isErrorEnabled()) {
            this.logger.error(msg, args);
        }
    }

    @Override
    public void error(String msg, Throwable e) {
        if (this.logger.isErrorEnabled()) {
            this.logger.error(msg, e);
        }
    }
}
