package com.lab.equipment.interceptor;
import com.lab.equipment.entity.Equipment;
import com.lab.equipment.entity.User;
import com.lab.equipment.service.EquipmentService;
import com.lab.equipment.service.UserService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 水平越权拦截器：普通用户只能操作自己的设备
 */
@Component
public class HorizontalPrivilegeInterceptor implements HandlerInterceptor {

    @Resource
    private EquipmentService equipmentService;

    @Resource
    private UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        // 管理员放行
        if ("admin".equals(username)) {
            return true;
        }

        // 普通用户校验设备归属
        String id = request.getParameter("id");
        if (id != null) {
            Equipment equipment = equipmentService.getById(Long.parseLong(id));
            if (equipment != null) {
                // 修复：获取当前登录用户ID，而非硬编码1L
                User currentUser = userService.getOne(com.baomidou.mybatisplus.core.toolkit.Wrappers.<User>lambdaQuery()
                        .eq(User::getUsername, username));
                if (currentUser == null || !equipment.getUserId().equals(currentUser.getId())) {
                    response.setStatus(403);
                    response.getWriter().write("无权操作他人设备");
                    return false;
                }
            }
        }
        return true;
    }
}