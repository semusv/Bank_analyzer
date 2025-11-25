package ru.vvsem.bank.analyzer.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.vvsem.bank.analyzer.models.Currency;

import java.util.Optional;

@Repository
public interface CurrencyRepository extends JpaRepository<Currency, Long> {
    Optional<Currency> findByCode(String code);

    @Query("SELECT cur FROM Card c " +
           "JOIN c.account ba " +
           "JOIN ba.currency cur " +
           "WHERE c.id = :cardId")
    Optional<Currency> getCurrencyByCardId(Long cardId);
}