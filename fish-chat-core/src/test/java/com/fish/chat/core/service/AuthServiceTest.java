package com.fish.chat.core.service;

import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fish.chat.common.exception.BusinessException;
import com.fish.chat.core.entity.po.UserPO;
import com.fish.chat.core.entity.req.LoginRequest;
import com.fish.chat.core.entity.resp.AuthResponse;
import com.fish.chat.core.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    private UserPO testUserPO;
    
    @BeforeEach
    void setUp() {
        loginRequest = LoginRequest.builder()
                .username("testuser")
                .password("password123")
                .build();
        
        testUserPO = new UserPO();
        testUserPO.setId(1L);
        testUserPO.setUsername("testuser");
        testUserPO.setPassword(DigestUtil.sha256Hex("password123" + "salt123"));
        testUserPO.setSalt("salt123");
        testUserPO.setStatus(1);
        testUserPO.setNickname("Test User");
    }
    
    @Test
    void testLogin_Success() {
        // Given
        when(userMapper.selectOne(any(QueryWrapper.class))).thenReturn(testUserPO);
        
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
        UserPO wrongPasswordUserPO = new UserPO();
        wrongPasswordUserPO.setPassword("wronghash");
        when(userMapper.selectOne(any(QueryWrapper.class))).thenReturn(wrongPasswordUserPO);
        
        // When & Then
        assertThrows(BusinessException.class, () -> {
            authService.login(loginRequest);
        });
    }
    
    @Test
    void testLogin_UserDisabled() {
        // Given
        testUserPO.setStatus(0); // Disabled
        when(userMapper.selectOne(any(QueryWrapper.class))).thenReturn(testUserPO);
        
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            authService.login(loginRequest);
        });
        assertEquals("账户已被禁用", exception.getMessage());
    }
}
