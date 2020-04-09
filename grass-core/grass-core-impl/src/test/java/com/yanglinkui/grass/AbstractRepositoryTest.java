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

import com.yanglinkui.grass.exception.DuplicateException;
import com.yanglinkui.grass.exception.NotFoundException;
import com.yanglinkui.grass.mock.MockService;
import com.yanglinkui.grass.provider.Service;
import com.yanglinkui.grass.provider.ServiceBuilder;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class AbstractRepositoryTest {

    Service<MockService> service;
    @BeforeEach
    public void init() {
        ServiceBuilder<MockService> builder = new ServiceBuilder<>();
        builder.setHandler(new MockService());
        service = builder.build();
    }

    @Test
    public void testAdd() {
        AbstractRepository<Service> repository = new MockAbstractRepository<>();
        repository.add(service);

        DuplicateException de = assertThrows(DuplicateException.class, () -> repository.add(service));
    }

    @Test
    public void testGetInvoker() {
        AbstractRepository<Service> repository = new MockAbstractRepository<>();
        repository.add(service);

        Invoker invoker = repository.getInvoker("service.defaultMethod", null, null);
        assertNotNull(invoker);
        assertEquals("service.defaultMethod", invoker.getAttributes().getId());
        assertEquals("", invoker.getAttributes().getAction());
        assertEquals("", invoker.getAttributes().getVersion());

        invoker = repository.getInvoker("service.users-age", "get", "");
        assertNotNull(invoker);
        assertEquals(repository.getInvoker("service.users-age", "get", "1.0"), invoker);
    }

    @Test
    public void testGetInvoker_exception() {
        AbstractRepository<Service> repository = new MockAbstractRepository<>();
        repository.add(service);

        NotFoundException ne1 = assertThrows(NotFoundException.class, () -> repository.getInvoker("test", null, null));
        assertEquals("Not found invoker: test", ne1.getMessage());

        NotFoundException ne2 = assertThrows(NotFoundException.class, () -> repository.getInvoker("service.users-age", "test", null));
        assertEquals("Not found action(test) of invoker: service.users-age", ne2.getMessage());

        NotFoundException ne3 = assertThrows(NotFoundException.class, () -> repository.getInvoker("service.users-age", "get", "2.0"));
        assertEquals("Not found action(get)'s version(2.0) of invoker: service.users-age", ne3.getMessage());

        assertNotNull(repository.getInvoker("service.users-age", "get", "1.0"));
    }

    static class MockAbstractRepository<T extends InvokerOwner> extends AbstractRepository<T> {

        private List<T> list = new LinkedList();

        @Override
        protected void addToList(T object) {
            this.list.add(object);
        }

        @Override
        public List<T> getList() {
            return (List) this.list.stream().collect(Collectors.toList());
        }
    }
}
