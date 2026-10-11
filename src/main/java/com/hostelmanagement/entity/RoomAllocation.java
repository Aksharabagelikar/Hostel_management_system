package com.hostelmanagement.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "room_allocations")
public class RoomAllocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    private LocalDate allocatedDate;

    private LocalDate vacatedDate;

    private boolean active;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }
    public Room getRoom() { return room; }
    public void setRoom(Room room) { this.room = room; }
    public LocalDate getAllocatedDate() { return allocatedDate; }
    public void setAllocatedDate(LocalDate allocatedDate) { this.allocatedDate = allocatedDate; }
    public LocalDate getVacatedDate() { return vacatedDate; }
    public void setVacatedDate(LocalDate vacatedDate) { this.vacatedDate = vacatedDate; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
