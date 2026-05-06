package com.ahmed.Secure.Task.Management.System.taskActivity;

import com.ahmed.Secure.Task.Management.System.task.events.TaskAssignedEvent;
import com.ahmed.Secure.Task.Management.System.task.events.TaskReassignedEvent;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.test.context.transaction.TestTransaction;
import java.util.concurrent.TimeUnit;

@SpringBootTest
public class TaskServiceListenerIntegrationTest {

    @Autowired
    ApplicationEventPublisher publisher;

    @Autowired
    TaskActivityRepository taskActivityRepository;

    @BeforeEach
    void clean () {
        this.taskActivityRepository.deleteAll();
    }

    @Test
    @Transactional
    void shouldHandleTaskAssignedSuccess () {
        //given
        TaskAssignedEvent taskAssignedEvent = new TaskAssignedEvent(1, 1, 2);

        //when
        this.publisher.publishEvent(taskAssignedEvent);

        //handling the transaction phase after commit in my event listener
        TestTransaction.flagForCommit();
        TestTransaction.end();

        //then
        await().atMost(2, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    assertEquals(1, this.taskActivityRepository.findAll().size());
                });
    }

    @Test
    @Transactional
    void shouldHandleTaskReassignedSuccess () {
        //given
        TaskReassignedEvent taskReassignedEvent = new TaskReassignedEvent(1, 1, 3, 2);

        //when
        this.publisher.publishEvent(taskReassignedEvent);

        //handling the transaction phase after commit
        TestTransaction.flagForCommit();
        TestTransaction.end();

        //then
        await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
            assertEquals(1, taskActivityRepository.findAll().size());
        });
    }

}
