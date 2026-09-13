package com.saka.qms.model;

import java.time.LocalDateTime;

public interface Auditable {
    String getModifiedBy();

    void setModifiedBy(String modifiedBy);

    LocalDateTime getModifiedOn();

    void setModifiedOn(LocalDateTime modifiedOn);

    String getAuthorisedBy();

    void setAuthorisedBy(String authorisedBy);
}
