package com.hostelmanagement.repository;

import com.hostelmanagement.entity.RequestStatus;
import com.hostelmanagement.entity.RoomChangeRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomChangeRequestRepository extends JpaRepository<RoomChangeRequest, Long> {
    List<RoomChangeRequest> findAllByOrderByCreatedAtDesc();
    List<RoomChangeRequest> findByStudentIdOrderByCreatedAtDesc(Long studentId);
    boolean existsByStudentIdAndStatus(Long studentId, RequestStatus status);
    long countByStatus(RequestStatus status);
    void deleteByStudentId(Long studentId);
    void deleteByCurrentRoomId(Long roomId);
    void deleteByRequestedRoomId(Long roomId);
}
