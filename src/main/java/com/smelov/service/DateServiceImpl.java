package com.smelov.service;

import java.sql.Date;
import java.util.Optional;

public interface DateServiceImpl {
    Optional<Date> StrToDate(String inputDate);
}
