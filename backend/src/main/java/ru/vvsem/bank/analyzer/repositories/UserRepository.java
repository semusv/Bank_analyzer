package ru.vvsem.bank.analyzer.repositories;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.vvsem.bank.analyzer.models.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    @EntityGraph("user-accounts-category-budget-graph")
    Optional<User> findByLogin(String login);
}