package com.litmind.service.department;

import com.litmind.model.entity.Department;

import java.util.List;

public interface DepartmentService {

    List<Department> getAllDepartments();

    Department getDepartmentById(Long id);

    Department getDepartmentByName(String name);

    Department createDepartment(String name, String description);

    Department findOrCreateDepartment(String name);
}
