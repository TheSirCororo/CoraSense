# Используемые технологии

## Код

- [Kotlin](https://kotlinlang.org/) - современный, удобный и практичный язык программирования
- [Ktor](https://ktor.io) - высокопроизводительный, реактивный и удобный для Kotlin-разработчиков web-фреймворк
- [Exposed](https://github.com/JetBrains/Exposed) - библиотека для взаимодействия с СУБД, полу-ORM
- [Micrometer](https://micrometer.io/) - библиотека для агрегирования микрометрик
- [Koin](https://insert-koin.io/) - Dependency Injection фреймворк, практичный для использования в Kotlin проектах
- [konform](https://github.com/konform-kt/konform) - библиотека для валидации на Kotlin
- [vendelieu/telegram-bot](https://github.com/vendelieu/telegram-bot) - лучшая (по моему мнению) библиотека для Telegram
  ботов на Kotlin

## Тестирование

- [Kotest](https://kotest.io/) + [JUnit](https://junit.org/junit5/) - фреймворки для запуска тестирования, устоявшиеся
  на рынке
- [Testcontainers](https://testcontainers.com/) - способ контейнеризации необходимых зависимостей на время тестов.
  Библиотека обладает высокой производительностью и подходит для E2E тестов
- [MockK](https://mockk.io/) - библиотека для мокирования зависимостей при `unit`-тестировании
- [JaCoCo](https://www.eclemma.org/jacoco/) - инструмент для создания `test coverage` отчётов

## Хранение данных

- [PostgreSQL](https://www.postgresql.org/) - высокопроизводительная СУБД

## Контейнеризация

- [Docker](http://docker.io/)
- [Docker Compose](https://github.com/docker/compose)

## Статистика и агрегирование микрометрик

- [Grafana](https://grafana.com/) - инструмент для визуализации статистики
- [Prometheus](https://prometheus.io/) - инструмент для передачи данных о статистике в Grafana

## Документация

- [Swagger](http://swagger.io/) - средство отображения OpenAPI спецификации
- [smiley4 swagger UI](https://github.com/SMILEY4/ktor-swagger-ui) - генерация OpenAPI спецификации и отображение
  Swagger UI для Ktor