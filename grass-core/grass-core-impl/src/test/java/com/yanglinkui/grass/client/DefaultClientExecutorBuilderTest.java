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

package com.yanglinkui.grass.client;

import com.yanglinkui.grass.GrassContext;
import static org.junit.jupiter.api.Assertions.*;

import com.yanglinkui.grass.registry.Registry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DefaultClientExecutorBuilderTest {

    @Test
    public void testBuild(@Mock GrassContext context, @Mock Registry registry) {
        DefaultClientExecutorBuilder builder = new DefaultClientExecutorBuilder(context, registry);
        builder.setLoadBalance(null);
        builder.setSpiRouter(null);

        assertNotNull(builder.build());
        assertNotNull(builder.build() instanceof ClientExecutor);
    }
}
