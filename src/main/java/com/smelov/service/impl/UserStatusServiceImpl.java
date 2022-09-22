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
        log.info("----> вход в getCurrentStatus() <----");
        Status status = userStatusMap.get(userId);
//        log.debug("Получили статус {} для userId {}", status, userId);
        log.info("<---- выход из getCurrentStatus() ---->");
        return status;
    }

    @Override
    public void setCurrentStatus(Long userId, Status status) {
        log.info("----> вход в setCurrentStatus() <----");
        userStatusMap.put(userId, status);
//        log.debug("Установили статус {} для userId {}", status, userId);
        log.info("<---- выход из setCurrentStatus() ---->");
    }
}
