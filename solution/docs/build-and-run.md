# Сборка и запуск приложения
Для полноценного запуска приложения вам понадобится установленный Docker.

## Режим продакшена
### Запуск
Выполните команду в терминале:
```bash
docker compose build
docker compose up -d
```

Приложение самостоятельно соберётся и запустится.
### Остановка
Выполните команду в терминале:
```bash
docker compose down
```

## Режим разработки
В режиме разработки всё работает аналогично, кроме пары моментов:
1) Само приложение не помещается в контейнер, а запускается как обычное JVM приложение (т.е. вам необходимо иметь на компьютере установленный JDK 17).
2) Данные в dev и production режиме в Grafana, Prometheus и PostgreSQL сохраняются независимо друг от друга (разные volumes)

### Запуск
1. Задайте переменные среды (environment variables)
2. Поднимите сторонние сервисы:
```bash
docker compose -f dev-compose.yml up -d
```

3. Запустите само приложение:
```bash
./gradlew runFatJar
```

### Остановка
1. Остановите приложение (Ctrl + C)
2. Остановите сторонние сервисы:
```bash
docker compose down
```

## Переменные среды
Для запуска по умолчанию все переменные заданы в файле `.env`.

В приложении используются следующие environment variables:
1. `POSTGRES_USERNAME` - PostgreSQL username
2. `POSTGRES_PASSWORD` - PosgresSQL password
3. `POSTGRES_DB` - PostgreSQL database name
4. `POSTGRES_JDBC_URL` - строка подключения к БД в формате JDBC
5. `IMAGE_STORAGE_TYPE` - тип хранилища картинок. FILE - хранение локально в файлах в docker volume. S3 - хранение на удалённом S3 хранилище (данные для S3 далее)
6. `S3_ENDPOINT` - базовый URL S3 хранилища
7. `S3_KEY_ID` - ID ключа S3
8. `S3_KEY_VALUE` - значение ключа S3
9. `S3_BUCKET` - бакет S3 хранилища
10. `GROQ_API_KEY` - API ключ для [Groq](https://groq.com/), который используется как агрегатор LLM моделей
11. `LLM_ENABLED` - включен ли LLM в приложении (true/false)
12. `GROQ_BASE_URL` - базовый URL для [Groq](https://groq.com/)
13. `MODERATION_ENABLED` - включена ли модерация в приложении (true/false)
14. `MODERATION_MODE` - общий режим модерации - LLM или BLACKLIST
15. `TELEGRAM_BOT_TOKEN` - токен бота Telegram
16. `TELEGRAM_BOT_ENABLED` - включена ли функциональность телеграм бота (true/false)
17. `SERVER_PORT` - порт приложения на хост-машине