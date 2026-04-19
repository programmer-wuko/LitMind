package com.litmind.service.department.impl;

import com.litmind.common.exception.BusinessException;
import com.litmind.model.entity.Department;
import com.litmind.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import com.litmind.service.department.DepartmentService;

@Service
@RequiredArgsConstructor
@Slf4j
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;

    @Override
    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    @Override
    public Department getDepartmentById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "部门不存在"));
    }

    @Override
    public Department getDepartmentByName(String name) {
        return departmentRepository.findByName(name)
                .orElseThrow(() -> new BusinessException(404, "部门不存在"));
    }

    @Override
    @Transactional
    public Department createDepartment(String name, String description) {
        if (departmentRepository.findByName(name).isPresent()) {
            throw new BusinessException(400, "部门名称已存在");
        }

        Department department = new Department();
        department.setName(name);
        department.setDescription(description);

        return departmentRepository.save(department);
    }

    @Override
    @Transactional
    public Department findOrCreateDepartment(String name) {
        return departmentRepository.findByName(name)
                .orElseGet(() -> {
                    Department newDepartment = new Department();
                    newDepartment.setName(name);
                    return departmentRepository.save(newDepartment);
                });
    }
}
