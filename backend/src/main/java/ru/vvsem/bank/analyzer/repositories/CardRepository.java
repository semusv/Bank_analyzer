package ru.vvsem.bank.analyzer.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.vvsem.bank.analyzer.models.Card;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {
    Optional<Card> findByIdAndAccountUserId(Long cardId, Long userId);

    List<Card> findByAccountId(long accountId);

    List<Card> findByIssuerBankId(long issuerBankId);

    List<Card>  findByAccountUserId(long userId);
}