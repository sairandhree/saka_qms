package com.saka.qms.repository;

import com.saka.qms.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Integer> {
    Optional<Employee> findByUsername(String username);
    List<Employee> findAllByIsDeletedFalse();
}
