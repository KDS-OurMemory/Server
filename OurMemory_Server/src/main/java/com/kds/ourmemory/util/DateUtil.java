package com.kds.ourmemory.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.lang3.time.DateUtils;

public class DateUtil {
    private DateUtil() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
        Locale.setDefault(Locale.KOREA);
    }
    
    public static String currentDate() {
        return new SimpleDateFormat("yyyyMMdd").format(DateUtils.addHours(new Date(), 9));
    }
    
    public static Date currentTime(){
        
        try {
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            
            return format.parse(format.format(new Date()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        
        return new Date();
    }
}
