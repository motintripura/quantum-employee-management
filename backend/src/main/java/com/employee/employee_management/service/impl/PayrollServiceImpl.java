package com.employee.employee_management.service.impl;

import com.employee.employee_management.dto.PayrollRequest;
import com.employee.employee_management.dto.PayrollResponse;
import com.employee.employee_management.dto.PayslipResponse;
import com.employee.employee_management.entity.Employee;
import com.employee.employee_management.entity.PaymentStatus;
import com.employee.employee_management.entity.Payroll;
import com.employee.employee_management.entity.Role;
import com.employee.employee_management.entity.User;
import com.employee.employee_management.exception.BadRequestException;
import com.employee.employee_management.exception.DuplicateResourceException;
import com.employee.employee_management.exception.ForbiddenException;
import com.employee.employee_management.exception.ResourceNotFoundException;
import com.employee.employee_management.mapper.PayrollMapper;
import com.employee.employee_management.repository.EmployeeRepository;
import com.employee.employee_management.repository.PayrollRepository;
import com.employee.employee_management.repository.UserRepository;
import com.employee.employee_management.service.PayrollService;
import com.employee.employee_management.util.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PayrollServiceImpl implements PayrollService {

    private final PayrollRepository payrollRepository;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PayrollResponse> getAll(Long employeeId,
                                                Integer month,
                                                Integer year,
                                                PaymentStatus paymentStatus,
                                                Pageable pageable) {
        User user = getCurrentUser();
        if (user.getRole() == Role.EMPLOYEE) {
            Long ownId = user.getEmployee().getId();
            if (employeeId != null && !employeeId.equals(ownId)) {
                throw new ForbiddenException("Employees can only view their own payroll");
            }
            employeeId = ownId;
        }
        Page<Payroll> page = payrollRepository.search(employeeId, month, year, paymentStatus, pageable);
        return PageResponse.from(page.map(PayrollMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PayrollResponse> getEmployeeHistory(Long employeeId) {
        User user = getCurrentUser();
        if (user.getRole() == Role.EMPLOYEE && !employeeId.equals(user.getEmployee().getId())) {
            throw new ForbiddenException("Employees can only view their own payroll");
        }
        findEmployee(employeeId);
        return payrollRepository.findByEmployeeIdOrderByYearDescMonthDesc(employeeId)
                .stream().map(PayrollMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PayrollResponse getById(Long id) {
        Payroll payroll = findPayroll(id);
        enforceEmployeeAccess(payroll);
        return PayrollMapper.toResponse(payroll);
    }

    @Override
    @Transactional
    public PayrollResponse create(PayrollRequest request) {
        Employee employee = findEmployee(request.getEmployeeId());
        if (payrollRepository.existsByEmployeeIdAndMonthAndYear(employee.getId(), request.getMonth(), request.getYear())) {
            throw new DuplicateResourceException("Payroll already exists for this employee for "
                    + request.getMonth() + "/" + request.getYear());
        }

        Payroll payroll = new Payroll();
        applyRequest(payroll, request, employee);
        payrollRepository.save(payroll);
        return PayrollMapper.toResponse(payroll);
    }

    @Override
    @Transactional
    public PayrollResponse update(Long id, PayrollRequest request) {
        Payroll payroll = findPayroll(id);
        Employee employee = findEmployee(request.getEmployeeId());
        if (!payroll.getEmployee().getId().equals(employee.getId())
                || !payroll.getMonth().equals(request.getMonth())
                || !payroll.getYear().equals(request.getYear())) {
            if (payrollRepository.existsByEmployeeIdAndMonthAndYear(employee.getId(), request.getMonth(), request.getYear())) {
                throw new DuplicateResourceException("Another payroll already exists for this employee for "
                        + request.getMonth() + "/" + request.getYear());
            }
        }
        applyRequest(payroll, request, employee);
        payrollRepository.save(payroll);
        return PayrollMapper.toResponse(payroll);
    }

    @Override
    @Transactional
    public PayrollResponse markAsPaid(Long id) {
        Payroll payroll = findPayroll(id);
        payroll.setPaymentStatus(PaymentStatus.PAID);
        if (payroll.getPaymentDate() == null) {
            payroll.setPaymentDate(LocalDate.now());
        }
        payrollRepository.save(payroll);
        return PayrollMapper.toResponse(payroll);
    }

    @Override
    @Transactional(readOnly = true)
    public PayslipResponse getPayslip(Long id) {
        Payroll payroll = findPayroll(id);
        enforceEmployeeAccess(payroll);
        return PayrollMapper.toPayslipResponse(payroll);
    }

    private void applyRequest(Payroll payroll, PayrollRequest request, Employee employee) {
        BigDecimal basic = nvl(request.getBasicSalary());
        BigDecimal house = nvl(request.getHouseAllowance());
        BigDecimal transport = nvl(request.getTransportAllowance());
        BigDecimal medical = nvl(request.getMedicalAllowance());
        BigDecimal overtime = nvl(request.getOvertime());
        BigDecimal bonus = nvl(request.getBonus());
        BigDecimal tax = nvl(request.getTax());
        BigDecimal deduction = nvl(request.getDeduction());

        if (basic.signum() == 0) {
            basic = employee.getSalary();
        }

        BigDecimal gross = basic.add(house).add(transport).add(medical).add(overtime).add(bonus);
        BigDecimal net = gross.subtract(tax).subtract(deduction);

        payroll.setEmployee(employee);
        payroll.setMonth(request.getMonth());
        payroll.setYear(request.getYear());
        payroll.setBasicSalary(basic);
        payroll.setHouseAllowance(house);
        payroll.setTransportAllowance(transport);
        payroll.setMedicalAllowance(medical);
        payroll.setOvertime(overtime);
        payroll.setBonus(bonus);
        payroll.setTax(tax);
        payroll.setDeduction(deduction);
        payroll.setGrossSalary(gross);
        payroll.setNetSalary(net);

        PaymentStatus status = request.getPaymentStatus() == null ? PaymentStatus.PENDING : request.getPaymentStatus();
        payroll.setPaymentStatus(status);
        if (status == PaymentStatus.PAID) {
            if (payroll.getPaymentDate() == null) {
                payroll.setPaymentDate(LocalDate.now());
            }
        } else {
            payroll.setPaymentDate(null);
        }
    }

    private void enforceEmployeeAccess(Payroll payroll) {
        User user = getCurrentUser();
        if (user.getRole() == Role.EMPLOYEE && !payroll.getEmployee().getId().equals(user.getEmployee().getId())) {
            throw new ForbiddenException("Employees can only view their own payroll");
        }
    }

    private BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));
    }

    private Employee findEmployee(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
    }

    private Payroll findPayroll(Long id) {
        return payrollRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payroll not found with id: " + id));
    }
}
