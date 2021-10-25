package com.kds.ourmemory.controller.v1.todolist;

import com.kds.ourmemory.controller.v1.ApiResult;
import com.kds.ourmemory.service.v1.todolist.TodolistService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.kds.ourmemory.controller.v1.ApiResult.ok;

@Api(tags = {"6. Todolist"})
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/v1/todolist")
public class TodolistController {
    private final TodolistService todolistService;

    @ApiOperation(value = "알림 목록 조회", notes = "사용자 번호에 해당하는 알림 목록을 조회한다. 조회된 모든 알림은 읽음처리한다.")
    @GetMapping("/{userId}")
    public ApiResult<Object> insert(@RequestBody Object request) {
        return ok(todolistService.insert(request));
    }
}
