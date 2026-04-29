package com.ahmed.Secure.Task.Management.System.task;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Integer> {

    @EntityGraph( attributePaths = {"createdBy", "assignedTo"}) //for n+1 query
    Page<Task> findAll(Pageable pageable);
}
