package ru.vvsem.bank.analyzer.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.vvsem.bank.analyzer.models.Category;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByUserId(long userId);
}