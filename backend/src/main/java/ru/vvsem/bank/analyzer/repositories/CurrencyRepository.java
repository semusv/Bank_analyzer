package ru.vvsem.bank.analyzer.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.vvsem.bank.analyzer.models.Currency;

import java.util.Optional;

public interface CurrencyRepository extends JpaRepository<Currency, Long> {
    Optional<Currency> findByCode(String code);

    @Query("SELECT cur FROM Card c " +
           "JOIN c.account ba " +         // ✅ Используем отношение
           "JOIN ba.currency cur " +      // ✅ Предполагаем, что в BankAccount есть поле currency
           "WHERE c.id = :cardId")
    Optional<Currency> getCurrencyByCardId(Long cardId);
}