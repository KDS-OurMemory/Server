package com.kds.ourmemory.controller.v1.memo;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.kds.ourmemory.dto.memo.AddMemoRequest;
import com.kds.ourmemory.dto.memo.AddMemoResponse;
import com.kds.ourmemory.service.v1.memo.MemoService;

@RestController(value = "/Memo")
public class MemoController {
    
    private MemoService memoService;
    
    public MemoController(MemoService memoService) {
        this.memoService = memoService;
    }

    @PostMapping("/AddMemo")
    public AddMemoResponse addMemo(@RequestBody AddMemoRequest request) {
        return memoService.addMemo(request);
    }
}
