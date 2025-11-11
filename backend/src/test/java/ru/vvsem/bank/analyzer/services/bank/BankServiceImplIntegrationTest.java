package ru.vvsem.bank.analyzer.services.bank;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import ru.vvsem.bank.analyzer.dto.bank.BankDto;
import ru.vvsem.bank.analyzer.mappers.BankMapperImpl;
import ru.vvsem.bank.analyzer.models.Bank;
import ru.vvsem.bank.analyzer.providers.EntityAccessProviderImpl;
import ru.vvsem.bank.analyzer.repositories.BankRepository;
import ru.vvsem.bank.analyzer.repositories.BaseRepositoryTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestPropertySource("classpath:application.yml")
@Import({
        BankServiceImpl.class,
        BankMapperImpl.class,
        EntityAccessProviderImpl.class
})
class BankServiceImplIntegrationTest extends BaseRepositoryTest {

    @Autowired
    private BankService bankService;

    @Autowired
    private BankRepository bankRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EntityAccessProviderImpl entityAccessProvider;

    private Bank bank1;
    private Bank bank2;

    @BeforeEach
    void setUp() {
        // Очищаем базу перед каждым тестом
        bankRepository.deleteAll();

        // Создаём тестовые банки
        bank1 = new Bank();
        bank1.setName("Сбербанк");
        bank1.setBankCode("sber");
        bank1.setBic("044525225");
        bank1 = entityManager.persistAndFlush(bank1);

        bank2 = new Bank();
        bank2.setName("Тинькофф");
        bank2.setBankCode("tinkoff");
        bank2.setBic("044525974");
        bank2 = entityManager.persistAndFlush(bank2);
    }

    @Test
    @DisplayName("Должен вернуть все банки")
    void shouldReturnAllBanks() {
        // When
        List<BankDto> result = bankService.getBanks();

        // Then
        assertThat(result).hasSize(2);

        // Проверяем первый банк
        BankDto firstBank = result.get(0);
        assertThat(firstBank.getId()).isEqualTo(bank1.getId());
        assertThat(firstBank.getName()).isEqualTo("Сбербанк");
        assertThat(firstBank.getBankCode()).isEqualTo("sber");
        assertThat(firstBank.getBic()).isEqualTo("044525225");

        // Проверяем второй банк
        BankDto secondBank = result.get(1);
        assertThat(secondBank.getId()).isEqualTo(bank2.getId());
        assertThat(secondBank.getName()).isEqualTo("Тинькофф");
        assertThat(secondBank.getBankCode()).isEqualTo("tinkoff");
        assertThat(secondBank.getBic()).isEqualTo("044525974");
    }

    @Test
    @DisplayName("Должен вернуть пустой список когда банков нет")
    void shouldReturnEmptyListWhenNoBanks() {
        // Given
        bankRepository.deleteAll();

        // When
        List<BankDto> result = bankService.getBanks();

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Должен корректно маппить сущность Bank в BankDto")
    void shouldCorrectlyMapBankToBankDto() {
        // When
        List<BankDto> result = bankService.getBanks();
        BankDto bankDto = result.stream()
                .filter(b -> b.getId().equals(bank1.getId()))
                .findFirst()
                .orElseThrow();

        // Then
        assertThat(bankDto).isNotNull();
        assertThat(bankDto.getId()).isEqualTo(bank1.getId());
        assertThat(bankDto.getName()).isEqualTo(bank1.getName());
        assertThat(bankDto.getBankCode()).isEqualTo(bank1.getBankCode());
        assertThat(bankDto.getBic()).isEqualTo(bank1.getBic());

        // Проверяем, что все поля заполнены
        assertThat(bankDto.getId()).isNotNull();
        assertThat(bankDto.getName()).isNotNull();
        assertThat(bankDto.getBankCode()).isNotNull();
        assertThat(bankDto.getBic()).isNotNull();
    }

    @Test
    @DisplayName("Должен возвращать банки в порядке их создания")
    void shouldReturnBanksInCreationOrder() {
        // When
        List<BankDto> result = bankService.getBanks();

        // Then
        assertThat(result).hasSize(2);
        // Проверяем порядок (должен соответствовать порядку вставки в @BeforeEach)
        assertThat(result.get(0).getName()).isEqualTo("Сбербанк");
        assertThat(result.get(1).getName()).isEqualTo("Тинькофф");
    }
}