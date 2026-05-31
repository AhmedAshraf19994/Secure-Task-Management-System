package com.ahmed.Secure.Task.Management.System.taskAttachment;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskAttachmentRepository extends JpaRepository<TaskAttachment, Integer> {

    Optional<TaskAttachment> findByIdAndTaskId(int attachmentId, int taskId);

    @EntityGraph(attributePaths = {"uploadedBy"})
    List<TaskAttachment> findByTaskId(int taskId);

    @Modifying
    @Transactional
    @Query("UPDATE TaskAttachment t Set t.status = :status WHERE t.id = :attachmentId")
    void updateStatus(@Param("attachmentId") int attachmentId, @Param("status") TaskAttachmentStatus status);
}
