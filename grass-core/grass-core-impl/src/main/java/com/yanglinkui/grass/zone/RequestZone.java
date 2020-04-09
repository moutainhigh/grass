package com.yanglinkui.grass.zone;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class RequestZone {

    public final static RequestZone DEFAULT_REQUEST_ZONE = new RequestZone(Collections.EMPTY_LIST, Collections.EMPTY_MAP, "");

    private final List<Zone> zoneList;

    private final Map<String, String> applicationAndZoneRelationshipList;

    private final String value;

    RequestZone(List<Zone> zoneList, Map<String, String> applicationAndZoneRelationshipList,  String value) {
        this.zoneList = zoneList;
        this.applicationAndZoneRelationshipList = applicationAndZoneRelationshipList;
        this.value = value;
    }

    public List<Zone> getZoneList() {
        return this.zoneList;
    }

    public String getValue() {
        return this.value;
    }

    public String getZone(String applicationName) {
        return this.applicationAndZoneRelationshipList.get(applicationName);
    }

    public boolean contains(String applicationName) {
        return this.applicationAndZoneRelationshipList.get(applicationName) != null;
    }

    public static class Zone {

        private final String id;

        private final int applicationSize;

        public Zone(String id, int applicationSize) {
            this.id = id;
            this.applicationSize = applicationSize;
        }

        public String getId() {
            return id;
        }

        public int getApplicationSize() {
            return applicationSize;
        }
    }




}
