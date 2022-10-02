package com.smelov.service;

import com.smelov.model.Status;

import java.util.Map;

public interface UserStatusService {
    Map<Long, Status> getStatusMap();
    Status getCurrentStatus(Long userId);
    void setCurrentStatus(Long userId, Status status);
    void resetStatus(Long userId);
}
