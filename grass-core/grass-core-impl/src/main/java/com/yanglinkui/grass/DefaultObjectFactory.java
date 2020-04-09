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
import com.yanglinkui.grass.exception.NoUniqueException;

import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class DefaultObjectFactory implements ObjectFactory {

    protected final Map<Class, Map<String, Object>> instancesOfClass = new ConcurrentHashMap<>(128, 0.75F);

    protected final Map<String, Object> instances = new ConcurrentHashMap<>(128, 0.75F);

    protected final List<ObjectFactory> objectFactoryList;

    public DefaultObjectFactory(List<ObjectFactory> objectFactoryList) {
        this.objectFactoryList = objectFactoryList;
    }

    @Override
    public <T> T getInstance(Class<?> clazz, String id) {
        if (clazz == null) {
            throw new IllegalArgumentException("clazz cannot be null");
        }

        if (Utils.isEmpty(id)) {
            throw new IllegalArgumentException("id cannot be null");
        }

        T obj = null;

        for (ObjectFactory factory : this.objectFactoryList) {
            obj = factory.getInstance(clazz, id);
            if (obj != null) {
                return obj;
            }
        }

        //直接查id就行了
        if (clazz.equals(Object.class) || clazz.equals(Type.class)) {
            return getInstance(id);
        }

        Map<String, Object> instanceList = this.instancesOfClass.get(clazz);
        if (instanceList == null) {
            return null;
        }

        return (T) instanceList.get(id);
    }

    public <T> T getInstance(Class<?> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("clazz cannot be null");
        }

        T obj = null;

        for (ObjectFactory factory : this.objectFactoryList) {
            obj = factory.getInstance(clazz);
            if (obj != null) {
                return obj;
            }
        }

        Map<String, Object> instanceList = this.instancesOfClass.get(clazz);
        if (instanceList == null) {
            return null;
        }

        //避免变化
        List<Object> objectList = instanceList.values().stream().collect(Collectors.toList());

        if (objectList.size() > 1) {
            throw new NoUniqueException("The instance of " + clazz.getCanonicalName() + " is more than one");
        }

        return (T) objectList.get(0);
    }

    public <T> T getInstance(String id) {
        if (id == null) {
            throw new IllegalArgumentException("clazz cannot be null");
        }

        T obj = null;

        for (ObjectFactory factory : this.objectFactoryList) {
            obj = factory.getInstance(id);
            if (obj != null) {
                return obj;
            }
        }


        return (T) this.instances.get(id);
    }

    public void addInstance(String id, Object obj) {
        if (obj == null) {
            throw new IllegalArgumentException("obj cannot be null");
        }

        if (Utils.isEmpty(id)) {
            throw new IllegalArgumentException("id cannot be null");
        }

        if (this.getInstance(id) != null) {
            throw new DuplicateException("The id(" + id + ") is duplicate.");
        }

        //重复就报错
        Object value = this.instances.putIfAbsent(id, obj);
        if (value != null) {
            throw new DuplicateException("The id(" + id + ") is duplicate.");
        }

        Class<?> clazz = obj.getClass();
        put(clazz, id, obj);
    }

    void put(Class<?> clazz, String id, Object obj) {
        //Object.class和Type.class没有意义，直接查id就行了
        while (clazz != null && !Object.class.equals(clazz) && !Type.class.equals(clazz)) {

            //如果对象很多，会产生很多垃圾(浪费空间)，哈哈
            Map<String, Object> map = instancesOfClass.putIfAbsent(clazz, new ConcurrentHashMap<String, Object>());
            if (map == null) {
                map = instancesOfClass.get(clazz);
            }

            //前面的方法检查过是否会重复了
            map.put(id, obj);

            Class<?>[] interfaces = clazz.getInterfaces();
            if (interfaces != null) {
                for (Class<?> interfaceClazz : interfaces) {
                    put(interfaceClazz, id, obj);
                }
            }

            clazz = clazz.getSuperclass();

        }
    }

    public List getInstanceList() {
        List list = new LinkedList();
        this.instances.entrySet().forEach(e -> list.add(e.getValue()));

        return list;
    }
}
