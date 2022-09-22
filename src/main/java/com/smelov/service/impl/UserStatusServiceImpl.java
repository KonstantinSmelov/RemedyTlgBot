package com.smelov.service.impl;

import com.smelov.model.Status;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class UserStatusServiceImpl implements com.smelov.service.UserStatusService {

    private final Map<Long, Status> userStatusMap = new HashMap<>();

    @Override
    public Map<Long, Status> getStatusMap() {
        return userStatusMap;
    }

    @Override
    public Status getCurrentStatus(Long userId) {
        Status status = userStatusMap.get(userId);
        if (status != null) {
            log.info("Получили статус {} для userId {}", status, userId);
        } else {
            log.info("Получили статус {} для userId {}", status, userId);
        }
        return status;
    }

    @Override
    public void setCurrentStatus(Long userId, Status status) {
        userStatusMap.put(userId, status);
        if (status != null) {
            log.info("Установили статус {} для userId {}", status, userId);
        } else {
            log.info("Установили статус {} для userId {}", status, userId);
        }
    }
}
