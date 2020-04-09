package com.yanglinkui.grass.zone;

public class RequestZoneLexer extends Lexer {

    RequestZoneLexer(String input) {
        super(input);
    }

    public Token nextToken() {
        while ( c!=EOF ) {
            switch ( c ) {
                case ',' : consume(); return new RequestZoneToken(RequestZoneToken.TOKEN_COMMA, ",");
                case '(' : consume(); return new RequestZoneToken(RequestZoneToken.TOKEN_LBRACK, "(");
                case ')' : consume(); return new RequestZoneToken(RequestZoneToken.TOKEN_RBRACK, ")");
                default:
                    if (isWS(c)) {
                        WS();
                        continue;
                    }

                    if (isNAME()) {
                        return NAME();
                    }

                    throw new Error("Invalid character: " + c);
            }
        }
        return new RequestZoneToken(RequestZoneToken.TOKEN_EOF, "<EOF>");
    }

    Token NAME() {
        StringBuilder buf = new StringBuilder();
        do {
            buf.append(c);
            consume();
        } while (isNAME());

        return new RequestZoneToken(RequestZoneToken.TOKEN_NAME, buf.toString());

    }

    boolean isNAME() {
        return isLETTER() || isNUMBER() || c == '-' || c == '_' || c == '.';
    }


    boolean isLETTER() {
        return c>='a'&&c<='z' || c>='A'&&c<='Z';
    }

    boolean isNUMBER() {
        return c>='0'&&c<='9';
    }

}
