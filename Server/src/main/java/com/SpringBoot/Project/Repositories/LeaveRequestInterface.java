package com.SpringBoot.Project.Repositories;

import com.SpringBoot.Project.Models.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LeaveRequestInterface extends JpaRepository<LeaveRequest, Long> {

    // Query to find overlapping leave requests
    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employee.id = :employeeId " +
            "AND ((lr.startDate BETWEEN :startDate AND :endDate) OR (lr.endDate BETWEEN :startDate AND :endDate)) " +
            "AND lr.status = 'Approved'")
    List<LeaveRequest> findOverlappingLeaveRequests(Long employeeId, java.util.Date startDate, java.util.Date endDate);
}
