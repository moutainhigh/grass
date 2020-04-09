package com.yanglinkui.grass.zone;

import java.util.*;

public class RequestZoneParser extends Parser {

    private Map<String, String> applicationAndZoneRelationshipList = new HashMap<>(32, 0.75F);

    private List<RequestZone.Zone> zoneList = new LinkedList<>();

    private String value;

    RequestZoneParser(String value) {
        super(new RequestZoneLexer(value), 1);
        this.value = value;
    }

    protected String getTokenName(int type) {
        return RequestZoneToken.getTokenName(type);
    }

    public RequestZone requestZone() {
        RequestZone.Zone zone = zone();
        zoneList.add(zone);

        while (LA(1) == RequestZoneToken.TOKEN_COMMA) {
            consume();
            zoneList.add(zone());
        }

        return new RequestZone(Collections.unmodifiableList(zoneList), applicationAndZoneRelationshipList, value);
    }

    RequestZone.Zone zone() {
        int size = 0;
        String id = match(RequestZoneToken.TOKEN_NAME).getText();

        if (LA(1) == RequestZoneToken.TOKEN_LBRACK) {
            consume();

            if (LA(1) == RequestZoneToken.TOKEN_NAME) {
                size++;
                applicationAndZoneRelationshipList.put(match(RequestZoneToken.TOKEN_NAME).getText(), id);

                while(LA(1) == RequestZoneToken.TOKEN_COMMA) {
                    consume();
                    applicationAndZoneRelationshipList.put(match(RequestZoneToken.TOKEN_NAME).getText(), id);

                    size++;
                }
            }

            match(RequestZoneToken.TOKEN_RBRACK);
        }

        return new RequestZone.Zone(id, size);
    }

    public static RequestZone parse(String input) {
        if (input == null || input.length() == 0) {
            return null;
        }

        return new RequestZoneParser(input).requestZone();
    }
}
