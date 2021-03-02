package com.kds.ourmemory.service.v1.memo;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.stereotype.Service;

import com.kds.ourmemory.dto.memo.AddMemoRequest;
import com.kds.ourmemory.dto.memo.AddMemoResponse;

@Service
public class MemoService {
    
    public AddMemoResponse addMemo(AddMemoRequest request) {
        DateFormat format = new SimpleDateFormat("yyyyMMdd");
        String today = format.format(new Date());
        
        return new AddMemoResponse(1, today);
    }
}
