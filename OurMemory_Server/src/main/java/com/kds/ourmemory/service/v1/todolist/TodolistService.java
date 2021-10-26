package com.kds.ourmemory.service.v1.todolist;

import com.kds.ourmemory.advice.v1.todolist.exception.TodolistInternalServerException;
import com.kds.ourmemory.advice.v1.user.exception.UserNotFoundException;
import com.kds.ourmemory.controller.v1.todolist.dto.InsertTodolistDto;
import com.kds.ourmemory.entity.todolist.Todolist;
import com.kds.ourmemory.entity.user.User;
import com.kds.ourmemory.repository.todolist.TodolistRepository;
import com.kds.ourmemory.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class TodolistService {
    private final TodolistRepository todolistRepository;

    // Add to work in todolist and user relationship tables
    private final UserRepository userRepo;

    private static final String INSERT_FAILED_MESSAGE = "%s '%s' insert failed";

    private static final String NOT_FOUND_MESSAGE = "Not found %s matched id: %d";

    private static final String TODOLIST = "todolist";

    private static final String USER = "user";

    public InsertTodolistDto.Response insert(InsertTodolistDto.Request request) {
        return findUser(request.getWriter())
                .map(writer -> {
                    var todolist = Todolist.builder()
                            .writer(writer)
                            .contents(request.getContents())
                            .todoDate(request.getTodoDate())
                            .used(true)
                            .build();

                    return insertTodolist(todolist)
                            .orElseThrow(() -> new TodolistInternalServerException(
                                    String.format(INSERT_FAILED_MESSAGE, TODOLIST, request.getContents())
                            ));
                })
                .map(InsertTodolistDto.Response::new)
                .orElseThrow(() -> new UserNotFoundException(
                        String.format(NOT_FOUND_MESSAGE, USER, request.getWriter())
                ));
    }

    /**
     * Todolist Repository
     */
    private Optional<Todolist> insertTodolist(Todolist todolist) {
        return Optional.of(todolistRepository.save(todolist));
    }

    /**
     * User Repository
     *
     * When working with a service code, the service code is connected to each other
     * and is caught in an infinite loop in the injection of dependencies.
     */
    private Optional<User> findUser(Long id) {
        return Optional.ofNullable(id).flatMap(userId -> userRepo.findById(userId).filter(User::isUsed));
    }
}
