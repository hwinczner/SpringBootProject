package com.SpringBoot.Project.Repositories;

import com.SpringBoot.Project.Models.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeaveRequestInterface extends JpaRepository<LeaveRequest, Long> {
}
