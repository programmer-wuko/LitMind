package com.litmind.controller.department;

import com.litmind.common.response.ApiResponse;
import com.litmind.model.entity.Department;
import com.litmind.service.department.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    /**
     * 获取所有部门列表
     */
    @GetMapping
    public ApiResponse<List<Department>> getAllDepartments() {
        List<Department> departments = departmentService.getAllDepartments();
        return ApiResponse.success(departments);
    }

    /**
     * 根据ID获取部门
     */
    @GetMapping("/{id}")
    public ApiResponse<Department> getDepartmentById(@PathVariable Long id) {
        Department department = departmentService.getDepartmentById(id);
        return ApiResponse.success(department);
    }

    /**
     * 创建新部门
     */
    @PostMapping
    public ApiResponse<Department> createDepartment(@RequestBody Map<String, String> request) {
        String name = request.get("name");
        String description = request.get("description");
        
        if (name == null || name.trim().isEmpty()) {
            return ApiResponse.error(400, "部门名称不能为空");
        }
        
        Department department = departmentService.createDepartment(name.trim(), description);
        return ApiResponse.success("部门创建成功", department);
    }
}

