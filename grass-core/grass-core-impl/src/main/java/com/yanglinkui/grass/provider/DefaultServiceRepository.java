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

package com.yanglinkui.grass.provider;

import com.yanglinkui.grass.AbstractRepository;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class DefaultServiceRepository extends AbstractRepository<Service> implements ServiceRepository {
    private final List<Service> list = new LinkedList<>();

    @Override
    protected void addToList(Service object) {
        this.list.add(object);
    }

    @Override
    public List<Service> getList() {
        return this.list.stream().collect(Collectors.toList());
    }
}
