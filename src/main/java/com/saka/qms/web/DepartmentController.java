package com.saka.qms.web;

import com.saka.qms.model.Department;
import com.saka.qms.repository.DepartmentRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController extends CrudController<Department, Integer> {
    public DepartmentController(DepartmentRepository repository) {
        super(repository);
    }
}
