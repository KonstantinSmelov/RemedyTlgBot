package com.smelov.service.impl;

import com.smelov.entity.Medicine;
import com.smelov.model.AddStatus;
import com.smelov.model.EditStatus;
import com.smelov.model.MainStatus;
import com.smelov.model.Status;
import com.smelov.service.UserStatusService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class UserStatusServiceImpl implements UserStatusService {

    private final Map<Long, Status> userStatusMap = new HashMap<>();

    @Override
    public Map<Long, Status> getStatusMap() {
        return userStatusMap;
    }

    @Override
    public Status getCurrentStatus(Long userId) {
        log.info("----> вход в getCurrentStatus() <----");
        System.out.println("getCurrentStatus(): " + userStatusMap);
        Status status = Status.builder()
                .mainStatus(MainStatus.NONE)
                .addStatus(AddStatus.NONE)
                .editStatus(EditStatus.NONE)
                .medicine(new Medicine())
                .build();
        if (userStatusMap.containsKey(userId)) {
            status = userStatusMap.get(userId);
        }
        log.info("status: {}", status);
        log.info("<---- выход из getCurrentStatus() ---->");
        return status;
    }

    @Override
    public void setCurrentStatus(Long userId, Status status) {
        log.info("----> вход в setCurrentStatus() <----");
        userStatusMap.put(userId, status);
        log.info("status: {}", status);
        log.info("<---- выход из setCurrentStatus() ---->");
    }

    @Override
    public void resetStatus(Long userId) {
        log.info("----> вход в resetStatus() <----");
        Status status = userStatusMap.get(userId);
        log.info("status на входе для userId {}: {}", userId, status);
        status = Status.builder()
                .mainStatus(MainStatus.NONE)
                .addStatus(AddStatus.NONE)
                .editStatus(EditStatus.NONE)
                .medicine(new Medicine())
                .build();
        userStatusMap.put(userId, status);
        log.info("status на выходе для userId {}: {}", userId, status);
        log.info("<---- выход из resetStatus() ---->");
    }
}
