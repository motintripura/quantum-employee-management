package com.employee.employee_management.dto;

import com.employee.employee_management.entity.PaymentStatus;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayrollRequest {

    @NotNull(message = "Employee is required")
    private Long employeeId;

    @NotNull(message = "Month is required")
    @Min(value = 1, message = "Month must be between 1 and 12")
    @Max(value = 12, message = "Month must be between 1 and 12")
    private Integer month;

    @NotNull(message = "Year is required")
    @Min(value = 2000, message = "Year must be 2000 or later")
    private Integer year;

    @DecimalMin(value = "0.00", message = "Basic salary cannot be negative")
    private BigDecimal basicSalary;

    @DecimalMin(value = "0.00", message = "House allowance cannot be negative")
    private BigDecimal houseAllowance;

    @DecimalMin(value = "0.00", message = "Transport allowance cannot be negative")
    private BigDecimal transportAllowance;

    @DecimalMin(value = "0.00", message = "Medical allowance cannot be negative")
    private BigDecimal medicalAllowance;

    @DecimalMin(value = "0.00", message = "Overtime cannot be negative")
    private BigDecimal overtime;

    @DecimalMin(value = "0.00", message = "Bonus cannot be negative")
    private BigDecimal bonus;

    @DecimalMin(value = "0.00", message = "Tax cannot be negative")
    private BigDecimal tax;

    @DecimalMin(value = "0.00", message = "Deduction cannot be negative")
    private BigDecimal deduction;

    private PaymentStatus paymentStatus;
}
