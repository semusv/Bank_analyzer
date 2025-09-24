#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import csv
import random
from datetime import datetime, timedelta
import pathlib

# ------------------------------------------------------------------
# Параметры: в списках – ID, которые появятся в БД (если они не auto‑инкрементны, укажите вручную)
# В правах подготовительных скриптов вставляем в реальном кейсе целевая таблица – то, что сейчас несколько ID создаются автоматически.
# ------------------------------------------------------------------
# Категории
categories = [
    # (id, user_id) – в реальности категория id – уникальный по всем пользователям, здесь просто просто числа 1‑9
    1,
    2,
    3,
    4,
    5,
    6,
    7,
    8,
    9,
]

# Банки (первые 4)
bank_ids = [1, 2, 3, 4]

# Валюты (наконец – 3)
currency_ids = [1, 2, 3]

# Пользователи
user_ids = [4]

# Карты (id 1‑6, как указываются в cards.csv: каждая карта связана с определённым account_id, но нам нужен только card_id)
card_ids = [1, 2, 3, 4, 5, 6]

# Для parent_transaction_id ссылаться будем лишь на произвольные ранее сгенерированные транзакции.
# Далее будем генерировать транзакции последовательно, а потом смшить случайно 0/1 (т. е. 1–возможно родитель, 0 – нет).
# ------------------------------------------------------------------
# Список семейства operation_type – в реальности он может быть перечислением наличных ордеров.
operation_types = ["EXPENSE", "INCOME", "TRANSFER", "WITHDRAWAL", "DEPOSIT"]

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


# ---- Helper: случайный amount (до 5000 руб, но с десятичным знаком,  позиции)
def random_amount():
    return f"{random.uniform(1, 5000):.2f}"


# ---- Helper: случайный hide / master (0/1)
def rand_flag():
    return str(random.randint(0, 1))


# ------------------------------------------------------------------
# Формируем CSV
# ------------------------------------------------------------------
output_path = pathlib.Path("csv") / "2025-09-24--0003-transactions.csv"
output_path.parent.mkdir(parents=True, exist_ok=True)

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

    # список к старым id транзакций для случайных родительских ссылок
    existing_ids = []

    for i in range(1, NUM_TRANSACTIONS + 1):
        # Описание – простое темповое приветствие, можно варьировать слова, но просто оставляем.
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

        amount = random_amount()
        op_time = random_datetime()
        hide = rand_flag()
        master = rand_flag()

        currency_id = random.choice(currency_ids)
        category_id = random.choice(categories)
        card_id = random.choice(card_ids)

        # parent_transaction_id: 5% шанс ссылаться
        parent_transaction_id = ""
        if existing_ids and random.random() < 0.05:
            parent_transaction_id = str(random.choice(existing_ids))

        user_id = random.choice(user_ids)
        op_type = random.choice(operation_types)

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
