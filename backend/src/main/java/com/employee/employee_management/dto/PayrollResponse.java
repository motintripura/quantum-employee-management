package com.employee.employee_management.dto;

import com.employee.employee_management.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayrollResponse {

    private Long id;
    private Long employeeId;
    private String employeeCode;
    private String employeeName;
    private Long departmentId;
    private String departmentName;
    private Integer month;
    private Integer year;
    private BigDecimal basicSalary;
    private BigDecimal houseAllowance;
    private BigDecimal transportAllowance;
    private BigDecimal medicalAllowance;
    private BigDecimal overtime;
    private BigDecimal bonus;
    private BigDecimal tax;
    private BigDecimal deduction;
    private BigDecimal grossSalary;
    private BigDecimal netSalary;
    private PaymentStatus paymentStatus;
    private LocalDate paymentDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
