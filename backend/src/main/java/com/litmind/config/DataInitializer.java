package com.litmind.config;

import com.litmind.model.entity.Department;
import com.litmind.model.entity.User;
import com.litmind.repository.DepartmentRepository;
import com.litmind.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // 1. 创建默认部门（如果不存在）
        Department defaultDepartment = departmentRepository.findByName("默认部门")
                .orElseGet(() -> {
                    log.info("创建默认部门...");
                    Department dept = new Department();
                    dept.setName("默认部门");
                    dept.setDescription("系统默认部门");
                    Department saved = departmentRepository.save(dept);
                    log.info("默认部门创建成功: id={}, name={}", saved.getId(), saved.getName());
                    return saved;
                });

        // 2. 检查是否已有admin用户
        if (!userRepository.existsByUsername("admin")) {
            log.info("创建默认管理员账户...");
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setEmail("admin@litmind.com");
            admin.setNickname("管理员");
            admin.setDepartmentId(defaultDepartment.getId()); // 设置默认部门
            userRepository.save(admin);
            log.info("默认管理员账户创建成功: admin / admin123, departmentId={}", defaultDepartment.getId());
        } else {
            // 如果admin用户已存在但没有部门，则分配默认部门
            User admin = userRepository.findByUsername("admin").orElse(null);
            if (admin != null && admin.getDepartmentId() == null) {
                log.info("为admin用户分配默认部门...");
                admin.setDepartmentId(defaultDepartment.getId());
                userRepository.save(admin);
                log.info("admin用户已分配默认部门: departmentId={}", defaultDepartment.getId());
            } else {
                log.info("默认管理员账户已存在");
            }
        }
    }
}

