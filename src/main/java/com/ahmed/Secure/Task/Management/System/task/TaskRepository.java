package com.ahmed.Secure.Task.Management.System.task;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TaskRepository extends JpaRepository<Task, Integer> , JpaSpecificationExecutor<Task> {

    @EntityGraph( attributePaths = {"createdBy", "assignedTo"}) //for n+1 query
    Page<Task> findAll(Pageable pageable);
}
