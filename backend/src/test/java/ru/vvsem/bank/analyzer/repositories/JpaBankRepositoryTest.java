package ru.vvsem.bank.analyzer.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.vvsem.bank.analyzer.models.Bank;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("Репозиторий банков")
class JpaBankRepositoryTest {

    @Autowired
    private BankRepository bankRepository;

    @Autowired
    private TestEntityManager em;

    private Bank testBank;

    @BeforeEach
    void setUp() {
        testBank = new Bank();
        testBank.setName("МойБанк");
        testBank.setBic("999999999");
        testBank.setLogoUrl("https://example.com/logo.png");
        em.persist(testBank);
        em.flush();
    }

    @Test
    @DisplayName("Find by ID")
    void shouldFindById() {
        //given
        //when
        Bank found = bankRepository.findById(testBank.getId()).orElse(null);
        //then
        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("МойБанк");
    }

    @Test
    @DisplayName("Create new bank")
    void shouldCreateBank() {
        //given
        Bank newBank = new Bank();
        newBank.setName("МойБанк2");
        newBank.setBic("999999991");
        Bank saved = bankRepository.save(newBank);
        em.flush();
        em.clear();
        //when
        Bank found = em.find(Bank.class, saved.getId());
        //then
        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo(newBank.getName());
    }

    @Test
    @DisplayName("Update bank")
    void shouldUpdateBank() {
        //given
        testBank.setName("МойБанк Updated");
        bankRepository.save(testBank);
        em.flush();
        em.clear();
        //when
        Bank updated = em.find(Bank.class, testBank.getId());
        //then
        assertThat(updated.getName()).isEqualTo("МойБанк Updated");
    }

    @Test
    @DisplayName("Delete bank")
    void shouldDeleteBank() {
        //given
        bankRepository.delete(testBank);
        em.flush();
        em.clear();
        //when
        Bank deleted = em.find(Bank.class, testBank.getId());
        //then
        assertThat(deleted).isNull();
    }
}