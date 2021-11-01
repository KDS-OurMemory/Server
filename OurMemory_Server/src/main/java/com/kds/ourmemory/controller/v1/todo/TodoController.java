package com.kds.ourmemory.controller.v1.todo;

import com.kds.ourmemory.controller.v1.ApiResult;
import com.kds.ourmemory.controller.v1.todo.dto.*;
import com.kds.ourmemory.service.v1.todo.TodoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.kds.ourmemory.controller.v1.ApiResult.ok;

@Api(tags = {"6. Todo"})
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/v1/todo")
public class TodoController {
    private final TodoService todoService;

    @ApiOperation(value = "TODO 추가", notes = "TODO 항목을 추가한다.")
    @PostMapping
    public ApiResult<InsertTodoDto.Response> insert(@RequestBody InsertTodoDto.Request request) {
        return ok(todoService.insert(request));
    }

    @ApiOperation(value = "TODO 단일 조회", notes = "TODO 항목을 조회한다. 삭제되었거나 없는 경우 예외를 발생시킨다.")
    @GetMapping("/{todoId}")
    public ApiResult<FindTodoDto.Response> find(@PathVariable long todoId) {
        return ok(todoService.find(todoId));
    }

    @ApiOperation(value = "TODO 리스트 조회", notes = "사용자가 작성한 TODO 리스트를 조회한다.")
    @GetMapping("/user/{userId}")
    public ApiResult<List<FindTodosDto.Response>> findTodos(@PathVariable long userId) {
        return ok(todoService.findTodos(userId));
    }

    @ApiOperation(value = "TODO 수정", notes = "전달받은 값이 있는 항목만 수정한다.")
    @PutMapping("/{todoId}")
    public ApiResult<UpdateTodoDto.Response> update(
            @PathVariable long todoId,
            @RequestBody UpdateTodoDto.Request request
    ) {
        return ok(todoService.update(todoId, request));
    }

    @ApiOperation(value = "TODO 삭제", notes = "TODO 삭제처리한다. used=false 처리")
    @DeleteMapping("/{todoId}")
    public ApiResult<DeleteTodoDto.Response> delete(@PathVariable long todoId) {
        return ok(todoService.delete(todoId));
    }
}
