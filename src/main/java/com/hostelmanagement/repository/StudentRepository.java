package com.hostelmanagement.repository;

import com.hostelmanagement.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByUserId(Long userId);

    boolean existsByRollNo(String rollNo);

    @Query("select s from Student s where lower(s.name) like lower(concat('%', :q, '%')) "
         + "or lower(s.rollNo) like lower(concat('%', :q, '%')) "
         + "or lower(s.course) like lower(concat('%', :q, '%')) order by s.name")
    List<Student> search(@Param("q") String q);
}
