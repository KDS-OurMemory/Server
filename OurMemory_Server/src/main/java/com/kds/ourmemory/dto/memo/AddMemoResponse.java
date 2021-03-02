package com.kds.ourmemory.dto.memo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AddMemoResponse {
    private int result;
    private String addTime;
}
