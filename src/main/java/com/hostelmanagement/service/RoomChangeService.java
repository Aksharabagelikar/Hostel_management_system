package com.hostelmanagement.service;

import com.hostelmanagement.dto.RoomChangeCreate;
import com.hostelmanagement.dto.RoomChangeDecision;
import com.hostelmanagement.entity.RequestStatus;
import com.hostelmanagement.entity.Room;
import com.hostelmanagement.entity.RoomAllocation;
import com.hostelmanagement.entity.RoomChangeRequest;
import com.hostelmanagement.entity.Student;
import com.hostelmanagement.exception.ApiException;
import com.hostelmanagement.repository.RoomChangeRequestRepository;
import com.hostelmanagement.util.Check;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RoomChangeService {

    private final RoomChangeRequestRepository requestRepo;
    private final AllocationService allocationService;
    private final RoomService roomService;

    public RoomChangeService(RoomChangeRequestRepository requestRepo, AllocationService allocationService,
                             RoomService roomService) {
        this.requestRepo = requestRepo;
        this.allocationService = allocationService;
        this.roomService = roomService;
    }

    public List<RoomChangeRequest> listAll() {
        return requestRepo.findAllByOrderByCreatedAtDesc();
    }

    public List<RoomChangeRequest> forStudent(Long studentId) {
        return requestRepo.findByStudentIdOrderByCreatedAtDesc(studentId);
    }

    @Transactional
    public RoomChangeRequest create(Student student, RoomChangeCreate r) {
        RoomAllocation current = allocationService.currentFor(student.getId())
                .orElseThrow(() -> ApiException.badRequest("You do not have a room yet, so you cannot request a change."));
        if (requestRepo.existsByStudentIdAndStatus(student.getId(), RequestStatus.PENDING)) {
            throw ApiException.badRequest("You already have a pending room change request.");
        }
        RoomChangeRequest req = new RoomChangeRequest();
        req.setStudent(student);
        req.setCurrentRoom(current.getRoom());
        if (r.requestedRoomId() != null) {
            Room wanted = roomService.get(r.requestedRoomId());
            if (wanted.getId().equals(current.getRoom().getId())) {
                throw ApiException.badRequest("You are already in that room.");
            }
            req.setRequestedRoom(wanted);
        }
        req.setReason(Check.text(r.reason(), "Reason"));
        req.setStatus(RequestStatus.PENDING);
        req.setCreatedAt(LocalDateTime.now());
        return requestRepo.save(req);
    }

    /** Approve: moves the student to the chosen room (admin may override the requested room). */
    @Transactional
    public RoomChangeRequest approve(Long id, RoomChangeDecision d) {
        RoomChangeRequest req = findPending(id);
        Long roomId = d.roomId() != null ? d.roomId()
                : (req.getRequestedRoom() != null ? req.getRequestedRoom().getId() : null);
        if (roomId == null) {
            throw ApiException.badRequest("Select the room to move the student into");
        }
        allocationService.changeRoom(req.getStudent().getId(), roomId);
        req.setRequestedRoom(roomService.get(roomId));
        req.setStatus(RequestStatus.APPROVED);
        req.setAdminRemarks(Check.optional(d.remarks()));
        return requestRepo.save(req);
    }

    @Transactional
    public RoomChangeRequest reject(Long id, RoomChangeDecision d) {
        RoomChangeRequest req = findPending(id);
        req.setStatus(RequestStatus.REJECTED);
        req.setAdminRemarks(Check.optional(d.remarks()));
        return requestRepo.save(req);
    }

    private RoomChangeRequest findPending(Long id) {
        RoomChangeRequest req = requestRepo.findById(id)
                .orElseThrow(() -> ApiException.notFound("Request not found"));
        if (req.getStatus() != RequestStatus.PENDING) {
            throw ApiException.badRequest("This request has already been processed");
        }
        return req;
    }
}
