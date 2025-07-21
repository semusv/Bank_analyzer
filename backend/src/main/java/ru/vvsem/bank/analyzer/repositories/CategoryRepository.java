package ru.vvsem.bank.analyzer.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.vvsem.bank.analyzer.models.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}