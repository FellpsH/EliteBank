package com.fellps.apibank.service;

import com.fellps.apibank.dto.AuthResponse;
import com.fellps.apibank.dto.RegisterRequest;
import com.fellps.apibank.exception.BusinessException;
import com.fellps.apibank.model.User;
import com.fellps.apibank.repository.UserRepository;
import com.fellps.apibank.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
                .name("João Silva")
                .email("joao@email.com")
                .cpf("52998224725") // CPF válido
                .password("senha123")
                .build();
    }

    @Test
    @DisplayName("Deve registrar usuário com sucesso")
    void shouldRegisterUserSuccessfully() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByCpf(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        
        User savedUser = User.builder()
                .id(1L)
                .name(registerRequest.getName())
                .email(registerRequest.getEmail())
                .cpf(registerRequest.getCpf())
                .password("encodedPassword")
                .build();
        
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        
        when(jwtTokenProvider.generateToken(any(Authentication.class))).thenReturn("token123");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(savedUser));

        // Act
        AuthResponse response = authService.register(registerRequest);

        // Assert
        assertNotNull(response);
        assertEquals("token123", response.getToken());
        assertEquals("Bearer", response.getType());
        assertEquals(savedUser.getId(), response.getUserId());
        assertEquals(savedUser.getName(), response.getName());
        
        verify(userRepository, times(1)).save(any(User.class));
        verify(auditService, times(1)).logUserCreation(any(User.class));
    }

    @Test
    @DisplayName("Não deve registrar usuário com CPF inválido")
    void shouldNotRegisterUserWithInvalidCpf() {
        // Arrange
        registerRequest.setCpf("12345678901"); // CPF inválido

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            authService.register(registerRequest);
        });

        assertEquals("CPF inválido", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Não deve registrar usuário com email já existente")
    void shouldNotRegisterUserWithExistingEmail() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            authService.register(registerRequest);
        });

        assertEquals("E-mail já cadastrado", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Não deve registrar usuário com CPF já existente")
    void shouldNotRegisterUserWithExistingCpf() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByCpf(anyString())).thenReturn(true);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            authService.register(registerRequest);
        });

        assertEquals("CPF já cadastrado", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }
}

