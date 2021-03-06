package com.yanglinkui.grass.zone;

public abstract class Lexer {

    public static final char EOF = (char) -1;

    protected final String input;
    protected int p = 0;
    protected char c;

    public Lexer(String input) {
        if (input == null || input.length() == 0) {
            throw new IllegalArgumentException("input cannot be null or zero length");
        }

        this.input = input;
        c = input.charAt(p);
    }

    protected void consume() {
        p++;
        if (p >= input.length()) c = EOF;
        else c = input.charAt(p);
    }

    protected void match(char x) {
        if (c == x) {
            consume();
        } else {
            throw new Error("Expecting " + x + "; found " + c);
        }
    }

    protected abstract Token nextToken();

    protected void WS() {
        while (isWS(c)) {
            consume();
        }
    }

    protected boolean isWS(char c) {
        return (c==' ' || c=='\t' || c=='\n' || c=='\r');
    }

}

