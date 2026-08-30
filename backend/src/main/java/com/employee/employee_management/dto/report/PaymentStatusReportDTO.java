package com.employee.employee_management.dto.report;

import com.employee.employee_management.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentStatusReportDTO {

    private PaymentStatus status;
    private long count;
    private BigDecimal totalAmount;
}
