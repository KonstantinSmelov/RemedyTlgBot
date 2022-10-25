package com.smelov.service.impl;

import com.smelov.entity.Medicine;
import com.smelov.model.AddStatus;
import com.smelov.model.EditStatus;
import com.smelov.model.MainStatus;
import com.smelov.model.Status;
import com.smelov.service.UserStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserStatusServiceImpl implements UserStatusService {

    private final Map<Long, Status> userStatusMap = new HashMap<>();

    @Override
    public Map<Long, Status> getStatusMap() {
        log.debug("----> вход в getStatusMap()");
        log.debug("<---- выход из getStatusMap(): {}", userStatusMap);
        return userStatusMap;
    }

    @Override
    public Status getCurrentStatus(Long userId) {
        log.debug("----> вход в getCurrentStatus()");
        Status status;
        if (userStatusMap.containsKey(userId)) {
            status = userStatusMap.get(userId);
        } else {
            status = Status.builder()
                    .mainStatus(MainStatus.MAIN_MENU)
                    .addStatus(AddStatus.NONE)
                    .editStatus(EditStatus.NONE)
                    .medicine(new Medicine())
                    .comparator(Comparator.comparing(Medicine::getName))
                    .userMessageIds(new HashSet<>())
                    .build();
        }
        log.debug("<---- выход из getCurrentStatus(): {}: {}", userId, status);
        return status;
    }

    @Override
    public void changeCurrentStatus(Long userId, Status status) {
        log.debug("----> вход в changeCurrentStatus()");
        Status currentStatus = getCurrentStatus(userId);
        Status changedStatus = Status.builder()
                .mainStatus(status.getMainStatus() != null ? status.getMainStatus() : currentStatus.getMainStatus())
                .addStatus(status.getAddStatus() != null ? status.getAddStatus() : currentStatus.getAddStatus())
                .editStatus(status.getEditStatus() != null ? status.getEditStatus() : currentStatus.getEditStatus())
                .comparator(status.getComparator() != null ? status.getComparator() : currentStatus.getComparator())
                .medicine(status.getMedicine() != null ? status.getMedicine() : currentStatus.getMedicine())
                .userMessageIds(status.getUserMessageIds() != null ? status.getUserMessageIds() : currentStatus.getUserMessageIds())
                .build();
        userStatusMap.put(userId, changedStatus);
        log.debug("<---- выход из changeCurrentStatus() {}: {}", userId, status);
    }

    @Override
    public void resetStatus(Long chatId, Long userId) {
        log.debug("----> вход в resetStatus()");
        Status status = userStatusMap.get(userId);
        log.debug("----- status на входе {}: {}", userId, status);
        status = Status.builder()
                .mainStatus(MainStatus.MAIN_MENU)
                .addStatus(AddStatus.NONE)
                .editStatus(EditStatus.NONE)
                .comparator(Comparator.comparing(Medicine::getName))
                .medicine(new Medicine())
                .userMessageIds(new HashSet<>())
                .build();
        userStatusMap.put(userId, status);
        log.debug("<---- выход из resetStatus(): {}: {}", userId, status);
    }
}
