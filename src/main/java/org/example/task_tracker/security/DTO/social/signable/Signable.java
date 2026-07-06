package org.example.task_tracker.security.DTO.social.signable;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

public interface Signable {

    @JsonIgnore
    List<Object> getSignableFields();

    String getSignature();

}
