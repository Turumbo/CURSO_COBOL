package br.com.bora.delivery.security;

import br.com.bora.delivery.domain.Account;
import br.com.bora.delivery.repository.AccountRepository;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SessionTokenService {
    private static final long SESSION_LIFETIME_SECONDS = 12 * 60 * 60;
    private final AccountRepository accountRepository;
    private final ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<>();
    private final SecureRandom secureRandom = new SecureRandom();

    public SessionTokenService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public String create(Account account) {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        sessions.put(token, new Session(account.getId(), Instant.now().plusSeconds(SESSION_LIFETIME_SECONDS)));
        return token;
    }

    public AppPrincipal authenticate(String token) {
        Session session = sessions.get(token);
        if (session == null) return null;
        if (Instant.now().isAfter(session.expiresAt())) {
            sessions.remove(token);
            return null;
        }
        return accountRepository.findById(session.accountId()).map(AppPrincipal::from).orElse(null);
    }

    public void revoke(String token) {
        sessions.remove(token);
    }

    private record Session(Long accountId, Instant expiresAt) {
    }
}
