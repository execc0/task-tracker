#  Task Tracker

REST API для управления задачами, написанный в учебных целях с упором на реальные практики разработки на Java/Spring Boot.

🔗 [GitHub Repository](https://github.com/execc0/task-tracker)

---

##  Стек технологий

| Слой | Технология |
|---|---|
| Язык | Java 17 |
| Фреймворк | Spring Boot 3.4.1 |
| Сборка | Gradle (Groovy) |
| База данных | PostgreSQL 15 |
| ORM | Hibernate / Spring Data JPA |
| Миграции | Liquibase |
| Очереди сообщений | Apache Kafka |
| Безопасность | Spring Security + JWT (jjwt 0.12.6) |
| Утилиты | Lombok |
| Тестирование | JUnit 5, Mockito |
| Инфраструктура | Docker Compose |


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
