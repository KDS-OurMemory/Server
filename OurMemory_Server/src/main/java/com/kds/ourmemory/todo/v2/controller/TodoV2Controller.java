package com.kds.ourmemory.todo.v2.controller;

import com.kds.ourmemory.common.v1.controller.ApiResult;
import com.kds.ourmemory.todo.v2.controller.dto.*;
import com.kds.ourmemory.todo.v2.service.TodoV2Service;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.kds.ourmemory.common.v1.controller.ApiResult.ok;

@Api(tags = {"6-2. Todo V2"})
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/v2/todos")
public class TodoV2Controller {

    private final TodoV2Service todoV2Service;

    @ApiOperation(value = "할일 생성", notes = "전달받은 날짜의 할일을 추가한다.")
    @PostMapping
    public ApiResult<TodoInsertRspDto> insert(@RequestBody TodoInsertReqDto reqDto) {
        return ok(todoV2Service.insert(reqDto));
    }

    @ApiOperation(value = "할일 단일 조회", notes = "할일을 조회한다. id 값으로 정확히 조회하기 때문에 조회되지 않는 경우 예외를 발생시킨다.")
    @GetMapping("/{todoId}")
    public ApiResult<TodoFindRspDto> find(@PathVariable long todoId) {
        return ok(todoV2Service.find(todoId));
    }

    @ApiOperation(value = "할일 목록 조회", notes = "사용자가 작성한 할일 목록을 조회한다.")
    @GetMapping("/writer/{userId}")
    public ApiResult<List<TodoFindRspDto>> findTodos(@PathVariable long userId) {
        return ok(todoV2Service.findTodos(userId));
    }

    @ApiOperation(value = "할일 수정", notes = "전달받은 값이 있는 항목만 수정한다. 데이터 수정날짜는 항상 갱신된다.")
    @PutMapping("/{todoId}")
    public ApiResult<TodoUpdateRspDto> update(
            @PathVariable long todoId,
            @RequestBody TodoUpdateReqDto reqDto
    ) {
        return ok(todoV2Service.update(todoId, reqDto));
    }

    @ApiOperation(value = "할일 삭제", notes = "성공한 경우, 삭제 여부를 resultCode 로 전달하기 때문에 response=null 을 리턴한다.")
    @DeleteMapping("/{todoId}")
    public ApiResult<Void> delete(@PathVariable long todoId) {
        return ok(todoV2Service.delete(todoId));
    }

}
