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

import com.yanglinkui.grass.common.Utils;
import com.yanglinkui.grass.exception.DuplicateException;
import com.yanglinkui.grass.exception.GrassException;
import com.yanglinkui.grass.exception.NotFoundException;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractRepository<T extends InvokerOwner> implements Repository<T> {

    //<serviceId, <action, <version, invoker>>>
    private final Map<String, Map<String, Map<String, Invoker>>> invokerList = new ConcurrentHashMap<>(128, 0.75F);

    @Override
    public Invoker getInvoker(String id, String action, String version) throws NotFoundException {
        Map<String, Map<String, Invoker>> actionInvokerList = this.invokerList.get(id);
        if (actionInvokerList == null) {
            throw newGrassNotFoundException("Not found invoker: " + id);
        }

        action = (action == null ? "" : action);
        Map<String, Invoker> versionInvokerList = actionInvokerList.get(action);
        if (versionInvokerList == null) {
            throw newGrassNotFoundException("Not found action(" + action + ") of invoker: " + id);
        }

        version = (version == null ? "" : version);
        Invoker invoker = versionInvokerList.get(version);

        //如果是不指定版本，先搜索version = ""的， 如果没有找到，随便挑选一个
        if (Utils.isEmpty(version)) {
            invoker = versionInvokerList.values().iterator().next();
        };

        if (invoker == null) {
            throw newGrassNotFoundException("Not found action(" + action + ")'s version(" + version + ") of invoker: " + id);
        }

        return invoker;
    }

    public synchronized void add(T object) {
        if (object == null) {
            return;
        }

        object.getInvokerList().stream().forEach(i -> {
            InvokerAttributes attributes = i.getAttributes();
            Map<String, Map<String, Invoker>> actionList = invokerList.putIfAbsent(attributes.getId(), new ConcurrentHashMap<>());
            if (actionList == null) {
                actionList = invokerList.get(attributes.getId());
            }

            Map<String, Invoker> versionList = actionList.putIfAbsent(attributes.getAction(), new ConcurrentHashMap<>());
            if (versionList == null) {
                versionList = actionList.get(attributes.getAction());
            }

            if (versionList.putIfAbsent(attributes.getVersion(), i) != null) {
                throw new DuplicateException(object.getClass() + "[id: " + attributes.getId()
                        + ", action: " + attributes.getAction()
                        + ", version: " + attributes.getVersion() + "]");
            }
        });

        this.addToList(object);
    }

    protected abstract void addToList(T object);

    GrassException newGrassNotFoundException(String message) {
        GrassException exception = new NotFoundException(message);
        exception.setFillInStackTrace(false);
        return exception;
    }
}
