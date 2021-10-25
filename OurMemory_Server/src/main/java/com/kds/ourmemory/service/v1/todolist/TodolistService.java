package com.kds.ourmemory.service.v1.todolist;

import com.kds.ourmemory.repository.todolist.TodolistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class TodolistService {
    private final TodolistRepository todolistRepository;

    public Object insert(Object request) {
        // TODO: Req, Rsp 정의, 로직 정의
        return true;
    }
}
