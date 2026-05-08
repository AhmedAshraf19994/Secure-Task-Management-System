package com.ahmed.Secure.Task.Management.System.taskActivity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskActivityRepository extends JpaRepository<TaskActivity, Integer> {

    @EntityGraph(attributePaths = {"actor", "newAssignee", "oldAssignee"})
    Page<TaskActivity> findAllByTaskId(int taskId, Pageable pageable);
}
