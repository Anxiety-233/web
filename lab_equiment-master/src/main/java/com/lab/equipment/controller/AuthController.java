package com.lab.equipment.controller;

import com.lab.equipment.dto.LoginDTO;
import com.lab.equipment.dto.ResultDTO;
import com.lab.equipment.dto.TokenDTO;
import com.lab.equipment.util.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Resource
    private AuthenticationManager authenticationManager;

    @Resource
    private UserDetailsService userDetailsService;

    @Resource
    private JwtUtil jwtUtil;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ResultDTO<TokenDTO> login(@RequestBody LoginDTO dto) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword())
        );
        UserDetails userDetails = userDetailsService.loadUserByUsername(dto.getUsername());
        String accessToken = jwtUtil.generateAccessToken(userDetails);
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);
        return ResultDTO.success(new TokenDTO(accessToken, refreshToken));
    }

    /**
     * 刷新AccessToken
     */
    @PostMapping("/refresh-token")
    public ResultDTO<TokenDTO> refreshToken(HttpServletRequest request, @RequestParam String username) {
        String refreshToken = request.getHeader("refresh-token");
        if (jwtUtil.validateRefreshToken(username, refreshToken)) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            String newAccessToken = jwtUtil.generateAccessToken(userDetails);
            return ResultDTO.success(new TokenDTO(newAccessToken, refreshToken));
        }
        return ResultDTO.error("RefreshToken无效");
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public ResultDTO<?> logout(@RequestHeader("Authorization") String token) {
        String accessToken = token.replace("Bearer ", "");
        String username = jwtUtil.getUsernameFromToken(accessToken);
        // 加入黑名单
        jwtUtil.addToBlacklist(accessToken);
        // 删除RefreshToken
        jwtUtil.removeRefreshToken(username);
        return ResultDTO.success("登出成功");
    }
}