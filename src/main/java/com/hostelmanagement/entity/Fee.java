package com.hostelmanagement.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "fees")
public class Fee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(nullable = false)
    private Double amount;

    private LocalDate dueDate;

    private LocalDate paidDate;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private FeeStatus status;

    @Column(length = 150)
    private String description;

    @Column(length = 30)
    private String paymentMode;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public LocalDate getPaidDate() { return paidDate; }
    public void setPaidDate(LocalDate paidDate) { this.paidDate = paidDate; }
    public FeeStatus getStatus() { return status; }
    public void setStatus(FeeStatus status) { this.status = status; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getPaymentMode() { return paymentMode; }
    public void setPaymentMode(String paymentMode) { this.paymentMode = paymentMode; }
}
