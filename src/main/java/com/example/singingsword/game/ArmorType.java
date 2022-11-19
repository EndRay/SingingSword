package com.example.singingsword.game;

public enum ArmorType {
    NONE,
    TOP,
    BOTTOM,
    BOTH,
    STRONG_TOP,
    STRONG_BOTTOM,
    STRONG_BOTH,
    ;

    public boolean isStrong() {
        return this == STRONG_TOP || this == STRONG_BOTTOM || this == STRONG_BOTH;
    }

    public boolean isTop() {
        return this == TOP || this == BOTH || this == STRONG_TOP || this == STRONG_BOTH;
    }

    public boolean isBottom() {
        return this == BOTTOM || this == BOTH || this == STRONG_BOTTOM || this == STRONG_BOTH;
    }

    public ArmorType strong(){
        return switch (this) {
            case NONE -> NONE;
            case TOP, STRONG_TOP -> STRONG_TOP;
            case BOTTOM, STRONG_BOTTOM -> STRONG_BOTTOM;
            case BOTH, STRONG_BOTH -> STRONG_BOTH;
        };
    }
}
