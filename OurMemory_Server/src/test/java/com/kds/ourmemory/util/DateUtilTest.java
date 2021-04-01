package com.kds.ourmemory.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;

import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class DateUtilTest {
    @Test
    void 시간_계산() {
        log.info(DateUtil.currentTime().toString());
    }
    
    @Test
    void 날짜_계산() {
        String currentDate = DateUtil.currentDate();
        assertThat(currentDate).isEqualTo("20210401");
        
        log.info(currentDate);
    }

    @Test
    void 날짜_포맷_변환() {
        log.info(DateUtil.formatDate(new Date()));
    }
    
    @Test
    void 시간_포맷_변환() {
        log.info(DateUtil.formatTime(new Date()).toString());
    }
}
