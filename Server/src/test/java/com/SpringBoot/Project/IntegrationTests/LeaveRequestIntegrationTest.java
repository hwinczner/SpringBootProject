package com.SpringBoot.Project.IntegrationTests;

import com.SpringBoot.Project.Models.*;
import com.SpringBoot.Project.Repositories.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.Matchers.containsString;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LeaveRequestIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LeaveRequestInterface leaveRequestInterface;

    @Autowired
    private EmployeeInterface employeeInterface;

    @Autowired
    private DepartmentInterface departmentInterface;

    private Department department;
    private Employee employee;
    private Employee managerEmployee;

    @BeforeEach
    void setUp() {
        // Clean up the database
        leaveRequestInterface.deleteAll();
        employeeInterface.deleteAll();
        departmentInterface.deleteAll();

        // Create test department
        department = departmentInterface.save(new Department("IT", "Information Technology"));

        // Create test employee
        employee = new Employee("John Doe", "john.doe@example.com", department, "Developer");
        employee = employeeInterface.save(employee);

        // Create test manager
        managerEmployee = new Employee("Jane Manager", "jane.manager@example.com", department, "Manager");
        managerEmployee = employeeInterface.save(managerEmployee);
    }

    @Test
    @Transactional
    void testSubmitLeaveRequest() throws Exception {
        LeaveRequest newRequest = createTestLeaveRequest(
                employee,
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(10),
                "Annual leave"
        );

        String requestJson = objectMapper.writeValueAsString(newRequest);

        mockMvc.perform(post("/api/leaves")
                        .param("employeeId", String.valueOf(employee.getEmployeeId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    @Transactional
    void testOverlappingLeaveRequestsForSameEmployee() throws Exception {
        // Create and save first leave request
        LeaveRequest firstRequest = createTestLeaveRequest(
                employee,
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(10),
                "First leave"
        );
        firstRequest.setStatus(Status.APPROVED);
        leaveRequestInterface.save(firstRequest);

        // Try to create overlapping request
        LeaveRequest overlappingRequest = createTestLeaveRequest(
                employee,
                LocalDate.now().plusDays(8),  // Overlaps with first request
                LocalDate.now().plusDays(15),
                "Overlapping leave"
        );

        String requestJson = objectMapper.writeValueAsString(overlappingRequest);

        mockMvc.perform(post("/api/leaves")
                        .param("employeeId", String.valueOf(employee.getEmployeeId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Overlapping")));
    }

    @Test
    @Transactional
    void testLeaveRequestValidation() throws Exception {
        // Test with past start date
        LeaveRequest pastRequest = createTestLeaveRequest(
                employee,
                LocalDate.now().minusDays(5),  // Past date
                LocalDate.now().plusDays(5),
                "Invalid dates"
        );

        String pastRequestJson = objectMapper.writeValueAsString(pastRequest);

        mockMvc.perform(post("/api/leaves")
                        .param("employeeId", String.valueOf(employee.getEmployeeId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pastRequestJson))
                .andExpect(status().isBadRequest());

        // Test with end date before start date
        LeaveRequest invalidDateRequest = createTestLeaveRequest(
                employee,
                LocalDate.now().plusDays(10),
                LocalDate.now().plusDays(5),  // Before start date
                "Invalid date range"
        );

        String invalidDateJson = objectMapper.writeValueAsString(invalidDateRequest);

        mockMvc.perform(post("/api/leaves")
                        .param("employeeId", String.valueOf(employee.getEmployeeId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidDateJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    void testNonOverlappingLeaveRequestsForDifferentEmployees() throws Exception {
        // Create and save first employee's leave request
        LeaveRequest firstEmployeeRequest = createTestLeaveRequest(
                employee,
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(10),
                "First employee leave"
        );
        firstEmployeeRequest.setStatus(Status.APPROVED);
        leaveRequestInterface.save(firstEmployeeRequest);

        // Create second employee's request for same dates (should be allowed)
        Employee secondEmployee = new Employee("Jane Smith", "jane.smith@example.com", department, "Developer");
        secondEmployee = employeeInterface.save(secondEmployee);

        LeaveRequest secondEmployeeRequest = createTestLeaveRequest(
                secondEmployee,
                LocalDate.now().plusDays(5),  // Same dates as first request
                LocalDate.now().plusDays(10),
                "Second employee leave"
        );

        String requestJson = objectMapper.writeValueAsString(secondEmployeeRequest);

        mockMvc.perform(post("/api/leaves")
                        .param("employeeId", String.valueOf(secondEmployee.getEmployeeId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // Helper method to create test leave requests
    private LeaveRequest createTestLeaveRequest(Employee employee, LocalDate startDate, LocalDate endDate, String reason) {
        LeaveRequest request = new LeaveRequest();
        request.setEmployee(employee);
        request.setStartDate(startDate);
        request.setEndDate(endDate);
        request.setStatus(Status.PENDING);
        request.setReason(reason);
        return request;
    }
}