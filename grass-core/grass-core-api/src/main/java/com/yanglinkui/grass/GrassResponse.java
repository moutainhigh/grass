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

package com.yanglinkui.grass;

public interface GrassResponse {

    public static final int SUCCESS = 0;

    public static final int INTERNAL_ERROR = 503;

    public static final int NOT_FOUND = 404;

    public String getCorrelationId();

    public String getContentType();

    public String getAction();

    public String getVersion();

    public String getApplicationName();

    public String getInstanceId();

    public String getZone();

    public String getServiceId();

    public String[] getHeaderNames();

    public String getHeader(String key);

    public void setHeader(String key, String value);

    public void removeHeader(String key);

    public int getStatus();

    public <T> T getBody();

    public Long getInvokedTime();

    public Long getReturnedTime();

    public RawInputStream getInputStream();

}
