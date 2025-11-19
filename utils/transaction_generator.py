#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import csv
import random
from datetime import datetime, timedelta
import pathlib
import os

# ------------------------------------------------------------------
# Параметры: в списках – ID, которые появятся в БД
# ------------------------------------------------------------------

# Пользователи
user_ids = [1, 2, 3]

# Карты: по 2 карты на каждого пользователя по порядку
# Формат: (card_id, user_id, currency_id)
cards = [
    (1, 1, 1), (2, 1, 2),  # пользователь 1: карта 1 (валюта 1), карта 2 (валюта 2)
    (3, 2, 1), (4, 2, 2),  # пользователь 2: карта 3 (валюта 1), карта 4 (валюта 2)
    (5, 3, 1), (6, 3, 2),  # пользователь 3: карта 5 (валюта 1), карта 6 (валюта 2)
]

# Категории: по 3 категории на каждого пользователя по порядку
# Формат: (category_id, user_id)
categories = [
    (1, 1), (2, 1), (3, 1),  # пользователь 1: категории 1, 2, 3
    (4, 2), (5, 2), (6, 2),  # пользователь 2: категории 4, 5, 6
    (7, 3), (8, 3), (9, 3),  # пользователь 3: категории 7, 8, 9
]

# Банки (первые 4)
bank_ids = [1, 2, 3, 4]

# Валюты (3 валюты)
currency_ids = [1, 2, 3]

# ------------------------------------------------------------------
# Список типов операций 
operation_types = ["OUTGOING", "INCOMING", "TRANSFER"]

# ------------------------------------------------------------------
# Настраиваем генерацию
# ------------------------------------------------------------------
NUM_TRANSACTIONS = 500  # сколько строк
SEED = 42  # фиксируем генератор, чтобы при повторе получаем те же данные
random.seed(SEED)


# ---- Helper: случайный datetime в пределах последних 180 дней
def random_datetime():
    now = datetime.now()
    start = now - timedelta(days=180)
    return (start + (now - start) * random.random()).strftime("%Y-%m-%d %H:%M:%S")


# ---- Helper: случайный amount с учетом типа операции
def random_amount(operation_type):
    amount = random.uniform(1, 5000)
    # OUTGOING - отрицательные суммы, остальные положительные
    if operation_type == "OUTGOING":
        amount = -amount
    return f"{amount:.2f}"


# ---- Helper: случайный hide / master (0/1)
def rand_flag():
    return str(random.randint(0, 1))


# ---- Helper: получить карты пользователя
def get_user_cards(user_id):
    return [card for card in cards if card[1] == user_id]


# ---- Helper: получить категории пользователя
def get_user_categories(user_id):
    return [category[0] for category in categories if category[1] == user_id]


# ------------------------------------------------------------------
# Формируем CSV
# ------------------------------------------------------------------
# Создаем папку в текущей директории скрипта
script_dir = pathlib.Path(__file__).parent
output_dir = script_dir / "csv"
output_dir.mkdir(parents=True, exist_ok=True)

output_path = output_dir / "2025-09-24--0003-transactions.csv"

print(f"Создаем файл: {output_path}")

with output_path.open("w", encoding="utf-8", newline="") as f:
    writer = csv.writer(f, delimiter=";", quotechar='"', quoting=csv.QUOTE_MINIMAL)

    # заголовки
    writer.writerow(
        [
            "description",
            "amount",
            "operation_time",
            "hide",
            "master",
            "currency_id",
            "category_id",
            "card_id",
            "parent_transaction_id",
            "user_id",
            "operation_type",
        ]
    )

    # список существующих id транзакций для случайных родительских ссылок
    existing_ids = []

    for i in range(1, NUM_TRANSACTIONS + 1):
        # Сначала выбираем пользователя
        user_id = random.choice(user_ids)
        
        # Получаем карты и категории этого пользователя
        user_cards = get_user_cards(user_id)
        user_category_ids = get_user_categories(user_id)
        
        # Выбираем случайную карту пользователя
        selected_card = random.choice(user_cards)
        card_id = selected_card[0]
        card_currency_id = selected_card[2]  # валюта карты
        
        # Описание
        descr = random.choice(
            [
                "Оплата товара",
                "Приход с карты",
                "Перевод на счет",
                "Вывод на карту",
                "Зарплата",
                "Капитализация",
                "Купил ленту",
                "Расход на поездку",
                "Платеж в сервис",
                "Кредитный платеж",
                "Выплата процентов",
                "Выплата займа",
            ]
        )
        # иногда более длинный текст
        if random.random() < 0.3:
            descr += f" ({random.randint(100, 9999)})"

        # Выбираем тип операции
        op_type = random.choice(operation_types)
        
        # Генерируем сумму с учетом типа операции
        amount = random_amount(op_type)
        
        op_time = random_datetime()
        hide = rand_flag()
        master = rand_flag()

        # Используем валюту карты, а не случайную валюту
        currency_id = card_currency_id
        
        category_id = random.choice(user_category_ids)  # только категории пользователя

        # parent_transaction_id: 5% шанс ссылаться
        parent_transaction_id = ""
        if existing_ids and random.random() < 0.05:
            parent_transaction_id = str(random.choice(existing_ids))

        row = [
            descr,
            amount,
            op_time,
            hide,
            master,
            currency_id,
            category_id,
            card_id,
            parent_transaction_id,
            user_id,
            op_type,
        ]
        writer.writerow(row)

        # сохраняем создаваемый id (это просто индекс от 1)
        existing_ids.append(i)

print(f"Сгенерировано {NUM_TRANSACTIONS} строк в {output_path}")

# Выводим информацию о картах для проверки
print("\nИнформация о картах:")
print("Карта | Пользователь | Валюта")
print("-" * 25)
for card in cards:
    print(f"{card[0]:5} | {card[1]:11} | {card[2]:6}")

print(f"\nФайл успешно создан: {output_path.absolute()}")