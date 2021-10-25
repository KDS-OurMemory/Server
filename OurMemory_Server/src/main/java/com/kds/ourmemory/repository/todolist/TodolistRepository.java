package com.kds.ourmemory.repository.todolist;

import com.kds.ourmemory.entity.todolist.Todolist;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;

@Transactional
public interface TodolistRepository extends JpaRepository<Todolist, Long> {
}
