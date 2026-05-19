package com.ahmed.Secure.Task.Management.System.notification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    Page<Notification> findByReceiverIdAndIsReadFalseOrderByCreatedAtDesc(int receiverId, Pageable pageable);


}
