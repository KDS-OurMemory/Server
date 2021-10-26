package com.kds.ourmemory.controller.v1.todolist;

import com.kds.ourmemory.controller.v1.ApiResult;
import com.kds.ourmemory.controller.v1.todolist.dto.InsertTodolistDto;
import com.kds.ourmemory.service.v1.todolist.TodolistService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import static com.kds.ourmemory.controller.v1.ApiResult.ok;

@Api(tags = {"6. Todolist"})
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/v1/todolist")
public class TodolistController {
    private final TodolistService todolistService;

    @ApiOperation(value = "TODO 리스트 추가", notes = "TODO 리스트에 항목을 추가한다.")
    @PostMapping
    public ApiResult<InsertTodolistDto.Response> insert(@RequestBody InsertTodolistDto.Request request) {
        return ok(todolistService.insert(request));
    }
}
