package com.kds.ourmemory.v1.service.todo;

import com.kds.ourmemory.v1.advice.todo.exception.TodoInternalServerException;
import com.kds.ourmemory.v1.advice.todo.exception.TodoNotFoundException;
import com.kds.ourmemory.v1.advice.user.exception.UserNotFoundException;
import com.kds.ourmemory.v1.controller.todo.dto.TodoReqDto;
import com.kds.ourmemory.v1.controller.todo.dto.TodoRspDto;
import com.kds.ourmemory.v1.entity.todo.Todo;
import com.kds.ourmemory.v1.entity.user.User;
import com.kds.ourmemory.v1.repository.todo.TodoRepository;
import com.kds.ourmemory.v1.repository.user.UserRepository;
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

    public TodoRspDto insert(TodoReqDto reqDto) {
        return findUser(reqDto.getWriterId())
                .map(writer -> insertTodolist(reqDto.toEntity(writer))
                        .orElseThrow(TodoInternalServerException::new)
                )
                .map(TodoRspDto::new)
                .orElseThrow(() -> new UserNotFoundException(reqDto.getWriterId()));
    }

    public TodoRspDto find(long todoId) {
        return findTodo(todoId)
                .map(TodoRspDto::new)
                .orElseThrow(() -> new TodoNotFoundException(todoId));
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
                                .orElseThrow(TodoInternalServerException::new)
                )
                .orElseThrow(() -> new TodoNotFoundException(todoId));
    }

    @Transactional
    public TodoRspDto delete(long todoId) {
        findTodo(todoId)
                .map(todo -> {
                    todo.deleteTodo();
                    return true;
                })
                .orElseThrow(() -> new TodoNotFoundException(todoId));

        // delete response is null -> client already have data, so don't need response data.
        return null;
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
