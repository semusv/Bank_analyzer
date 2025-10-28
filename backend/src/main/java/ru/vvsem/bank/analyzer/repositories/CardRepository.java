package ru.vvsem.bank.analyzer.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.vvsem.bank.analyzer.models.Card;

import java.util.List;
import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, Long> {
    Optional<Card> findByIdAndAccount_User_Id(Long cardId, Long userId);

    List<Card> findByAccountId(long accountId);

    List<Card> findByIssuerBankId(long issuerBankId);

    List<Card>  findByAccountUserId(long userId);
}