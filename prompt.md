# Employee Management System — Full-Stack Development Prompt

I want to build a **professional, secure, scalable, and production-ready Employee Management System (EMS)**.

Act as a **Senior Full-Stack Software Engineer and Software Architect**. Build this application step-by-step using clean architecture, best practices, proper security, validation, error handling, and a professional UI.

Do not try to build the entire application in a single response. Develop it phase by phase and make sure each phase is functional before moving to the next one.

---

# 1. Technology Stack

## Backend

Use:

- Java 17+
- Spring Boot 4.x
- Spring Web
- Spring Data JPA
- Spring Security
- JWT Authentication
- MySQL
- Maven
- Bean Validation
- Lombok

## Frontend

Use:

- React.js
- Axios
- React Router
- Bootstrap 5
- JavaScript
- Responsive Design

## Architecture

Use a clean layered architecture:

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
Database
```

Use DTOs, mappers, validation, global exception handling, security configuration, and utility classes where appropriate.

---

# 2. Authentication & Authorization

Implement a complete authentication and authorization system.

Features:

- Login
- Logout
- JWT authentication
- Password hashing
- Role-based authorization
- Protected API endpoints
- Protected React routes
- Token validation
- Secure password handling

Roles:

```text
ADMIN
HR
MANAGER
EMPLOYEE
```

## Admin

Admin should have full system access.

## HR

HR should be able to manage:

- Employees
- Departments
- Designations
- Attendance
- Leave
- Payroll
- Performance

## Manager

Manager should be able to:

- View assigned employees
- View team attendance
- Approve/reject leave requests
- View team performance

## Employee

Employee should only be able to:

- View own profile
- View own attendance
- Apply for leave
- View own leave history
- View own salary
- View own performance

---

# 3. Employee Management

Create a complete Employee CRUD system.

Employee fields:

```text
id
employeeCode
firstName
lastName
email
phone
dateOfBirth
gender
address
joiningDate
department
designation
salary
profileImage
status
createdAt
updatedAt
```

Features:

- Add Employee
- Edit Employee
- Delete Employee
- View Employee
- Employee Details
- Search Employee
- Filter Employee
- Pagination
- Sorting
- Active/Inactive status
- Profile image upload

Search should support:

```text
Employee Name
Employee ID
Email
Phone
Department
Designation
```

---

# 4. Department Management

Create a complete Department CRUD system.

Fields:

```text
id
name
description
status
createdAt
updatedAt
```

Features:

- Add Department
- Edit Department
- Delete Department
- View Department
- Search Department
- Active/Inactive status

Prevent deletion when the department still has employees assigned to it, unless a proper reassignment process is implemented.

---

# 5. Designation Management

Create Designation management.

Example designations:

```text
Software Engineer
Senior Software Engineer
Project Manager
HR Manager
Accountant
Marketing Executive
Support Engineer
```

Fields:

```text
id
name
description
departmentId
status
createdAt
updatedAt
```

Features:

- Add Designation
- Edit Designation
- Delete Designation
- Search Designation
- Filter by Department
- Active/Inactive status

---

# 6. Attendance Management

Create a complete attendance management system.

Attendance fields:

```text
id
employeeId
date
checkIn
checkOut
workingHours
overtime
status
remarks
createdAt
updatedAt
```

Attendance statuses:

```text
PRESENT
ABSENT
LATE
HALF_DAY
LEAVE
```

Features:

- Daily attendance
- Employee check-in
- Employee check-out
- Working hours calculation
- Overtime calculation
- Monthly attendance
- Employee attendance history
- Attendance search
- Attendance filtering
- Attendance reports

Example workflow:

```text
Employee
    ↓
Check In
    ↓
Work
    ↓
Check Out
    ↓
Working Hours Calculated
    ↓
Attendance Saved
```

---

# 7. Leave Management

Create a complete Leave Management system.

Leave types:

```text
CASUAL
SICK
ANNUAL
EMERGENCY
MATERNITY
OTHER
```

Leave request fields:

```text
id
employeeId
leaveType
startDate
endDate
totalDays
reason
status
approvedBy
approvedAt
createdAt
updatedAt
```

Leave statuses:

```text
PENDING
APPROVED
REJECTED
CANCELLED
```

Workflow:

```text
Employee
    ↓
Submit Leave Request
    ↓
Manager / HR
    ↓
Approve / Reject
```

Features:

- Apply for leave
- View leave requests
- Approve leave
- Reject leave
- Cancel leave
- Leave history
- Leave balance
- Leave filtering
- Leave reports

Validate:

- Start date cannot be after end date.
- Total leave days must be calculated automatically.
- Employee cannot apply for overlapping approved leave.
- Employee cannot apply for leave if the leave balance is insufficient.

---

# 8. Payroll Management

Create a complete Payroll Management system.

Payroll fields:

```text
id
employeeId
month
year
basicSalary
houseAllowance
transportAllowance
medicalAllowance
overtime
bonus
tax
deduction
grossSalary
netSalary
paymentStatus
paymentDate
createdAt
updatedAt
```

Payment statuses:

```text
PENDING
PAID
CANCELLED
```

Salary calculation:

```text
Gross Salary =
Basic Salary
+ House Allowance
+ Transport Allowance
+ Medical Allowance
+ Overtime
+ Bonus

Net Salary =
Gross Salary
- Tax
- Deduction
```

Features:

- Generate payroll
- Edit payroll
- View payroll
- Monthly payroll
- Employee salary history
- Salary reports
- Payslip generation
- Payment status tracking

Prevent duplicate payroll records for the same employee, month, and year.

---

# 9. Performance Management

Create an Employee Performance Management system.

Fields:

```text
id
employeeId
reviewerId
reviewDate
rating
strengths
weaknesses
comments
createdAt
updatedAt
```

Rating:

```text
1 - Poor
2 - Needs Improvement
3 - Average
4 - Good
5 - Excellent
```

Features:

- Add performance review
- Edit performance review
- View performance
- Employee performance history
- Performance filtering
- Performance reports

---

# 10. Dashboard

Create a professional Admin/HR dashboard.

Display:

```text
Total Employees
Active Employees
Inactive Employees
Total Departments
Today's Present
Today's Absent
Late Employees
Pending Leave Requests
This Month Payroll
```

Charts:

```text
Employees by Department
Attendance Overview
Leave Overview
Monthly Payroll
Employee Status
Performance Overview
```

The dashboard must be:

- Responsive
- Professional
- Fast
- Mobile-friendly

---

# 11. Employee Profile

Create a professional Employee Profile page.

Display:

```text
Profile Photo
Employee Name
Employee ID
Department
Designation
Email
Phone
Joining Date
Status
```

Use tabs:

```text
Profile
Attendance
Leave
Payroll
Performance
```

Employees should only be able to access their own profile.

Admins and authorized HR/Managers should have access according to their permissions.

---

# 12. Search, Filter, Pagination & Sorting

Implement reusable search, filtering, pagination, and sorting components.

Employee filtering:

```text
Name
Employee ID
Department
Designation
Status
Joining Date
```

Attendance filtering:

```text
Employee
Date
Month
Status
Department
```

Leave filtering:

```text
Employee
Leave Type
Status
Date Range
```

Payroll filtering:

```text
Employee
Month
Year
Payment Status
```

Use server-side pagination where appropriate.

---

# 13. Validation

Implement validation on both frontend and backend.

Examples:

- Required fields
- Valid email
- Valid phone number
- Unique email
- Unique employee code
- Salary cannot be negative
- Valid date of birth
- Valid joining date
- Leave date validation
- Payroll validation
- Duplicate record prevention

Use Spring Boot Bean Validation on the backend.

---

# 14. Global Error Handling

Create centralized exception handling.

Example API response:

```json
{
  "success": false,
  "message": "Employee not found",
  "timestamp": "2026-08-08T12:00:00"
}
```

Use appropriate HTTP status codes:

```text
200 OK
201 CREATED
400 BAD REQUEST
401 UNAUTHORIZED
403 FORBIDDEN
404 NOT FOUND
409 CONFLICT
500 INTERNAL SERVER ERROR
```

Create custom exceptions where necessary.

---

# 15. Database Design

Use a normalized relational database design.

Main tables:

```text
users
roles
employees
departments
designations
attendance
leave_types
leave_requests
leave_balances
payroll
performance_reviews
```

Main relationships:

```text
Department
    │
    └── Employees
            │
            ├── Attendance
            ├── Leave Requests
            ├── Leave Balance
            ├── Payroll
            └── Performance Reviews
```

Use appropriate JPA relationships:

```text
@OneToMany
@ManyToOne
@OneToOne
@ManyToMany
```

Avoid unnecessary relationships and prevent circular JSON serialization.

---

# 16. REST API Structure

Create RESTful APIs.

Authentication:

```text
POST /api/auth/login
POST /api/auth/register
```

Employees:

```text
GET    /api/employees
GET    /api/employees/{id}
POST   /api/employees
PUT    /api/employees/{id}
DELETE /api/employees/{id}
```

Departments:

```text
GET    /api/departments
GET    /api/departments/{id}
POST   /api/departments
PUT    /api/departments/{id}
DELETE /api/departments/{id}
```

Designations:

```text
GET    /api/designations
GET    /api/designations/{id}
POST   /api/designations
PUT    /api/designations/{id}
DELETE /api/designations/{id}
```

Attendance:

```text
GET  /api/attendance
POST /api/attendance/check-in
POST /api/attendance/check-out
GET  /api/attendance/employee/{employeeId}
```

Leave:

```text
GET /api/leaves
POST /api/leaves
PUT /api/leaves/{id}/approve
PUT /api/leaves/{id}/reject
PUT /api/leaves/{id}/cancel
```

Payroll:

```text
GET  /api/payroll
GET  /api/payroll/{id}
POST /api/payroll
PUT  /api/payroll/{id}
```

Performance:

```text
GET  /api/performance
POST /api/performance
PUT  /api/performance/{id}
DELETE /api/performance/{id}
```

Dashboard:

```text
GET /api/dashboard/statistics
GET /api/dashboard/attendance
GET /api/dashboard/payroll
GET /api/dashboard/leave
```

---

# 17. Security

Use Spring Security with JWT.

Security requirements:

- BCrypt password hashing
- JWT access token
- Authentication filter
- Role-based authorization
- Protected endpoints
- CORS configuration
- Secure password handling
- Token expiration
- Unauthorized access handling
- Forbidden access handling

Never store passwords as plain text.

Do not expose sensitive information in API responses.

---

# 18. React Frontend

Create a professional HR/Admin dashboard UI.

Pages:

```text
/login

/dashboard

/employees
/employees/add
/employees/:id
/employees/:id/edit

/departments

/designations

/attendance

/leaves

/payroll

/performance

/profile

/settings
```

UI components:

```text
Sidebar
Navbar
Dashboard Cards
Data Tables
Forms
Modals
Dropdowns
Pagination
Search Box
Filter Panel
Loading Spinner
Toast Notifications
Confirmation Dialogs
```

The UI must be:

- Modern
- Clean
- Responsive
- Mobile-friendly
- Accessible
- Easy to use

---

# 19. Project Structure

## Backend

```text
src/main/java/com/example/ems/

├── config
├── controller
├── dto
├── entity
├── repository
├── service
├── service/impl
├── security
├── exception
├── mapper
└── util
```

## Frontend

```text
src/

├── components
├── pages
├── layouts
├── services
├── hooks
├── context
├── utils
├── routes
└── assets
```

Keep the project structure clean and scalable.

---

# 20. Code Quality Rules

Follow these rules throughout the project:

1. Write clean and maintainable code.
2. Follow SOLID principles.
3. Follow DRY principles.
4. Avoid duplicate code.
5. Use DTOs for API communication.
6. Do not expose JPA entities directly when DTOs are more appropriate.
7. Use meaningful class, method, and variable names.
8. Use proper validation.
9. Implement global exception handling.
10. Follow REST API best practices.
11. Follow security best practices.
12. Use proper database constraints.
13. Avoid unnecessary dependencies.
14. Keep frontend and backend loosely coupled.
15. Write production-ready code.
16. Keep API responses consistent.
17. Use transactions where necessary.
18. Optimize database queries when required.

---

# 21. Development Phases

Develop the system in the following order.

## Phase 1 — Project Setup

- Spring Boot project
- React project
- MySQL database
- Maven configuration
- Database connection
- Basic project architecture

## Phase 2 — Database & Entities

- Database schema
- JPA entities
- Relationships
- Repositories
- Initial database configuration

## Phase 3 — Authentication & Security

- User entity
- Role entity
- Login
- JWT
- Spring Security
- Role-based authorization

## Phase 4 — Employee Management

- Employee CRUD
- Search
- Filter
- Pagination
- Profile image

## Phase 5 — Department & Designation

- Department CRUD
- Designation CRUD
- Employee assignment

## Phase 6 — Attendance

- Check-in
- Check-out
- Attendance history
- Working hours
- Overtime
- Reports

## Phase 7 — Leave Management

- Leave types
- Leave balance
- Leave application
- Approval/rejection
- Leave history

## Phase 8 — Payroll

- Salary configuration
- Payroll generation
- Salary calculation
- Payslip
- Payment tracking

## Phase 9 — Performance

- Performance review
- Ratings
- Performance history
- Reports

## Phase 10 — Dashboard

- Statistics
- Charts
- Reports
- Recent activities

## Phase 11 — Testing

Implement:

- Unit testing
- Integration testing
- API testing
- Authentication testing
- Authorization testing
- Validation testing

## Phase 12 — Optimization & Deployment

- Database optimization
- API optimization
- Security review
- Production configuration
- Environment variables
- Docker support
- Deployment documentation

---

# 22. Development Instructions

Do not generate the entire project at once.

Work phase-by-phase.

Before starting each phase, explain briefly:

```text
1. What will be built
2. Which files will be created
3. Which files will be modified
4. What database changes are required
5. What APIs will be added
```

Then provide the complete required code.

If an existing file needs to be modified, ask for the current file if its contents are not already available.

When modifying a file, provide the complete updated file instead of only a small code fragment whenever practical.

Do not assume missing code.

After completing each phase, provide:

```text
1. What was completed
2. How to run it
3. How to test it
4. Common errors and solutions
5. What the next phase will be
```

Do not move to the next phase until the current phase is working correctly.

---

# 23. Important Requirement

The final application should look and behave like a real-world professional Employee Management System rather than a basic CRUD demo.

The system must be:

- Secure
- Scalable
- Maintainable
- Responsive
- User-friendly
- Production-ready

Avoid unnecessary complexity, but do not simplify important security, validation, database, or business logic.

---

# START DEVELOPMENT

Start with **Phase 1: Spring Boot + React + MySQL Project Setup**.

First provide:

1. Complete project architecture
2. Backend project setup
3. Required Maven dependencies
4. MySQL database configuration
5. Backend folder structure
6. React project setup
7. Frontend folder structure
8. Basic frontend-backend connection
9. Instructions to run both applications

After Phase 1 is complete and working, proceed to Phase 2.