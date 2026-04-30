package com.ahmed.Secure.Task.Management.System.task;


import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class TaskSpecifications  {

    public static Specification<Task> titleContains (String providedTitle) {
        return (root,query, criteriaBuilder) ->
            criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), "%" + providedTitle + "%");
        }

    public static Specification<Task> hasStatus(TaskStatus providedStatus) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("status"), providedStatus);
    }

    public static Specification<Task> hasPriority(TaskPriority providedPriority) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("priority"), providedPriority);
    }

    public static Specification<Task> dueBefore (LocalDateTime providedDate) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThan(root.get("dueDate"), providedDate);
    }

    public static Specification<Task> dueBetween (LocalDateTime dueBefore, LocalDateTime dueAfter) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.between(root.get("dueDate"),dueBefore, dueAfter);
    }

}
