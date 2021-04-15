package com.kds.ourmemory.controller.v1.firebase.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class FcmRequestDto {
    private String token;
    private String deviceOs;
    private String title;
    private String body;
    
}
