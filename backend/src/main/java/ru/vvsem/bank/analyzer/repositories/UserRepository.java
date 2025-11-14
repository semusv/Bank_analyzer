package ru.vvsem.bank.analyzer.repositories;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.vvsem.bank.analyzer.models.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByLogin(String login);

    boolean existsByEmail(String email);

    @EntityGraph("user-accounts-category-budget-graph")
    Optional<User> findDataByLogin(String login);

    @EntityGraph("user-roles-graph")
    Optional<User> findByLogin(String logn);
}