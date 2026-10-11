package com.hostelmanagement.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "rooms")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String roomNumber;

    @Column(nullable = false, length = 50)
    private String blockName;

    private Integer floorNo;

    @Column(nullable = false)
    private Integer capacity;

    @Column(length = 30)
    private String roomType;

    private Double monthlyRent;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
    public String getBlockName() { return blockName; }
    public void setBlockName(String blockName) { this.blockName = blockName; }
    public Integer getFloorNo() { return floorNo; }
    public void setFloorNo(Integer floorNo) { this.floorNo = floorNo; }
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    public String getRoomType() { return roomType; }
    public void setRoomType(String roomType) { this.roomType = roomType; }
    public Double getMonthlyRent() { return monthlyRent; }
    public void setMonthlyRent(Double monthlyRent) { this.monthlyRent = monthlyRent; }
}
