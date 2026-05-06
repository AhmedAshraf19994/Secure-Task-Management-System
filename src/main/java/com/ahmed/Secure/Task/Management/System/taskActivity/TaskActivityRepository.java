package com.ahmed.Secure.Task.Management.System.taskActivity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskActivityRepository extends JpaRepository<TaskActivity, Integer> {


    List<TaskActivity> findAll();
}
