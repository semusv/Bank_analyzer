package ru.vvsem.bank.analyzer.services.currency;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.vvsem.bank.analyzer.dto.currency.CurrencyDto;
import ru.vvsem.bank.analyzer.exceptions.EntityNotFoundException;
import ru.vvsem.bank.analyzer.mappers.CurrencyMapperImpl;
import ru.vvsem.bank.analyzer.models.Bank;
import ru.vvsem.bank.analyzer.models.BankAccount;
import ru.vvsem.bank.analyzer.models.Card;
import ru.vvsem.bank.analyzer.models.Currency;
import ru.vvsem.bank.analyzer.models.User;
import ru.vvsem.bank.analyzer.providers.EntityAccessProvider;
import ru.vvsem.bank.analyzer.providers.EntityAccessProviderImpl;
import ru.vvsem.bank.analyzer.repositories.BaseRepositoryTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@Testcontainers
@TestPropertySource("classpath:application.yml")
@Import({
        CurrencyServiceImpl.class,
        CurrencyMapperImpl.class,
        EntityAccessProviderImpl.class
})
class CurrencyServiceIntegrationTest extends BaseRepositoryTest {

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EntityAccessProvider entityAccessProvider;

    private Currency currencyRub;
    private Currency currencyUsd;
    private Currency currencyEur;

    private User user;
    private Bank bank;
    private BankAccount account1;
    private BankAccount account2;
    private Card existingCard;

    @BeforeEach
    void setUp() {
        // Создаём валюты
        currencyRub = new Currency("RUB", "₽", "Russian Ruble");
        currencyUsd = new Currency("USD", "$", "US Dollar");
        currencyEur = new Currency("EUR", "€", "Euro");

        entityManager.persistAndFlush(currencyRub);
        entityManager.persistAndFlush(currencyUsd);
        entityManager.persistAndFlush(currencyEur);


        // Создаём пользователя
        user = new User();
        user.setLogin("testuser");
        user.setEmail("test@example.com");
        user.setPassword("pass");
        user.setName("Test");
        user.setSurname("User");
        user = entityManager.persistAndFlush(user);


        // Создаём банк
        bank = new Bank();
        bank.setName("Sberbank");
        bank.setBankCode("sber");
        bank.setBic("11111111");
        bank = entityManager.persistAndFlush(bank);

        // Создаём счета
        account1 = new BankAccount();
        account1.setName("Main Account");
        account1.setAccountNumber("40817810099910000001");
        account1.setBalance(new java.math.BigDecimal("1000.00"));
        account1.setUser(user);
        account1.setCurrency(currencyRub);
        account1.setBank(bank);
        account1 = entityManager.persistAndFlush(account1);

        account2 = new BankAccount();
        account2.setName("Secondary Account");
        account2.setAccountNumber("40817810099910000002");
        account2.setBalance(new java.math.BigDecimal("500.00"));
        account2.setUser(user);
        account2.setCurrency(currencyRub);
        account2.setBank(bank);
        account2 = entityManager.persistAndFlush(account2);

        // Создаём существующую карту
        existingCard = new Card();
        existingCard.setCardName("Existing Card");
        existingCard.setLastFourDigits("1234");
        existingCard.setAccount(account1);
        existingCard.setIssuerBank(bank);
        existingCard = entityManager.persistAndFlush(existingCard);

        entityManager.flush();
        entityManager.clear();

    }

    @Test
    @DisplayName("Должен вернуть все валюты")
    void shouldReturnAllCurrencies() {
        // Given
        entityManager.clear();

        // When
        List<CurrencyDto> result = currencyService.getAllCurrencyDto();

        // Then
        assertThat(result).hasSize(3);
        assertThat(result)
                .extracting(CurrencyDto::getCode)
                .containsExactlyInAnyOrder("RUB", "USD", "EUR");
    }

    @Test
    @DisplayName("Должен вернуть пустой список когда валют нет")
    void shouldReturnEmptyListWhenNoCurrencies() {
        // Given
        // Удаляем все валюты
        entityManager.getEntityManager().createQuery("DELETE FROM Card ").executeUpdate();
        entityManager.getEntityManager().createQuery("DELETE FROM BankAccount ").executeUpdate();
        entityManager.getEntityManager().createQuery("DELETE FROM Currency").executeUpdate();

        // When
        List<CurrencyDto> result = currencyService.getAllCurrencyDto();

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Должен вернуть валюту по ID")
    void shouldReturnCurrencyByID() {
        // Given
        entityManager.clear();

        // When
        Currency result = currencyService.findById(currencyRub.getId());

        // Then
        assertThat(result.getId()).isEqualTo(currencyRub.getId());
        assertThat(result.getCode()).isEqualTo(currencyRub.getCode());
    }

    @Test
    @DisplayName("Должен вернуть исключение, если валюта не найдена по ID")
    void shouldReturnExceptionWhenCurrencyNotFound() {
        // Given
        entityManager.clear();

        // Given
        Long nonExistentId = 999L;

        // When
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> currencyService.findById(nonExistentId));

        assertThat(exception.getMessageCode()).isEqualTo("exception.entity.not.found.entity");
    }

    @Test
    @DisplayName("Должен вернуть исключение, если ID равен null")
    void shouldReturnExceptionWhenIdIsNull() {
        // Given
        entityManager.clear();

        // then
         assertThrows(IllegalArgumentException.class,
                () -> currencyService.findById(null));
    }

    @Test
    @DisplayName("Должен вернуть исключение, если ID карты равен null")
    void shouldReturnExceptionWhenCardIdIsNull() {
        // Given
        entityManager.clear();

        // then
        assertThrows(IllegalArgumentException.class,
                () -> currencyService.findByCardId(null));
    }

    @Test
    @DisplayName("Должен вернуть исключение, если по ID карты ничего не найдено")
    void shouldReturnExceptionWhenCardNotFound() {
        // Given
        entityManager.clear();
        Long nonExistentId = 999L;

        // then
        assertThrows(EntityNotFoundException.class,
                () -> currencyService.findByCardId(nonExistentId));
    }

    @Test
    @DisplayName("Должен вернуть валюту по ID карты")
    void shouldReturnCurrencyByCardId() {
        // Given
        entityManager.clear();

        //when
        Currency result = currencyService.findByCardId(existingCard.getId());

        // Then
        assertThat(result.getId()).isEqualTo(currencyRub.getId());
        assertThat(result.getCode()).isEqualTo(currencyRub.getCode());
    }


    @Test
    @DisplayName("Должен корректно маппить сущности в DTO")
    void shouldCorrectlyMapEntitiesToDto() {
        // When
        List<CurrencyDto> result = currencyService.getAllCurrencyDto();

        // Then
        CurrencyDto rubDto = result.stream()
                .filter(dto -> "RUB".equals(dto.getCode()))
                .findFirst()
                .orElseThrow();

        assertThat(rubDto.getCode()).isEqualTo("RUB");
        assertThat(rubDto.getSymbol()).isEqualTo("₽");
        assertThat(rubDto.getName()).isEqualTo("Russian Ruble");
    }
}