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

import com.yanglinkui.grass.*;
import com.yanglinkui.grass.exception.InvokedException;
import com.yanglinkui.grass.registry.Registry;

import java.util.ArrayList;
import java.util.List;

public class DefaultRouter implements Router {

    private final Registry registry;

    public DefaultRouter(Registry registry) {
        this.registry = registry;
    }

    @Override
    public List<ApplicationInstance> choose(Chain chain) {
        GrassRequest request = chain.getRequest();
        String zoneName = request.getZone();
        final Zone zone = this.registry.getZone(zoneName);
        request = checkRequestZone(request, zone.getVersion(), zone.getName());

        List<ApplicationInstance> resultList = getApplicationInstanceList(request, zone);

        return chain.proceed(request, resultList);
    }

    GrassRequest checkRequestZone(GrassRequest request, final Integer zoneNewlyVersion, final String zoneName) {
        if (request.getZoneVersion() != null) {
            return request;
        }

        return new GrassRequestWrapper(request) {
            @Override
            public Integer getZoneVersion() {
                return zoneNewlyVersion;
            }

            @Override
            public String getZone() {
                return zoneName;
            }
        };
    }

    List<ApplicationInstance> getApplicationInstanceList(GrassRequest request, Zone zone) {
        Integer zoneVersionId = request.getZoneVersion();
        ZoneVersion version = this.registry.getZoneVersion(zone.getName(), zoneVersionId);

        return getApplicationInstanceByVersion(version, zone, request.getApplicationName());
    }

    List<ApplicationInstance> getApplicationInstanceListOfZone(Zone zone, String applicationName) {
        for (; ;) {
            List<ApplicationInstance> resultList = this.registry.getApplicationInstanceList(zone.getName(), applicationName);
            if (resultList != null && resultList.size() > 0) {
                return resultList;
            }

            zone = this.registry.getZone(zone.getParentZoneName()); //递归查找
            if (zone == null) {
                break;
            }
        }

        return null;
    }

//    List<ApplicationInstance> getActiveApplicationInstanceListByZone(Zone zone, String applicationName) {
//        List<ApplicationInstance> sourceList = getApplicationInstanceListOfZone(zone, applicationName);
//        if (sourceList == null || sourceList.size() == 0) {
//            throw new InvokedException("No application instances to application: " + applicationName);
//        }
//        List<ApplicationInstance> resultList = new ArrayList<>();
//        for (ApplicationInstance instance : sourceList) {
//            if (instance.isActive()) {
//                resultList.add(instance);
//            }
//        }
//
//        return resultList;
//    }

    List<ApplicationInstance> getApplicationInstanceByVersion(ZoneVersion version, Zone zone, String applicationName) {
        List<ApplicationInstance> sourceList = getApplicationInstanceListOfZone(zone, applicationName);
        if (sourceList == null || sourceList.size() == 0) {
            throw new InvokedException("No application instances to application: " + applicationName);
        }

        List<ApplicationInstance> resultList = new ArrayList<>();
        int index = 0;

        while (version != null) {
            for (; index < sourceList.size(); ) {
                ApplicationInstance instance = sourceList.get(index);
                if (!instance.isActive()) { //非活动服务要排除
                    index = index + 1;
                    continue;
                }

                //来自Parent-Zone的
                if (!zone.getName().equals(instance.getZone())) {
                    resultList.add(instance);
                    index = index + 1;
                    continue;
                }

                //新版本，就直接略过
                if (isBefore(version.getId(), instance.getZoneVersion())) {
                    index = index + 1;
                    continue;
                }

                //等于当前版本的就可以放到实例列表中
                if (isEqual(version.getId(), instance.getZoneVersion())) {
                    resultList.add(instance);
                    index = index + 1;
                    continue;
                }

                //当前版本没有找到
                if (isAfter(version.getId(), instance.getZoneVersion())) {
                    break;
                }
            }

            //往前追溯
            version = this.registry.getZoneVersion(zone.getName(), version.getPrevVersionId());
        }

        return resultList;
    }

    boolean isBefore(Integer source, Integer target) {
        return (source.compareTo(target)) < 0;
    }

    boolean isEqual(Integer source, Integer target) {
        return (source.compareTo(target)) == 0;
    }

    boolean isAfter(Integer source, Integer target) {
        return (source.compareTo(target)) > 0;
    }}
