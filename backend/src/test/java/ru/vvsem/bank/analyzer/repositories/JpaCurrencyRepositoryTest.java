package ru.vvsem.bank.analyzer.repositories;

import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.vvsem.bank.analyzer.models.Currency;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("Репозиторий для работы с валютой")
class JpaCurrencyRepositoryTest {

    @Autowired
    private CurrencyRepository currencyRepository;

    @Autowired
    private TestEntityManager em;

    private Currency testCurrency;

    @BeforeEach
    void setUp() {
        testCurrency = new Currency();
        testCurrency.setCode("Val");
        testCurrency.setSymbol("V");
        testCurrency.setName("Валюта");
        em.persist(testCurrency);
        em.flush();
    }

    @Test
    @DisplayName("Find currency by code")
    void shouldFindByCode() {
        //given
        //when
        Optional<Currency> found = currencyRepository.findByCode(testCurrency.getCode());
        //then
        assertThat(found).isPresent().get()
                .satisfies(currency ->
                        assertThat(currency.getSymbol())
                                .isEqualTo(testCurrency.getSymbol()));
    }
}