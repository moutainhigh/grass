package com.yanglinkui.grass.zone;

public class RequestZoneToken extends Token {

    public static final char EOF = (char) -1;

    public static final int TOKEN_EOF = 1;

    public static final int TOKEN_NAME = 2;
    public static final int TOKEN_LBRACK = 3;
    public static final int TOKEN_RBRACK = 4;
    public static final int TOKEN_COMMA = 5;

    public static final String[] TOKEN_NAMES =
            {"n/a", "<EOF>", "NAME", "LBRACK(()", "RBRACK())", "COMMA(,)"};


    public RequestZoneToken(int type, String text) {
        super(type, TOKEN_NAMES[type], text);
    }


    public static String getTokenName(int type) {
        return TOKEN_NAMES[type];
    }
}
