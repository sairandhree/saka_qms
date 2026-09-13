package com.saka.qms.model;

public interface Identifiable<ID> {
    ID getId();

    void setId(ID id);
}
