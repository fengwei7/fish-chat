package com.fish.chat.core.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fish.chat.common.exception.BusinessException;
import com.fish.chat.core.dto.AuthResponse;
import com.fish.chat.core.dto.LoginRequest;
import com.fish.chat.core.entity.User;
import com.fish.chat.core.mapper.mysql.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 认证服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    
    @Mock
    private UserMapper userMapper;
    
    @InjectMocks
    private AuthService authService;
    
    private LoginRequest loginRequest;
    private User testUser;
    
    @BeforeEach
    void setUp() {
        loginRequest = LoginRequest.builder()
                .username("testuser")
                .password("password123")
                .build();
        
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword(DigestUtil.sha256Hex("password123" + "salt123"));
        testUser.setSalt("salt123");
        testUser.setStatus(1);
        testUser.setNickname("Test User");
    }
    
    @Test
    void testLogin_Success() {
        // Given
        when(userMapper.selectOne(any(QueryWrapper.class))).thenReturn(testUser);
        
        // When
        AuthResponse response = authService.login(loginRequest);
        
        // Then
        assertNotNull(response);
        assertEquals(1L, response.getUserId());
        assertEquals("testuser", response.getUsername());
        verify(userMapper).selectOne(any(QueryWrapper.class));
    }
    
    @Test
    void testLogin_UserNotFound() {
        // Given
        when(userMapper.selectOne(any(QueryWrapper.class))).thenReturn(null);
        
        // When & Then
        assertThrows(BusinessException.class, () -> {
            authService.login(loginRequest);
        });
    }
    
    @Test
    void testLogin_WrongPassword() {
        // Given
        User wrongPasswordUser = new User();
        wrongPasswordUser.setPassword("wronghash");
        when(userMapper.selectOne(any(QueryWrapper.class))).thenReturn(wrongPasswordUser);
        
        // When & Then
        assertThrows(BusinessException.class, () -> {
            authService.login(loginRequest);
        });
    }
    
    @Test
    void testLogin_UserDisabled() {
        // Given
        testUser.setStatus(0); // Disabled
        when(userMapper.selectOne(any(QueryWrapper.class))).thenReturn(testUser);
        
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            authService.login(loginRequest);
        });
        assertEquals("账户已被禁用", exception.getMessage());
    }
}
