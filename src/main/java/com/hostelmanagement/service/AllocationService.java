package com.hostelmanagement.service;

import com.hostelmanagement.dto.RoomDetails;
import com.hostelmanagement.dto.Roommate;
import com.hostelmanagement.entity.Room;
import com.hostelmanagement.entity.RoomAllocation;
import com.hostelmanagement.entity.Student;
import com.hostelmanagement.exception.ApiException;
import com.hostelmanagement.repository.RoomAllocationRepository;
import com.hostelmanagement.repository.RoomRepository;
import com.hostelmanagement.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class AllocationService {

    private final RoomAllocationRepository allocationRepo;
    private final StudentRepository studentRepo;
    private final RoomRepository roomRepo;
    private final RoomService roomService;

    public AllocationService(RoomAllocationRepository allocationRepo, StudentRepository studentRepo,
                             RoomRepository roomRepo, RoomService roomService) {
        this.allocationRepo = allocationRepo;
        this.studentRepo = studentRepo;
        this.roomRepo = roomRepo;
        this.roomService = roomService;
    }

    public List<RoomAllocation> activeAllocations() {
        return allocationRepo.findByActiveTrue();
    }

    @Transactional
    public RoomAllocation allocate(Long studentId, Long roomId) {
        Student student = findStudent(studentId);
        Room room = findRoom(roomId);
        if (allocationRepo.findByStudentIdAndActiveTrue(studentId).isPresent()) {
            throw ApiException.badRequest("This student already has a room. Use 'Change Room' instead.");
        }
        return createAllocation(student, room);
    }

    @Transactional
    public void vacate(Long allocationId) {
        RoomAllocation a = allocationRepo.findById(allocationId)
                .orElseThrow(() -> ApiException.notFound("Allocation not found"));
        if (!a.isActive()) {
            throw ApiException.badRequest("This allocation is already closed");
        }
        a.setActive(false);
        a.setVacatedDate(LocalDate.now());
        allocationRepo.save(a);
    }

    /** Moves a student from the current room to another room (closes old allocation, opens new one). */
    @Transactional
    public RoomAllocation changeRoom(Long studentId, Long newRoomId) {
        Student student = findStudent(studentId);
        Room newRoom = findRoom(newRoomId);
        RoomAllocation current = allocationRepo.findByStudentIdAndActiveTrue(studentId)
                .orElseThrow(() -> ApiException.badRequest("This student has no current room"));
        if (current.getRoom().getId().equals(newRoomId)) {
            throw ApiException.badRequest("Student is already in this room");
        }
        if (allocationRepo.countByRoomIdAndActiveTrue(newRoomId) >= newRoom.getCapacity()) {
            throw ApiException.badRequest("Room " + newRoom.getRoomNumber() + " is full");
        }
        current.setActive(false);
        current.setVacatedDate(LocalDate.now());
        allocationRepo.save(current);
        return createAllocation(student, newRoom);
    }

    public Optional<RoomAllocation> currentFor(Long studentId) {
        return allocationRepo.findByStudentIdAndActiveTrue(studentId);
    }

    /** Room + roommates for the student dashboard (returns null if no room yet). */
    public RoomDetails roomDetails(Long studentId) {
        Optional<RoomAllocation> opt = allocationRepo.findByStudentIdAndActiveTrue(studentId);
        if (opt.isEmpty()) {
            return null;
        }
        RoomAllocation a = opt.get();
        List<Roommate> mates = allocationRepo.findByRoomIdAndActiveTrue(a.getRoom().getId()).stream()
                .filter(x -> !x.getStudent().getId().equals(studentId))
                .map(x -> new Roommate(x.getStudent().getName(), x.getStudent().getRollNo(),
                        x.getStudent().getCourse(), x.getStudent().getPhone(), x.getStudent().getEmail()))
                .toList();
        return new RoomDetails(roomService.toView(a.getRoom()), a.getAllocatedDate(), mates);
    }

    private RoomAllocation createAllocation(Student student, Room room) {
        if (allocationRepo.countByRoomIdAndActiveTrue(room.getId()) >= room.getCapacity()) {
            throw ApiException.badRequest("Room " + room.getRoomNumber() + " is full");
        }
        RoomAllocation a = new RoomAllocation();
        a.setStudent(student);
        a.setRoom(room);
        a.setAllocatedDate(LocalDate.now());
        a.setActive(true);
        return allocationRepo.save(a);
    }

    private Student findStudent(Long id) {
        if (id == null) throw ApiException.badRequest("Student is required");
        return studentRepo.findById(id).orElseThrow(() -> ApiException.notFound("Student not found"));
    }

    private Room findRoom(Long id) {
        if (id == null) throw ApiException.badRequest("Room is required");
        return roomRepo.findById(id).orElseThrow(() -> ApiException.notFound("Room not found"));
    }
}
