package com.hostelmanagement.service;

import com.hostelmanagement.dto.RoomRequest;
import com.hostelmanagement.dto.RoomView;
import com.hostelmanagement.entity.Room;
import com.hostelmanagement.exception.ApiException;
import com.hostelmanagement.repository.RoomAllocationRepository;
import com.hostelmanagement.repository.RoomChangeRequestRepository;
import com.hostelmanagement.repository.RoomRepository;
import com.hostelmanagement.util.Check;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RoomService {

    private final RoomRepository roomRepo;
    private final RoomAllocationRepository allocationRepo;
    private final RoomChangeRequestRepository requestRepo;

    public RoomService(RoomRepository roomRepo, RoomAllocationRepository allocationRepo,
                       RoomChangeRequestRepository requestRepo) {
        this.roomRepo = roomRepo;
        this.allocationRepo = allocationRepo;
        this.requestRepo = requestRepo;
    }

    /** filter: all | available (has free beds) | occupied (at least one student) | vacant (empty) */
    public List<RoomView> list(String filter) {
        return roomRepo.findAll(Sort.by("blockName", "roomNumber")).stream()
                .map(this::toView)
                .filter(v -> matches(v, filter))
                .toList();
    }

    public Room get(Long id) {
        return roomRepo.findById(id).orElseThrow(() -> ApiException.notFound("Room not found"));
    }

    public RoomView toView(Room r) {
        long occupied = allocationRepo.countByRoomIdAndActiveTrue(r.getId());
        long available = Math.max(0L, r.getCapacity() - occupied);
        String status = occupied == 0 ? "VACANT" : (occupied >= r.getCapacity() ? "FULL" : "PARTIAL");
        return new RoomView(r.getId(), r.getRoomNumber(), r.getBlockName(), r.getFloorNo(), r.getCapacity(),
                r.getRoomType(), r.getMonthlyRent(), occupied, available, status);
    }

    @Transactional
    public RoomView create(RoomRequest req) {
        Room r = new Room();
        apply(r, req);
        if (roomRepo.existsByRoomNumber(r.getRoomNumber())) {
            throw ApiException.badRequest("Room number already exists");
        }
        return toView(roomRepo.save(r));
    }

    @Transactional
    public RoomView update(Long id, RoomRequest req) {
        Room r = get(id);
        String oldNumber = r.getRoomNumber();
        apply(r, req);
        if (!r.getRoomNumber().equals(oldNumber) && roomRepo.existsByRoomNumber(r.getRoomNumber())) {
            throw ApiException.badRequest("Room number already exists");
        }
        long occupied = allocationRepo.countByRoomIdAndActiveTrue(id);
        if (r.getCapacity() < occupied) {
            throw ApiException.badRequest("Capacity cannot be less than current occupants (" + occupied + ")");
        }
        return toView(roomRepo.save(r));
    }

    @Transactional
    public void delete(Long id) {
        Room r = get(id);
        if (allocationRepo.countByRoomIdAndActiveTrue(id) > 0) {
            throw ApiException.badRequest("Room has students. Vacate the room before deleting it.");
        }
        allocationRepo.deleteByRoomId(id);
        requestRepo.deleteByCurrentRoomId(id);
        requestRepo.deleteByRequestedRoomId(id);
        roomRepo.delete(r);
    }

    private void apply(Room r, RoomRequest req) {
        r.setRoomNumber(Check.text(req.roomNumber(), "Room number"));
        r.setBlockName(Check.text(req.blockName(), "Block"));
        if (req.capacity() == null || req.capacity() < 1) {
            throw ApiException.badRequest("Capacity must be at least 1");
        }
        if (req.monthlyRent() != null && req.monthlyRent() < 0) {
            throw ApiException.badRequest("Rent cannot be negative");
        }
        r.setCapacity(req.capacity());
        r.setFloorNo(req.floorNo() == null ? 0 : req.floorNo());
        r.setMonthlyRent(req.monthlyRent() == null ? 0.0 : req.monthlyRent());
        String type = Check.optional(req.roomType());
        if (type == null) {
            type = switch (req.capacity()) {
                case 1 -> "Single";
                case 2 -> "Double";
                case 3 -> "Triple";
                default -> "Dormitory";
            };
        }
        r.setRoomType(type);
    }

    private boolean matches(RoomView v, String filter) {
        if (filter == null || filter.equalsIgnoreCase("all")) {
            return true;
        }
        return switch (filter.toLowerCase()) {
            case "available" -> v.available() > 0;
            case "occupied" -> v.occupied() > 0;
            case "vacant" -> v.occupied() == 0;
            default -> true;
        };
    }
}
