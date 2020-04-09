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

package com.yanglinkui.grass.protocol;

import com.yanglinkui.grass.mock.MockProtocolFactory;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class DefaultProtocolFactoryManagerTest {

    @Test
    public void testGet_Add_RemoveProtocolFactory() {
        DefaultProtocolFactoryManager factoryManager = new DefaultProtocolFactoryManager();
        factoryManager.addProtocolFactory(new MockProtocolFactory(null));

        assertNotNull(factoryManager.getProtocolFactory(MockProtocolFactory.ID));
        assertEquals(MockProtocolFactory.class, factoryManager.getProtocolFactory(MockProtocolFactory.ID).getClass());

        assertNull(factoryManager.getProtocolFactory(null));

        factoryManager.removeProtocolFactory(MockProtocolFactory.ID);
        assertNull(factoryManager.getProtocolFactory(MockProtocolFactory.ID));
    }
}
