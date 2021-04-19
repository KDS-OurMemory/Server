package com.kds.ourmemory.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.TimeZone;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DateUtil {
    private static final DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
    private static final DateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public static Date currentDate() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        try {
            return dateFormat.parse(dateFormat.format(new Date(System.currentTimeMillis() + 9 * 1000 * 60 * 60)));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return new Date();
    }
    
    public static Date formatDate(Date date) {
        return Optional.ofNullable(date).map(d -> {
            try {
                return dateFormat.parse(dateFormat.format(d));
            } catch (ParseException e) {
                e.printStackTrace();
                log.warn(e.getMessage());
            }

            return date;
        }).orElse(date);
    }
    
    public static Date currentTime() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        try {
            return timeFormat.parse(timeFormat.format(new Date(System.currentTimeMillis() + 9 * 1000 * 60 * 60)));
        } catch (ParseException e) {
            e.printStackTrace();
            log.warn(e.getMessage());
        }

        return new Date();
    }

    public static Date formatTime(Date date) {
        return Optional.ofNullable(date).map(d -> {
            try {
                return timeFormat.parse(timeFormat.format(d));
            } catch (ParseException e) {
                e.printStackTrace();
                log.warn(e.getMessage());
            }

            return date;
        }).orElse(date);
    }
}
