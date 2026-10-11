package com.hostelmanagement.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "room_change_requests")
public class RoomChangeRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne
    @JoinColumn(name = "current_room_id")
    private Room currentRoom;

    @ManyToOne
    @JoinColumn(name = "requested_room_id")
    private Room requestedRoom;

    @Column(length = 500)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private RequestStatus status;

    private LocalDateTime createdAt;

    @Column(length = 500)
    private String adminRemarks;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }
    public Room getCurrentRoom() { return currentRoom; }
    public void setCurrentRoom(Room currentRoom) { this.currentRoom = currentRoom; }
    public Room getRequestedRoom() { return requestedRoom; }
    public void setRequestedRoom(Room requestedRoom) { this.requestedRoom = requestedRoom; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public RequestStatus getStatus() { return status; }
    public void setStatus(RequestStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getAdminRemarks() { return adminRemarks; }
    public void setAdminRemarks(String adminRemarks) { this.adminRemarks = adminRemarks; }
}
