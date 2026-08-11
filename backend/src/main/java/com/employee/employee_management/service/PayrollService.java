package com.employee.employee_management.service;

import com.employee.employee_management.dto.PayrollRequest;
import com.employee.employee_management.dto.PayrollResponse;
import com.employee.employee_management.dto.PayslipResponse;
import com.employee.employee_management.entity.PaymentStatus;
import com.employee.employee_management.util.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PayrollService {

    PageResponse<PayrollResponse> getAll(Long employeeId,
                                         Integer month,
                                         Integer year,
                                         PaymentStatus paymentStatus,
                                         Pageable pageable);

    List<PayrollResponse> getEmployeeHistory(Long employeeId);

    PayrollResponse getById(Long id);

    PayrollResponse create(PayrollRequest request);

    PayrollResponse update(Long id, PayrollRequest request);

    PayrollResponse markAsPaid(Long id);

    PayslipResponse getPayslip(Long id);
}
