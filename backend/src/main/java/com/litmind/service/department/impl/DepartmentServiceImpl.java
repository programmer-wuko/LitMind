package com.litmind.service.department.impl;

import com.litmind.common.exception.BusinessException;
import com.litmind.model.dto.DepartmentDTO;
import com.litmind.model.entity.Department;
import com.litmind.model.request.DepartmentCreateRequest;
import com.litmind.model.request.DepartmentUpdateRequest;
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
    public List<DepartmentDTO> getAllDepartments() {
        List<Department> departments = departmentRepository.findAll();
        return departments.stream().map(this::convertToDTO).toList();
    }

    @Override
    public DepartmentDTO getDepartmentById(Long departmentId) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new BusinessException(404, "部门不存在"));
        return convertToDTO(department);
    }

    @Override
    @Transactional
    public DepartmentDTO createDepartment(DepartmentCreateRequest request) {
        // 检查部门名称是否已存在
        if (departmentRepository.findByName(request.getName()).isPresent()) {
            throw new BusinessException(400, "部门名称已存在");
        }

        Department department = new Department();
        department.setName(request.getName());
        department.setDescription(request.getDescription());

        Department savedDepartment = departmentRepository.save(department);
        return convertToDTO(savedDepartment);
    }

    @Override
    @Transactional
    public DepartmentDTO updateDepartment(Long departmentId, DepartmentUpdateRequest request) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new BusinessException(404, "部门不存在"));

        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            // 检查部门名称是否已存在
            if (departmentRepository.findByName(request.getName()).isPresent()) {
                throw new BusinessException(400, "部门名称已存在");
            }
            department.setName(request.getName());
        }

        if (request.getDescription() != null) {
            department.setDescription(request.getDescription());
        }

        Department updatedDepartment = departmentRepository.save(department);
        return convertToDTO(updatedDepartment);
    }

    @Override
    @Transactional
    public void deleteDepartment(Long departmentId) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new BusinessException(404, "部门不存在"));
        departmentRepository.delete(department);
    }

    private DepartmentDTO convertToDTO(Department department) {
        DepartmentDTO dto = new DepartmentDTO();
        dto.setId(department.getId());
        dto.setName(department.getName());
        dto.setDescription(department.getDescription());
        dto.setCreatedAt(department.getCreatedAt());
        dto.setUpdatedAt(department.getUpdatedAt());
        return dto;
    }
}
