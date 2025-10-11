package com.fellps.apibank.service;

import com.fellps.apibank.dto.AuthResponse;
import com.fellps.apibank.dto.LoginRequest;
import com.fellps.apibank.dto.RegisterRequest;
import com.fellps.apibank.enums.AccountType;
import com.fellps.apibank.enums.Role;
import com.fellps.apibank.exception.BusinessException;
import com.fellps.apibank.model.Account;
import com.fellps.apibank.model.User;
import com.fellps.apibank.repository.UserRepository;
import com.fellps.apibank.security.JwtTokenProvider;
import com.fellps.apibank.util.AccountNumberGenerator;
import com.fellps.apibank.util.CpfValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final AuditService auditService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());

        // Validar CPF
        if (!CpfValidator.isValid(request.getCpf())) {
            throw new BusinessException("CPF inválido");
        }

        // Verificar se email já existe
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("E-mail já cadastrado");
        }

        // Verificar se CPF já existe
        if (userRepository.existsByCpf(request.getCpf())) {
            throw new BusinessException("CPF já cadastrado");
        }

        // Criar usuário
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .cpf(request.getCpf())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .active(true)
                .build();

        // Criar conta automática
        Account account = Account.builder()
                .accountNumber(AccountNumberGenerator.generateAccountNumber())
                .agency(AccountNumberGenerator.getDefaultAgency())
                .accountType(AccountType.CORRENTE)
                .balance(BigDecimal.ZERO)
                .active(true)
                .user(user)
                .build();

        user.addAccount(account);
        user = userRepository.save(user);

        // Registrar auditoria
        auditService.logUserCreation(user);

        log.info("User registered successfully with id: {}", user.getId());

        // Fazer login automático
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        String token = jwtTokenProvider.generateToken(authentication);

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        log.info("User login attempt with email: {}", request.getEmail());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("Usuário não encontrado"));

        String token = jwtTokenProvider.generateToken(authentication);

        log.info("User logged in successfully: {}", user.getId());

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }
}

