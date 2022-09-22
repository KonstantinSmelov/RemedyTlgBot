package com.smelov.service;

import java.sql.Date;
import java.util.Optional;

public interface DateService {
    Optional<Date> StrToDate(String inputDate);
}
