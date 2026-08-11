package com.employee.employee_management.util;

import java.time.LocalTime;

public final class Constants {

    private Constants() {
    }

    public static final String DEFAULT_USER_PASSWORD = "Default@123";
    public static final String EMPLOYEE_CODE_PREFIX = "EMP";
    public static final int EMPLOYEE_CODE_PADDING = 4;
    public static final int STANDARD_WORK_HOURS = 8;
    public static final LocalTime LATE_THRESHOLD = LocalTime.of(9, 30);
    public static final int HALF_DAY_HOURS = 4;
    public static final int MINUTES_PER_DAY = 1440;
}
