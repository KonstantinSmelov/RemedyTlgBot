package com.smelov.service.impl;

import com.smelov.service.DateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.Optional;

@Service
@Slf4j
public class DateServiceImpl implements DateService {

    @Override
    public Optional<Date> StrToDate(String inputDate) {

        log.debug("----> вход в StrToDate()");
        log.trace("StrToDate(): получена строка: {}", inputDate);
        Optional<Date> output = Optional.empty();

        String regEx1 = "^\\d{4}\\s0[1-9]$";
        String regEx2 = "^\\d{4}\\s1[0-2]$";
        String regEx3 = "^\\d{4}\\s[1-9]$";
        String regEx4 = "^\\d{2}\\s0[1-9]$";
        String regEx5 = "^\\d{2}\\s1[0-2]$";
        String regEx6 = "^\\d{2}\\s[1-9]$";

        if (inputDate.matches(regEx1)
                || inputDate.matches(regEx2)
                || inputDate.matches(regEx3)
                || inputDate.matches(regEx4)
                || inputDate.matches(regEx5)
                || inputDate.matches(regEx6)) {

            StringBuilder year = new StringBuilder();
            StringBuilder month = new StringBuilder();
            boolean isYear = true;

            for (int x = 0; x < inputDate.length(); x++) {
                if (inputDate.charAt(x) == ' ') {
                    isYear = false;
                    continue;
                }
                if (isYear) {
                    year.append(inputDate.charAt(x));
                } else {
                    month.append(inputDate.charAt(x));
                }
            }

            if (year.length() == 2) {
                year.insert(0, "20");
            }

            log.trace("StrToDate(): распарсено: Год: {}, месяц: {}", year, month);
            output = Optional.ofNullable(Date.valueOf(year.toString() + "-" + month.toString() + "-" + "01"));

        }

        log.debug("<---- выход из StrToDate(): {}", output.get());
        return output;
    }
}
