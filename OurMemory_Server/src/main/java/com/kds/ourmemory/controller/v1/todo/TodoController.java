package com.kds.ourmemory.controller.v1.todo;

import com.kds.ourmemory.controller.v1.ApiResult;
import com.kds.ourmemory.controller.v1.todo.dto.TodoReqDto;
import com.kds.ourmemory.controller.v1.todo.dto.TodoRspDto;
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
    public ApiResult<TodoRspDto> insert(@RequestBody TodoReqDto reqDto) {
        return ok(todoService.insert(reqDto));
    }

    @ApiOperation(value = "TODO 단일 조회", notes = "TODO 항목을 조회한다. 삭제되었거나 없는 경우 예외를 발생시킨다.")
    @GetMapping("/{todoId}")
    public ApiResult<TodoRspDto> find(@PathVariable long todoId) {
        return ok(todoService.find(todoId));
    }

    @ApiOperation(value = "TODO 목록 조회", notes = "사용자가 작성한 TODO 목록을 조회한다.")
    @GetMapping("/user/{userId}")
    public ApiResult<List<TodoRspDto>> findTodos(@PathVariable long userId) {
        return ok(todoService.findTodos(userId));
    }

    @ApiOperation(value = "TODO 수정", notes = "전달받은 값이 있는 항목만 수정한다.")
    @PutMapping("/{todoId}")
    public ApiResult<TodoRspDto> update(
            @PathVariable long todoId,
            @RequestBody TodoReqDto reqDto
    ) {
        return ok(todoService.update(todoId, reqDto));
    }

    @ApiOperation(value = "TODO 삭제", notes = "성공한 경우, 삭제 여부를 resultCode 로 전달하기 때문에 response=null 을 리턴한다.")
    @DeleteMapping("/{todoId}")
    public ApiResult<TodoRspDto> delete(@PathVariable long todoId) {
        return ok(todoService.delete(todoId));
    }
}
