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

package com.yanglinkui.grass.protocol.message;

import java.util.HashSet;

public class HeaderConstants {

    public static final String CONTENT_TYPE = "grass-content-type";
    public static final String CONTENT_ENCODING = "grass-content-encoding";
    public static final String MESSAGE_ID = "grass-message-id";
    public static final String REQUEST_ID = "grass-correlation-id";

    public static final String REMOTE_USER = "grass-remote-user";

    public static final String SERVICE_ID = "grass-service-id";
    public static final String ACTION = "grass-action";
    public static final String SERVICE_VERSION = "grass-service-version";

    public static final String ZONE = "grass-zone";
    public static final String REQUEST_TIME = "grass-request-time";

    public static final String RESPONSE_STATUS = "grass-response-status";
    public static final String INVOKED_TIME = "grass-invoked-time";
    public static final String RETURNED_TIME = "grass-returned-time";
    public static final String RESPONSE_INSTANCE = "grass-instance-id";

    public static final String PUBLISHER_APPLICATION = "grass-publisher-application";
    public static final String PUBLISHER_ZONE = "grass-publisher-zone";


    public static final String REPLY_TYPE = "grass-reply-type";
    public static final String REPLY_METADATA_PREFIX = "grass-reply-metadata-";
    public static final String REPLY_METADATA_HOST = REPLY_METADATA_PREFIX + "host";
    public static final String REPLY_METADATA_PORT = REPLY_METADATA_PREFIX + "port";
    public static final String REPLY_METADATA_TOPIC_ROUTING_KEY = REPLY_METADATA_PREFIX + "routing-key";
    public static final String REPLY_METADATA_TOPIC_NAME = REPLY_METADATA_PREFIX + "topic-name";


    public static final HashSet<String> RESERVED_KEYWORDS = new HashSet<>();

    static {
        RESERVED_KEYWORDS.add(CONTENT_TYPE);
        RESERVED_KEYWORDS.add(CONTENT_ENCODING);
        RESERVED_KEYWORDS.add(MESSAGE_ID);
        RESERVED_KEYWORDS.add(REQUEST_ID);
        RESERVED_KEYWORDS.add(REMOTE_USER);
        RESERVED_KEYWORDS.add(SERVICE_ID);
        RESERVED_KEYWORDS.add(ACTION);
        RESERVED_KEYWORDS.add(SERVICE_VERSION);
        RESERVED_KEYWORDS.add(ZONE);
        RESERVED_KEYWORDS.add(REQUEST_TIME);
        RESERVED_KEYWORDS.add(RESPONSE_STATUS);
        RESERVED_KEYWORDS.add(INVOKED_TIME);
        RESERVED_KEYWORDS.add(RETURNED_TIME);
        RESERVED_KEYWORDS.add(RESPONSE_INSTANCE);
        RESERVED_KEYWORDS.add(REPLY_TYPE);
        RESERVED_KEYWORDS.add(REPLY_METADATA_HOST);
        RESERVED_KEYWORDS.add(REPLY_METADATA_PORT);
        RESERVED_KEYWORDS.add(REPLY_METADATA_TOPIC_ROUTING_KEY);
        RESERVED_KEYWORDS.add(REPLY_METADATA_TOPIC_NAME);
        RESERVED_KEYWORDS.add(PUBLISHER_APPLICATION);
        RESERVED_KEYWORDS.add(PUBLISHER_ZONE);

    }
}
