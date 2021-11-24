package com.kds.ourmemory.service.v1.todo;

import com.kds.ourmemory.advice.v1.todo.exception.TodoInternalServerException;
import com.kds.ourmemory.advice.v1.todo.exception.TodoNotFoundException;
import com.kds.ourmemory.advice.v1.user.exception.UserNotFoundException;
import com.kds.ourmemory.controller.v1.todo.dto.*;
import com.kds.ourmemory.entity.todo.Todo;
import com.kds.ourmemory.entity.user.User;
import com.kds.ourmemory.repository.todo.TodoRepository;
import com.kds.ourmemory.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.temporal.TemporalQueries;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
@Service
public class TodoService {
    private final TodoRepository todoRepository;

    // Add to work in todolist and user relationship tables
    private final UserRepository userRepo;

    private static final String QUERY_FAILED_MESSAGE = "%s table's '%s' data %s failed";

    private static final String NOT_FOUND_MESSAGE = "Not found %s matched id: %d";

    private static final String TODO = "todo";

    private static final String USER = "user";

    private static final String INSERT = "insert";

    private static final String UPDATE = "update";

    public TodoRspDto insert(TodoReqDto reqDto) {
        return findUser(reqDto.getWriterId())
                .map(writer -> insertTodolist(reqDto.toEntity(writer))
                        .orElseThrow(() -> new TodoInternalServerException(
                                        String.format(QUERY_FAILED_MESSAGE, TODO, INSERT, reqDto.getContents())
                                )
                        )
                )
                .map(TodoRspDto::new)
                .orElseThrow(() -> new UserNotFoundException(
                        String.format(NOT_FOUND_MESSAGE, USER, reqDto.getWriterId())
                ));
    }

    public TodoRspDto find(long todoId) {
        return findTodo(todoId)
                .map(TodoRspDto::new)
                .orElseThrow(() -> new TodoNotFoundException(
                        String.format(NOT_FOUND_MESSAGE, TODO, todoId)
                ));
    }

    public List<TodoRspDto> findTodos(long userId) {
        List<Todo> findTodos = new ArrayList<>();

        findTodolistByWriterId(userId).ifPresent(findTodos::addAll);

        return findTodos.stream()
                .filter(Todo::isUsed)
                .filter(todo -> todo.getTodoDate().isAfter(LocalDateTime.now().minusDays(1))) // filtering past
                .filter(todo -> todo.getTodoDate().isBefore(
                        LocalDateTime.now().plusDays(2).query(TemporalQueries.localDate()).atStartOfDay()) // filtering 2days more
                )
                .sorted(
                        Comparator.comparing(Todo::getTodoDate)
                                .thenComparing(Todo::getRegDate)
                )
                .map(TodoRspDto::new)
                .collect(toList());
    }

    @Transactional
    public TodoRspDto update(long todoId, TodoReqDto reqDto) {
        return findTodo(todoId)
                .map(todo ->
                        todo.updateTodo(reqDto)
                                .map(TodoRspDto::new)
                                .orElseThrow(() -> new TodoInternalServerException(
                                        String.format(QUERY_FAILED_MESSAGE, TODO, UPDATE, reqDto.getContents())
                                ))
                )
                .orElseThrow(() -> new TodoNotFoundException(
                        String.format(NOT_FOUND_MESSAGE, TODO, todoId)
                ));
    }

    @Transactional
    public TodoRspDto delete(long todoId) {
        return findTodo(todoId)
                .map(todo -> {
                    todo.deleteTodo();
                    return new TodoRspDto(todo);
                })
                .orElseThrow(() -> new TodoNotFoundException(
                        String.format(NOT_FOUND_MESSAGE, TODO, todoId)
                ));
    }

    /**
     * Todolist Repository
     */
    private Optional<Todo> insertTodolist(Todo todolist) {
        return Optional.of(todoRepository.save(todolist));
    }

    private Optional<Todo> findTodo(Long todoId) {
        return Optional.ofNullable(todoId).flatMap(id -> todoRepository.findById(id).filter(Todo::isUsed));
    }

    private Optional<List<Todo>> findTodolistByWriterId(Long userId) {
        return todoRepository.findAllByWriterId(userId);
    }

    /**
     * User Repository
     * <p>
     * When working with a service code, the service code is connected to each other
     * and is caught in an infinite loop in the injection of dependencies.
     */
    private Optional<User> findUser(Long id) {
        return Optional.ofNullable(id).flatMap(userId -> userRepo.findById(userId).filter(User::isUsed));
    }
}
