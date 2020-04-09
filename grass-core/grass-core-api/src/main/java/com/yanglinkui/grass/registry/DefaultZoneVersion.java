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

package com.yanglinkui.grass.registry;

import com.yanglinkui.grass.ZoneVersion;

import java.util.Objects;

public class DefaultZoneVersion implements ZoneVersion {

    private Integer id;

    private Integer prevVersionId;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getPrevVersionId() {
        return prevVersionId;
    }

    public void setPrevVersionId(Integer prevVersionId) {
        this.prevVersionId = prevVersionId;
    }

    @Override
    public boolean before(ZoneVersion version) {
        if (version == null) {
            throw new IllegalArgumentException("version cannot be null");
        }
        return id.compareTo(version.getId()) < 0;
    }

    @Override
    public boolean after(ZoneVersion version) {
        if (version == null) {
            throw new IllegalArgumentException("version cannot be null");
        }
        return id.compareTo(version.getId()) > 0;
    }

    @Override
    public boolean equals(ZoneVersion version) {
        return equals(version);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !ZoneVersion.class.isAssignableFrom(o.getClass())) return false;
        ZoneVersion that = (ZoneVersion) o;
        return id.compareTo(that.getId()) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, prevVersionId);
    }

}
