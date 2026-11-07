package br.com.bora.delivery.api;

import br.com.bora.delivery.api.dto.AuthDtos.AccountDto;
import br.com.bora.delivery.api.dto.AuthDtos.LoginRequest;
import br.com.bora.delivery.api.dto.AuthDtos.RegisterRequest;
import br.com.bora.delivery.api.dto.AuthDtos.SessionDto;
import br.com.bora.delivery.api.dto.AuthDtos.UpdateProfileRequest;
import br.com.bora.delivery.security.AppPrincipal;
import br.com.bora.delivery.security.SessionTokenService;
import br.com.bora.delivery.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final SessionTokenService tokenService;

    public AuthController(AuthService authService, SessionTokenService tokenService) {
        this.authService = authService;
        this.tokenService = tokenService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public SessionDto register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public SessionDto login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public AccountDto me(@AuthenticationPrincipal AppPrincipal principal) {
        return authService.getAccount(principal.id());
    }

    @PutMapping("/me")
    public AccountDto updateProfile(@Valid @RequestBody UpdateProfileRequest request,
                                    @AuthenticationPrincipal AppPrincipal principal) {
        return authService.updateProfile(principal.id(), request);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@RequestHeader(name = "Authorization", required = false) String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            tokenService.revoke(authorization.substring(7));
        }
    }
}
