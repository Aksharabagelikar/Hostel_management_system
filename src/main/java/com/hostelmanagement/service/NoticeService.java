package com.hostelmanagement.service;

import com.hostelmanagement.dto.NoticeRequest;
import com.hostelmanagement.entity.Notice;
import com.hostelmanagement.exception.ApiException;
import com.hostelmanagement.repository.NoticeRepository;
import com.hostelmanagement.util.Check;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class NoticeService {

    private final NoticeRepository noticeRepo;

    public NoticeService(NoticeRepository noticeRepo) {
        this.noticeRepo = noticeRepo;
    }

    public List<Notice> listAll() {
        return noticeRepo.findAllByOrderByPostedDateDescIdDesc();
    }

    @Transactional
    public Notice create(NoticeRequest r, String postedBy) {
        Notice n = new Notice();
        n.setTitle(Check.text(r.title(), "Title"));
        n.setContent(Check.text(r.content(), "Content"));
        n.setImportant(Boolean.TRUE.equals(r.important()));
        n.setPostedBy(postedBy);
        n.setPostedDate(LocalDate.now());
        return noticeRepo.save(n);
    }

    @Transactional
    public Notice update(Long id, NoticeRequest r) {
        Notice n = noticeRepo.findById(id).orElseThrow(() -> ApiException.notFound("Notice not found"));
        n.setTitle(Check.text(r.title(), "Title"));
        n.setContent(Check.text(r.content(), "Content"));
        n.setImportant(Boolean.TRUE.equals(r.important()));
        return noticeRepo.save(n);
    }

    @Transactional
    public void delete(Long id) {
        if (!noticeRepo.existsById(id)) throw ApiException.notFound("Notice not found");
        noticeRepo.deleteById(id);
    }
}
