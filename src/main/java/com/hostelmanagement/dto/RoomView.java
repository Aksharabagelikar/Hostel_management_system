package com.hostelmanagement.dto;

import com.hostelmanagement.entity.*;
import java.time.LocalDate;
import java.util.List;

public record RoomView(Long id, String roomNumber, String blockName, Integer floorNo, Integer capacity, String roomType, Double monthlyRent, long occupied, long available, String status) { }
