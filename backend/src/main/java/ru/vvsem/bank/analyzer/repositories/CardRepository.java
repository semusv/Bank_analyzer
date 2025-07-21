package ru.vvsem.bank.analyzer.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.vvsem.bank.analyzer.models.Card;

public interface CardRepository extends JpaRepository<Card, Long> {
}