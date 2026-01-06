package com.litmind.service.department;

import com.litmind.common.exception.BusinessException;
import com.litmind.model.entity.Department;
import com.litmind.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    /**
     * 获取所有部门列表
     */
    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    /**
     * 根据ID获取部门
     */
    public Department getDepartmentById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "部门不存在"));
    }

    /**
     * 根据名称获取部门
     */
    public Department getDepartmentByName(String name) {
        return departmentRepository.findByName(name)
                .orElseThrow(() -> new BusinessException(404, "部门不存在"));
    }

    /**
     * 创建新部门
     */
    @Transactional
    public Department createDepartment(String name, String description) {
        if (departmentRepository.existsByName(name)) {
            throw new BusinessException(400, "部门名称已存在");
        }

        Department department = new Department();
        department.setName(name);
        department.setDescription(description);
        
        Department saved = departmentRepository.save(department);
        log.info("创建部门成功: id={}, name={}", saved.getId(), saved.getName());
        return saved;
    }

    /**
     * 根据名称查找或创建部门
     */
    @Transactional
    public Department findOrCreateDepartment(String name) {
        return departmentRepository.findByName(name)
                .orElseGet(() -> {
                    Department department = new Department();
                    department.setName(name);
                    Department saved = departmentRepository.save(department);
                    log.info("自动创建部门: id={}, name={}", saved.getId(), saved.getName());
                    return saved;
                });
    }
}

