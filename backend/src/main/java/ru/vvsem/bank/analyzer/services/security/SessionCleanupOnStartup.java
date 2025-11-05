package ru.vvsem.bank.analyzer.services.security;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SessionCleanupOnStartup {

    private final SessionRegistry sessionRegistry;

    @EventListener(ApplicationReadyEvent.class)
    public void cleanupSessionsOnStartup() {
        // Принудительно очищаем все сессии при старте
        sessionRegistry.getAllPrincipals().forEach(principal -> {
            sessionRegistry.getAllSessions(principal, false).forEach(session -> {
                session.expireNow();
            });
        });

        System.out.println("Все сессии очищены при старте приложения");
    }
}