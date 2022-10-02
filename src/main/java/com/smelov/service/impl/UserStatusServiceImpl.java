package com.smelov.service.impl;

import com.smelov.entity.Medicine;
import com.smelov.model.AddStatus;
import com.smelov.model.EditStatus;
import com.smelov.model.Status;
import com.smelov.service.UserStatusService;
import lombok.extern.slf4j.Slf4j;
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
        userStatusMap.put(null, Status.NONE.setMedicine(new Medicine()));
        log.info("----> вход в getCurrentStatus() <----");
        Status status = userStatusMap.get(userId);
        if(status == null) {
            status = userStatusMap.get(null);
        }
//        log.debug("Получили статус {} для userId {}", status, userId);
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
        log.info("status на входе: {}", status);
        status = Status.NONE.setEditStatus(EditStatus.NONE).setAddStatus(AddStatus.NONE).setMedicine(new Medicine());
        userStatusMap.put(userId, status);
        log.info("status на выходе: {}", status);
        log.info("<---- выход из resetStatus() ---->");
    }
}
