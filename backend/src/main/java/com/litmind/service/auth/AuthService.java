package com.litmind.service.auth;

import com.litmind.common.exception.BusinessException;
import com.litmind.common.util.SecurityUtil;
import com.litmind.dto.auth.LoginRequest;
import com.litmind.dto.auth.LoginResponse;
import com.litmind.dto.auth.RegisterRequest;
import com.litmind.dto.auth.UserInfoResponse;
import com.litmind.model.entity.Department;
import com.litmind.model.entity.User;
import com.litmind.repository.UserRepository;
import com.litmind.service.department.DepartmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final DepartmentService departmentService;
    private final SecurityUtil securityUtil;

    public LoginResponse login(LoginRequest request) {
        // 临时方案：硬编码验证（不依赖数据库）
        // TODO: 生产环境请移除此代码，使用数据库验证
        if ("admin".equals(request.getUsername()) && "admin123".equals(request.getPassword())) {
            log.info("使用硬编码验证登录成功: admin");
            UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                    .username("admin")
                    .password("$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwK8pJ5C") // 占位密码，不会验证
                    .authorities("ROLE_USER")
                    .build();
            String token = jwtService.generateToken(userDetails, 1L); // 使用固定userId: 1
            return new LoginResponse(token, "admin", 1L);
        }
        
        // 数据库验证（如果数据库可用）
        try {
            User user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new BusinessException(404, "用户不存在"));
            
            try {
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.getUsername(),
                                request.getPassword()
                        )
                );
            } catch (Exception e) {
                log.error("登录失败: username={}, error={}", request.getUsername(), e.getMessage());
                throw new BusinessException(401, "用户名或密码错误");
            }

            UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                    .username(user.getUsername())
                    .password(user.getPassword())
                    .authorities("ROLE_USER")
                    .build();

            String token = jwtService.generateToken(userDetails, user.getId());
            log.info("用户登录成功: username={}, userId={}", user.getUsername(), user.getId());

            return new LoginResponse(token, user.getUsername(), user.getId());
        } catch (Exception e) {
            // 如果数据库不可用，且不是admin/admin123，返回错误
            log.warn("数据库验证失败，且不是默认账号: username={}", request.getUsername());
            throw new BusinessException(401, "用户名或密码错误");
        }
    }

    @Transactional
    public LoginResponse register(RegisterRequest request) {
        try {
            log.info("开始注册用户: username={}, departmentId={}, newDepartmentName={}", 
                    request.getUsername(), request.getDepartmentId(), request.getNewDepartmentName());
            
            // 检查用户名是否已存在
            if (userRepository.existsByUsername(request.getUsername())) {
                log.warn("注册失败: 用户名已存在 - {}", request.getUsername());
                throw new BusinessException(400, "用户名已存在");
            }

            // 检查邮箱是否已存在（如果提供了邮箱）
            if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
                if (userRepository.existsByEmail(request.getEmail())) {
                    log.warn("注册失败: 邮箱已被注册 - {}", request.getEmail());
                    throw new BusinessException(400, "邮箱已被注册");
                }
            }

            // 处理部门
            Long departmentId = null;
            if (request.getDepartmentId() != null) {
                // 使用现有部门
                try {
                    Department department = departmentService.getDepartmentById(request.getDepartmentId());
                    departmentId = department.getId();
                    log.info("使用现有部门: departmentId={}, name={}", departmentId, department.getName());
                } catch (BusinessException e) {
                    log.error("部门不存在: departmentId={}", request.getDepartmentId());
                    throw new BusinessException(400, "选择的部门不存在");
                }
            } else if (request.getNewDepartmentName() != null && !request.getNewDepartmentName().trim().isEmpty()) {
                // 创建新部门
                try {
                    Department department = departmentService.findOrCreateDepartment(request.getNewDepartmentName().trim());
                    departmentId = department.getId();
                    log.info("创建/使用新部门: departmentId={}, name={}", departmentId, department.getName());
                } catch (Exception e) {
                    log.error("创建部门失败: name={}, error={}", request.getNewDepartmentName(), e.getMessage(), e);
                    throw new BusinessException(400, "创建部门失败: " + e.getMessage());
                }
            } else {
                log.warn("注册失败: 未选择部门也未创建新部门");
                throw new BusinessException(400, "请选择部门或创建新部门");
            }

            // 创建用户
            User user = new User();
            user.setUsername(request.getUsername());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setEmail(request.getEmail());
            user.setNickname(request.getNickname());
            user.setDepartmentId(departmentId);

            user = userRepository.save(user);
            log.info("用户注册成功: username={}, userId={}, departmentId={}", 
                    user.getUsername(), user.getId(), user.getDepartmentId());

            // 自动登录
            UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                    .username(user.getUsername())
                    .password(user.getPassword())
                    .authorities("ROLE_USER")
                    .build();

            String token = jwtService.generateToken(userDetails, user.getId());
            return new LoginResponse(token, user.getUsername(), user.getId());
        } catch (BusinessException e) {
            // 重新抛出业务异常
            throw e;
        } catch (Exception e) {
            log.error("注册过程中发生未知错误: username={}, error={}", request.getUsername(), e.getMessage(), e);
            throw new BusinessException(500, "注册失败: " + e.getMessage());
        }
    }

    /**
     * 获取当前用户信息（包括部门信息）
     */
    public UserInfoResponse getCurrentUserInfo(Authentication authentication) {
        try {
            Long userId = securityUtil.getUserId(authentication);
            if (userId == null) {
                log.warn("获取用户ID失败: authentication={}", authentication);
                throw new BusinessException(401, "未登录或用户不存在");
            }

            log.debug("获取用户信息: userId={}", userId);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> {
                        log.error("用户不存在: userId={}", userId);
                        return new BusinessException(404, "用户不存在");
                    });

            log.debug("用户信息: userId={}, username={}, departmentId={}", 
                    user.getId(), user.getUsername(), user.getDepartmentId());

            UserInfoResponse response = new UserInfoResponse();
            response.setId(user.getId());
            response.setUsername(user.getUsername());
            response.setEmail(user.getEmail());
            response.setNickname(user.getNickname());
            response.setDepartmentId(user.getDepartmentId());

            // 获取部门名称
            if (user.getDepartmentId() != null) {
                try {
                    Department department = departmentService.getDepartmentById(user.getDepartmentId());
                    response.setDepartmentName(department.getName());
                    log.debug("部门信息获取成功: departmentId={}, departmentName={}", 
                            department.getId(), department.getName());
                } catch (BusinessException e) {
                    log.error("获取部门信息失败: departmentId={}, error={}", 
                            user.getDepartmentId(), e.getMessage());
                    response.setDepartmentName("未知部门");
                } catch (Exception e) {
                    log.error("获取部门信息时发生异常: departmentId={}, error={}", 
                            user.getDepartmentId(), e.getMessage(), e);
                    response.setDepartmentName("未知部门");
                }
            } else {
                log.warn("用户没有分配部门: userId={}, username={}", user.getId(), user.getUsername());
                response.setDepartmentName("未分配部门");
            }

            return response;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取用户信息时发生未知错误: error={}", e.getMessage(), e);
            throw new BusinessException(500, "获取用户信息失败: " + e.getMessage());
        }
    }
}

