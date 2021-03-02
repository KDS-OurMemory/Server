package com.kds.ourmemory.dto.memo;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AddMemoRequest {
    private String name;
    private String contents;
    private String place;
    private Date startDate;
    private Date endDate;
    private Date firstAlarm;
    private Date secondAlarm;
    private String bgColor;
}
