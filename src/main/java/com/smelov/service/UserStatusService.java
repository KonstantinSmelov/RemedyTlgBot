package com.smelov.service;

import com.smelov.model.Status;

import java.util.Map;

public interface UserStatusService {
    Map<Long, Status> getUserStatusMap();
    Status getCurrentStatus(Long userId);
    void setCurrentStatus(Long userId, Status status);
}
