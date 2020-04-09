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

import com.yanglinkui.grass.AbstractRepository;
import com.yanglinkui.grass.DefaultObjectFactory;

import java.util.Collections;
import java.util.List;

public class DefaultClientRepository extends AbstractRepository<Client> implements ClientRepository {

    public final DefaultObjectFactory objectFactory = new DefaultObjectFactory(Collections.EMPTY_LIST);

    @Override
    protected void addToList(Client object) {
        this.objectFactory.addInstance(object.getId(), object);
    }

    @Override
    public List<Client> getList() {
        return (List<Client>) this.objectFactory.getInstanceList();
    }

    @Override
    public <T> T getInstance(Class<?> clazz, String id) {
        return this.objectFactory.getInstance(clazz, id);
    }

    @Override
    public <T> T getInstance(Class<?> clazz) {
        return this.objectFactory.getInstance(clazz);
    }

    @Override
    public <T> T getInstance(String id) {
        return this.objectFactory.getInstance(id);
    }
}
