package org.jlox;

public enum LoxConstants {
    INIT("init"),
    SELF("self"),
    SUPER("super"),
    THIS("this");

    private final String name;

    LoxConstants(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
