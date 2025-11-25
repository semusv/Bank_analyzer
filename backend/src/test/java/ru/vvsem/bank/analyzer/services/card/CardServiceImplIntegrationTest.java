package ru.vvsem.bank.analyzer.services.card;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import ru.vvsem.bank.analyzer.dto.card.CardDto;
import ru.vvsem.bank.analyzer.dto.card.NewCardDto;
import ru.vvsem.bank.analyzer.mappers.BankMapperImpl;
import ru.vvsem.bank.analyzer.mappers.CardMapperImpl;
import ru.vvsem.bank.analyzer.models.Bank;
import ru.vvsem.bank.analyzer.models.BankAccount;
import ru.vvsem.bank.analyzer.models.Card;
import ru.vvsem.bank.analyzer.models.Currency;
import ru.vvsem.bank.analyzer.models.SecurityUser;
import ru.vvsem.bank.analyzer.models.User;
import ru.vvsem.bank.analyzer.providers.EntityAccessProvider;
import ru.vvsem.bank.analyzer.providers.EntityAccessProviderImpl;
import ru.vvsem.bank.analyzer.repositories.BaseRepositoryTest;
import ru.vvsem.bank.analyzer.repositories.CardRepository;
import ru.vvsem.bank.analyzer.services.bank.BankService;
import ru.vvsem.bank.analyzer.services.bank.BankServiceImpl;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestPropertySource("classpath:application.yml")
@Import({
        CardServiceImpl.class,
        CardMapperImpl.class,
        EntityAccessProviderImpl.class,
        BankServiceImpl.class,
        BankMapperImpl.class
})
class CardServiceImplIntegrationTest extends BaseRepositoryTest {

    @Autowired
    private CardService cardService;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EntityAccessProvider entityAccessProvider;

    @Autowired
    private BankService bankService;

    private SecurityUser securityUser;
    private User user;
    private Currency currencyRub;
    private Bank bank;
    private BankAccount account1;
    private BankAccount account2;
    private Card existingCard;

    @BeforeEach
    void setUp() {
        // Очищаем базу перед каждым тестом
        cardRepository.deleteAll();

        // Создаём пользователя
        user = new User();
        user.setLogin("testuser");
        user.setEmail("test@example.com");
        user.setPassword("pass");
        user.setName("Test");
        user.setSurname("User");
        user = entityManager.persistAndFlush(user);

        securityUser = new SecurityUser(
                user.getId(), user.getLogin(), user.getPassword(), null,
                true, true, true, true
        );

        // Создаём валюту
        currencyRub = new Currency("RUB", "₽", "Russian Ruble");
        entityManager.persistAndFlush(currencyRub);

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
    @DisplayName("Должен создать новую карту")
    void shouldCreateNewCard() {
        // Given
        NewCardDto newCardDto = new NewCardDto();
        newCardDto.setCardName("Новая карта");
        newCardDto.setLastFourDigits("9999");
        newCardDto.setAccountId(account1.getId());

        // When
        CardDto result = cardService.createCard(newCardDto, securityUser);

        // Then
        Card savedCard = entityManager.find(Card.class, result.getId());
        assertThat(savedCard).isNotNull();
        assertThat(savedCard.getCardName()).isEqualTo("Новая карта");
        assertThat(savedCard.getLastFourDigits()).isEqualTo("9999");
        assertThat(savedCard.getAccount().getId()).isEqualTo(account1.getId());
        assertThat(savedCard.getIssuerBank().getId()).isEqualTo(bank.getId());
    }

    @Test
    @DisplayName("Должен вернуть список карт пользователя")
    void shouldReturnUserCardList() {
        // Given - создаём ещё одну карту для того же пользователя
        Card anotherCard = new Card();
        anotherCard.setCardName("Another Card");
        anotherCard.setLastFourDigits("5678");
        anotherCard.setAccount(account2); // другой счёт, но тот же пользователь
        anotherCard.setIssuerBank(bank);
        entityManager.persistAndFlush(anotherCard);

        // When
        List<CardDto> result = cardService.getCardListDto(securityUser);

        // Then
        assertThat(result).hasSize(2);

        // Проверяем, что все карты принадлежат пользователю
        assertThat(result)
                .extracting(CardDto::getCardName)
                .containsExactlyInAnyOrder("Existing Card", "Another Card");

        assertThat(result)
                .extracting(CardDto::getLastFourDigits)
                .containsExactlyInAnyOrder("1234", "5678");
    }

    @Test
    @DisplayName("Должен вернуть пустой список когда у пользователя нет карт")
    void shouldReturnEmptyListWhenUserHasNoCards() {
        // Given - создаём другого пользователя без карт
        User anotherUser = new User();
        anotherUser.setLogin("anotheruser");
        anotherUser.setEmail("another@example.com");
        anotherUser.setPassword("pass");
        anotherUser.setName("Another");
        anotherUser.setSurname("User");
        anotherUser = entityManager.persistAndFlush(anotherUser);

        SecurityUser anotherSecurityUser = new SecurityUser(
                anotherUser.getId(), anotherUser.getLogin(), anotherUser.getPassword(), null,
                true, true, true, true
        );

        // When
        List<CardDto> result = cardService.getCardListDto(anotherSecurityUser);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Должен удалить карту пользователя")
    void shouldDeleteUserCard() {
        // Given
        Long cardId = existingCard.getId();

        // Проверяем, что карта существует перед удалением
        assertThat(entityManager.find(Card.class, cardId)).isNotNull();

        // When
        cardService.deleteCard(cardId, securityUser);

        // Then
        // Проверяем, что карта удалена из БД
        assertThat(entityManager.find(Card.class, cardId)).isNull();
    }

    @Test
    @DisplayName("Должен корректно обрабатывать создание карты с минимальными данными")
    void shouldCreateCardWithMinimalData() {
        // Given
        NewCardDto newCardDto = new NewCardDto();
        newCardDto.setCardName("Minimal Card");
        newCardDto.setLastFourDigits("0000");
        newCardDto.setAccountId(account1.getId());

        // When
        CardDto result = cardService.createCard(newCardDto, securityUser);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCardName()).isEqualTo("Minimal Card");
        assertThat(result.getLastFourDigits()).isEqualTo("0000");
        assertThat(result.getAccountId()).isEqualTo(account1.getId());
        assertThat(result.getIssuerBankId()).isEqualTo(bank.getId());
    }

    @Test
    @DisplayName("Должен возвращать карты только для запрошенного пользователя")
    void shouldReturnCardsOnlyForRequestedUser() {
        // Given - создаём другого пользователя с картой
        User anotherUser = new User();
        anotherUser.setLogin("anotheruser");
        anotherUser.setEmail("another@example.com");
        anotherUser.setPassword("pass");
        anotherUser.setName("Another");
        anotherUser.setSurname("User");
        anotherUser = entityManager.persistAndFlush(anotherUser);

        BankAccount anotherAccount = new BankAccount();
        anotherAccount.setName("Another Account");
        anotherAccount.setAccountNumber("40817810099910000003");
        anotherAccount.setBalance(new java.math.BigDecimal("200.00"));
        anotherAccount.setUser(anotherUser);
        anotherAccount.setCurrency(currencyRub);
        anotherAccount.setBank(bank);
        anotherAccount = entityManager.persistAndFlush(anotherAccount);

        Card anotherUserCard = new Card();
        anotherUserCard.setCardName("Another User Card");
        anotherUserCard.setLastFourDigits("8888");
        anotherUserCard.setAccount(anotherAccount);
        anotherUserCard.setIssuerBank(bank);
        entityManager.persistAndFlush(anotherUserCard);

        SecurityUser anotherSecurityUser = new SecurityUser(
                anotherUser.getId(), anotherUser.getLogin(), anotherUser.getPassword(), null,
                true, true, true, true
        );

        // When - получаем карты для основного пользователя
        List<CardDto> mainUserCards = cardService.getCardListDto(securityUser);
        List<CardDto> anotherUserCards = cardService.getCardListDto(anotherSecurityUser);

        // Then
        assertThat(mainUserCards).hasSize(1);
        assertThat(mainUserCards.get(0).getCardName()).isEqualTo("Existing Card");

        assertThat(anotherUserCards).hasSize(1);
        assertThat(anotherUserCards.get(0).getCardName()).isEqualTo("Another User Card");
    }

    @Test
    @DisplayName("Должен вернуть DTO для существующей карты по ID")
    void shouldReturnCardDtoForExistingCard() {

        CardDto cardDto = cardService.getCardDtoById(existingCard.getId(), securityUser);
        assertThat(cardDto).isNotNull();
        assertThat(cardDto.getId()).isEqualTo(existingCard.getId());
        assertThat(cardDto.getCardName()).isEqualTo(existingCard.getCardName());
        assertThat(cardDto.getLastFourDigits()).isEqualTo(existingCard.getLastFourDigits());

        assertAllFieldsInitialized(cardDto);
    }






    @Test
    @DisplayName("Должен корректно маппить все поля при создании карты")
    void shouldCorrectlyMapAllFieldsWhenCreatingCard() {
        // Given

        String cardName = "Premium Card";
        String lastFourDigits = "5555";
        Long accountId = account1.getId();

        NewCardDto newCardDto = new NewCardDto();
        newCardDto.setCardName(cardName);
        newCardDto.setLastFourDigits(lastFourDigits);
        newCardDto.setAccountId(accountId);

        // When
        CardDto result = cardService.createCard(newCardDto, securityUser);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCardName()).isEqualTo(cardName);
        assertThat(result.getLastFourDigits()).isEqualTo(lastFourDigits);
        assertThat(result.getAccountId()).isEqualTo(accountId);

        // Проверяем, что все поля заполнены в entity
        Card savedCard = cardRepository.findById(result.getId()).orElseThrow();
        assertThat(savedCard.getCardName()).isEqualTo(cardName);
        assertThat(savedCard.getLastFourDigits()).isEqualTo(lastFourDigits);
        assertThat(savedCard.getAccount().getId()).isEqualTo(accountId);
    }
}