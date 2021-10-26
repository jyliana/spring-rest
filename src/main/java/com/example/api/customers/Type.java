package com.example.api.customers;

public enum Type {
    PERSON, COMPANY;

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
