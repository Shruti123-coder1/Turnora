package com.turnora.model.enums;

public enum PriorityType {
    NORMAL(1),
    SENIOR_CITIZEN(5),
    DIFFERENTLY_ABLED(5),
    URGENT(10);

    private final int weight;

    PriorityType(int weight) {
        this.weight = weight;
    }

    public int getWeight() {
        return weight;
    }
}