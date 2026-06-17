#  Task Tracker

REST API для управления задачами, написанный в учебных целях с упором на реальные практики разработки на Java/Spring Boot.

🔗 [GitHub Repository](https://github.com/execc0/task-tracker)
>  **Notification Service** — отдельный микросервис, который слушает события Task Tracker через Apache Kafka (изменение статуса задачи, регистрация и удаление пользователя) и отправляет email-уведомления. Репозиторий: [github.com/execc0/notification-service](https://github.com/execc0/notification-service).
---

## 🚀 Стек технологий

| Слой | Технология |
|---|---|
| Язык | Java 17 |
| Фреймворк | Spring Boot 3.4.1 |
| Сборка | Gradle (Groovy) |
| База данных | PostgreSQL 15 |
| ORM | Hibernate / Spring Data JPA |
| Миграции | Liquibase |
| Очереди сообщений | Apache Kafka |
| Кэширование | Redis 7.2 |
| Безопасность | Spring Security + JWT (jjwt 0.12.6) |
| Rate Limiting | Bucket4j |
| Утилиты | Lombok |
| Тестирование | JUnit 5, Mockito |
| Инфраструктура | Docker Compose |
| CI/CD | GitHub Actions |
---

##  Запуск проекта

### Требования
- Java 17+
- Docker & Docker Compose

### 1. Клонировать репозиторий
```bash
git clone https://github.com/execc0/task-tracker.git
cd task-tracker
```

### 2. Создать файл `.env` в корне проекта
```env
POSTGRES_DB=tasktracker
POSTGRES_USER=your_user
POSTGRES_PASSWORD=your_password
POSTGRES_PORT=5432
```

### 3. JWT Secret Key

Задаётся через переменную окружения `JWT_KEY`.

**Сгенерировать ключ:** `openssl rand -base64 64`

**IntelliJ IDEA:** Run Configuration → Environment Variables → `JWT_KEY=your_key`  

**Terminal:** `export JWT_KEY=your_key` (Linux/Mac) или `$env:JWT_KEY="your_key"` (PowerShell)

Минимальная длина ключа — 88 символов (base64-encoded, HS512)



### 4. Запустить инфраструктуру
```bash
docker-compose up -d
```
Запустит PostgreSQL 15, Apache Kafka и Zookeeper.

### 5. Запустить приложение
```bash
./gradlew bootRun
```

Liquibase автоматически применит все миграции при старте.

### 6. Тестирование API
#### Swagger UI
После запуска приложения документация доступна по адресу:
http://localhost:8080/swagger-ui.html
Для тестирования эндпоинтов необходимо:
1. Зарегистрироваться через POST /auth/register
2. Авторизоваться POST /auth/login — скопировать JWT токен из ответа.
3. Нажать **Authorize** → вставить токен → **Authorize**
4. Все эндпоинты теперь доступны
#### Postman Collection
Импортировать готовую коллекцию запросов task-tracker.postman_collection.json:
1. Открыть Postman → **Import** → выбрать файл
2. Создать пользователя через **Auth → register**
3. Авторизоваться через **Auth → Login (User)** или **Auth → Login (Admin)** — токен сохранится автоматически
4. Все запросы разбиты по папкам с настроенной авторизацией

> **Примечание:** Первый администратор создаётся вручную через БД.
> Необходимо зарегистрировать пользователя через API, затем выполнить SQL запрос:
> ```sql
> UPDATE users SET role = 'ADMIN' WHERE username = 'your_username';
> ```
> После этого все последующие назначения ролей можно делать через `PATCH /users/{id}/role` от имени администратора.

---

## Обзор API

##  Аутентификация
| Метод | Эндпоинт | Описание |
|---|---|---|
| POST | `/auth/register` | Регистрация нового пользователя |
| POST | `/auth/login` | Вход, возвращает JWT токен |

## 📋 Задачи — ADMIN
| Метод | Эндпоинт | Описание |
|---|---|---|
| GET | `/tasks` | Получить все задачи |
| GET | `/tasks/{id}` | Получить задачу по ID |
| GET | `/tasks/user/{userId}` | Получить задачи пользователя по ID |
| POST | `/tasks` | Создать задачу |
| PUT | `/tasks/{id}` | Обновить задачу |
| DELETE | `/tasks/{id}` | Удалить задачу |
| PATCH | `/tasks/{id}/status` | Обновить статус задачи |

## 📋 Задачи — USER
| Метод | Эндпоинт | Описание |
|---|---|---|
| GET | `/tasks/available` | Получить все свободные задачи |
| POST | `/tasks/available/{id}` | Взять свободную задачу |
| GET | `/tasks/my` | Получить свои задачи |
| GET | `/tasks/my/{id}` | Получить свою задачу по ID |
| POST | `/tasks/my` | Создать свою задачу (лимит: 20 активных) |
| PUT | `/tasks/my/{id}` | Обновить свою задачу |
| PATCH | `/tasks/my/{id}/status` | Обновить статус своей задачи |

## 👤 Пользователи — ADMIN
| Метод | Эндпоинт | Описание |
|---|---|---|
| GET | `/users` | Получить всех пользователей |
| GET | `/users/{id}` | Получить пользователя по ID |
| DELETE | `/users/{id}` | Удалить пользователя |
| PUT | `/users/{id}` | Обновить пользователя |
| PATCH | `/users/{id}/name` | Обновить имя пользователя |
| PATCH | `/users/{id}/email` | Обновить email пользователя |
| PATCH | `/users/{id}/role` | Обновить роль пользователя |

## 👤 Пользователи — USER
| Метод | Эндпоинт | Описание |
|---|---|---|
| PATCH | `/users/me/username` | Обновить свой username |
| PATCH | `/users/me/email` | Обновить свой email |
| PATCH | `/users/me/password` | Обновить свой пароль |
| PATCH | `/users/me/name` | Обновить своё имя |
