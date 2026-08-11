package com.employee.employee_management.mapper;

import com.employee.employee_management.dto.PayrollResponse;
import com.employee.employee_management.dto.PayslipResponse;
import com.employee.employee_management.entity.Employee;
import com.employee.employee_management.entity.Payroll;

public final class PayrollMapper {

    private PayrollMapper() {
    }

    public static PayrollResponse toResponse(Payroll payroll) {
        Employee employee = payroll.getEmployee();
        String name = employee.getLastName() == null || employee.getLastName().isBlank()
                ? employee.getFirstName()
                : employee.getFirstName() + " " + employee.getLastName();
        return PayrollResponse.builder()
                .id(payroll.getId())
                .employeeId(employee.getId())
                .employeeCode(employee.getEmployeeCode())
                .employeeName(name)
                .departmentId(employee.getDepartment().getId())
                .departmentName(employee.getDepartment().getName())
                .month(payroll.getMonth())
                .year(payroll.getYear())
                .basicSalary(payroll.getBasicSalary())
                .houseAllowance(payroll.getHouseAllowance())
                .transportAllowance(payroll.getTransportAllowance())
                .medicalAllowance(payroll.getMedicalAllowance())
                .overtime(payroll.getOvertime())
                .bonus(payroll.getBonus())
                .tax(payroll.getTax())
                .deduction(payroll.getDeduction())
                .grossSalary(payroll.getGrossSalary())
                .netSalary(payroll.getNetSalary())
                .paymentStatus(payroll.getPaymentStatus())
                .paymentDate(payroll.getPaymentDate())
                .createdAt(payroll.getCreatedAt())
                .updatedAt(payroll.getUpdatedAt())
                .build();
    }

    public static PayslipResponse toPayslipResponse(Payroll payroll) {
        Employee employee = payroll.getEmployee();
        String name = employee.getLastName() == null || employee.getLastName().isBlank()
                ? employee.getFirstName()
                : employee.getFirstName() + " " + employee.getLastName();
        return PayslipResponse.builder()
                .payrollId(payroll.getId())
                .generatedAt(java.time.LocalDateTime.now())
                .employeeId(employee.getId())
                .employeeCode(employee.getEmployeeCode())
                .employeeName(name)
                .departmentId(employee.getDepartment().getId())
                .departmentName(employee.getDepartment().getName())
                .designationId(employee.getDesignation().getId())
                .designationName(employee.getDesignation().getName())
                .joiningDate(employee.getJoiningDate())
                .month(payroll.getMonth())
                .year(payroll.getYear())
                .basicSalary(payroll.getBasicSalary())
                .houseAllowance(payroll.getHouseAllowance())
                .transportAllowance(payroll.getTransportAllowance())
                .medicalAllowance(payroll.getMedicalAllowance())
                .overtime(payroll.getOvertime())
                .bonus(payroll.getBonus())
                .grossSalary(payroll.getGrossSalary())
                .tax(payroll.getTax())
                .deduction(payroll.getDeduction())
                .netSalary(payroll.getNetSalary())
                .paymentStatus(payroll.getPaymentStatus())
                .paymentDate(payroll.getPaymentDate())
                .build();
    }
}
