# TaskTracker

Микросервисный трекер задач с асинхронным уведомлением через Kafka.

## Технологии

- Java 21, Spring Boot 4.0.7
- Spring Cloud Gateway
- PostgreSQL, Kafka
- Docker, Docker Compose

## Сервисы

| Сервис               | Порт | Описание                                                 |
|----------------------|------|----------------------------------------------------------|
| API Gateway          | 8080 | Маршрутизация, JWT-фильтр                                |
| Auth Service         | 8082 | Регистрация, логин, JWT + продюсер событий регистрации   |
| Task Service         | 8081 | CRUD задач, Kafka-продюсер, консюмер событий регистрации |
| Notification Service | 8083 | Kafka-консюмер, email-уведомления                        |

## Kafka-топики

- `user-registered-events` — новые пользователи (Auth → Task)
- `task-events` — создание задач (Task → Notification)
- `task-overdue-events` — просроченные задачи (Task → Notification)

## Запуск

```bash
docker-compose up -d

## Документация API

Swagger: http://localhost:8080/swagger-ui/index.html

## Переменные окружения

JWT_SECRET — ключ для подписи JWT