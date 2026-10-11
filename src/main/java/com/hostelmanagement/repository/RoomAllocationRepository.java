package com.hostelmanagement.repository;

import com.hostelmanagement.entity.RoomAllocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoomAllocationRepository extends JpaRepository<RoomAllocation, Long> {
    List<RoomAllocation> findByActiveTrue();
    List<RoomAllocation> findByRoomIdAndActiveTrue(Long roomId);
    Optional<RoomAllocation> findByStudentIdAndActiveTrue(Long studentId);
    long countByRoomIdAndActiveTrue(Long roomId);
    long countByActiveTrue();
    void deleteByStudentId(Long studentId);
    void deleteByRoomId(Long roomId);
}
