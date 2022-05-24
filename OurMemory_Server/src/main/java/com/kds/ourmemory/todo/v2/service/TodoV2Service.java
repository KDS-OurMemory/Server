package com.kds.ourmemory.todo.v2.service;

import com.kds.ourmemory.todo.v1.service.TodoService;
import com.kds.ourmemory.todo.v2.controller.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class TodoV2Service {

    private final TodoService todoService;

    public TodoInsertRspDto insert(TodoInsertReqDto reqDto) {
        return new TodoInsertRspDto(todoService.insert(reqDto.toDto()));
    }

    public TodoFindRspDto find(long todoId) {
        return new TodoFindRspDto(todoService.find(todoId));
    }

    public List<TodoFindRspDto> findTodos(long writerId) {
        return todoService.findTodos(writerId).stream().map(TodoFindRspDto::new).toList();
    }

    public TodoUpdateRspDto update(long todoId, TodoUpdateReqDto reqDto) {
        return new TodoUpdateRspDto(todoService.update(todoId, reqDto.toDto()));
    }

    public Void delete(long todoId) {
        todoService.delete(todoId);
        return null;
    }

}
