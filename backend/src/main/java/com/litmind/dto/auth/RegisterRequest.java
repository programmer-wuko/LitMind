package com.litmind.dto.auth;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class RegisterRequest {
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 100, message = "密码长度必须在6-100个字符之间")
    private String password;

    private String email;

    private String nickname;

    // 部门ID（如果选择现有部门）
    private Long departmentId;

    // 新部门名称（如果创建新部门）
    private String newDepartmentName;
    
    // 注意：departmentId 和 newDepartmentName 至少需要一个，但这个验证在Service层进行
}

