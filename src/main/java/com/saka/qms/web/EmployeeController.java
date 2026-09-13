package com.saka.qms.web;

import com.saka.qms.model.Employee;
import com.saka.qms.repository.EmployeeRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController extends CrudController<Employee, Integer> {
    public EmployeeController(EmployeeRepository repository) {
        super(repository);
    }
}
