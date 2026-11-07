package br.com.bora.delivery.service;

import br.com.bora.delivery.api.dto.AuthDtos.AccountDto;
import br.com.bora.delivery.api.dto.AuthDtos.LoginRequest;
import br.com.bora.delivery.api.dto.AuthDtos.RegisterRequest;
import br.com.bora.delivery.api.dto.AuthDtos.SessionDto;
import br.com.bora.delivery.api.dto.AuthDtos.UpdateProfileRequest;
import br.com.bora.delivery.domain.Account;
import br.com.bora.delivery.domain.AccountRole;
import br.com.bora.delivery.repository.AccountRepository;
import br.com.bora.delivery.security.SessionTokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AuthService {
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final SessionTokenService tokenService;

    public AuthService(AccountRepository accountRepository, PasswordEncoder passwordEncoder,
                       SessionTokenService tokenService) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    @Transactional
    public SessionDto register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (accountRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Já existe uma conta com este e-mail.");
        }
        Account account = new Account(
                request.name().trim(), email, passwordEncoder.encode(request.password()), AccountRole.CUSTOMER);
        account.updateProfile(account.getName(), account.getEmail(),
                normalizeOptional(request.phone()), null);
        accountRepository.save(account);
        return createSession(account);
    }

    @Transactional(readOnly = true)
    public SessionDto login(LoginRequest request) {
        Account account = accountRepository.findByEmailIgnoreCase(normalizeEmail(request.email()))
                .orElseThrow(() -> new IllegalArgumentException("E-mail ou senha incorretos."));
        if (!passwordEncoder.matches(request.password(), account.getPasswordHash())) {
            throw new IllegalArgumentException("E-mail ou senha incorretos.");
        }
        return createSession(account);
    }

    @Transactional(readOnly = true)
    public AccountDto getAccount(Long accountId) {
        return toAccountDto(accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Conta não encontrada.")));
    }

    @Transactional
    public AccountDto updateProfile(Long accountId, UpdateProfileRequest request) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Conta não encontrada."));
        String email = normalizeEmail(request.email());
        if (accountRepository.existsByEmailIgnoreCaseAndIdNot(email, accountId)) {
            throw new IllegalArgumentException("Este e-mail já está sendo usado por outra conta.");
        }
        account.updateProfile(request.name().trim(), email,
                normalizeOptional(request.phone()), normalizeOptional(request.deliveryAddress()));
        return toAccountDto(account);
    }

    private SessionDto createSession(Account account) {
        return new SessionDto(tokenService.create(account), toAccountDto(account));
    }

    private AccountDto toAccountDto(Account account) {
        return new AccountDto(account.getId(), account.getName(), account.getEmail(),
                account.getRole(), account.getPhone(), account.getDeliveryAddress());
    }

    private static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private static String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
