# Web-приложение «Сервис для планирования электросети и подбора оборудования в жилые помещения»

## Описание проекта

Веб-приложение для планирования электросети и подбора электротехнического оборудования в жилые помещения. Разработано на Java с использованием Spring Boot.

## Технологический стек

- **Java 17**
- **Spring Boot 3.5.7**
- **Spring Data JPA** (Code First подход)
- **Spring Security** с JWT аутентификацией
- **H2 Database** (для разработки) / **PostgreSQL** (для продакшена)
- **Lombok**
- **Maven**

## Архитектура проекта

Проект следует принципам чистой архитектуры:

```
src/main/java/com/verchuk/electro/
├── config/              # Конфигурационные классы
├── controller/          # REST контроллеры
├── dto/                 # Data Transfer Objects
│   ├── request/         # DTO для запросов
│   └── response/        # DTO для ответов
├── exception/           # Обработка исключений
├── model/              # Entity классы (JPA)
├── repository/         # Repository интерфейсы
├── security/           # Spring Security конфигурация
└── service/            # Бизнес-логика
```

## Роли пользователей

### Проектировщик (DESIGNER)
- Регистрация (новые пользователи автоматически получают роль DESIGNER)
- Аутентификация
- Создание, изменение, удаление проектов электросети
- Добавление, изменение, удаление жилых помещений в проект
- Просмотр каталога электроприборов
- Добавление, изменение, удаление списка электроприборов проекта
- Получение расчетной ведомости проекта
- Получение спецификации электротехнического оборудования
- Обновление, удаление личного профиля

### Администратор (ADMIN)
- Создание, изменение, удаление пользователей
- Добавление, изменение, удаление электроприборов
- Добавление, удаление типов жилых помещений
- Просмотр проектов электросети
- Просмотр статистики используемых электроприборов

## API Endpoints

### Аутентификация
- `POST /api/auth/register` - Регистрация
- `POST /api/auth/login` - Вход

### Профиль пользователя
- `GET /api/user/profile` - Получить текущий профиль
- `PUT /api/user/profile` - Обновить профиль
- `DELETE /api/user/profile` - Удалить профиль

### Проекты (требует роль DESIGNER)
- `GET /api/designer/projects` - Список проектов
- `GET /api/designer/projects/{id}` - Получить проект
- `POST /api/designer/projects` - Создать проект
- `PUT /api/designer/projects/{id}` - Обновить проект
- `DELETE /api/designer/projects/{id}` - Удалить проект

### Помещения (требует роль DESIGNER)
- `GET /api/designer/projects/{projectId}/rooms` - Список помещений
- `GET /api/designer/projects/{projectId}/rooms/{roomId}` - Получить помещение
- `POST /api/designer/projects/{projectId}/rooms` - Создать помещение
- `PUT /api/designer/projects/{projectId}/rooms/{roomId}` - Обновить помещение
- `DELETE /api/designer/projects/{projectId}/rooms/{roomId}` - Удалить помещение

### Электроприборы
- `GET /api/appliances` - Каталог электроприборов
- `GET /api/appliances/{id}` - Получить электроприбор

### Электроприборы проекта (требует роль DESIGNER)
- `GET /api/designer/projects/{projectId}/appliances` - Список электроприборов проекта
- `POST /api/designer/projects/{projectId}/appliances` - Добавить электроприбор
- `PUT /api/designer/projects/{projectId}/appliances/{projectApplianceId}` - Обновить электроприбор
- `DELETE /api/designer/projects/{projectId}/appliances/{projectApplianceId}` - Удалить электроприбор

### Расчеты (требует роль DESIGNER)
- `GET /api/designer/projects/{projectId}/calculations` - Получить расчетную ведомость

### Спецификации (требует роль DESIGNER)
- `GET /api/designer/projects/{projectId}/specifications` - Получить спецификацию оборудования

### Администратор (требует роль ADMIN)
- `GET /api/admin/users` - Список пользователей
- `GET /api/admin/users/{id}` - Получить пользователя
- `POST /api/admin/users` - Создать пользователя
- `PUT /api/admin/users/{id}` - Обновить пользователя
- `DELETE /api/admin/users/{id}` - Удалить пользователя
- `POST /api/admin/appliances` - Создать электроприбор
- `PUT /api/admin/appliances/{id}` - Обновить электроприбор
- `DELETE /api/admin/appliances/{id}` - Удалить электроприбор
- `GET /api/admin/room-types` - Список типов помещений
- `POST /api/admin/room-types` - Создать тип помещения
- `PUT /api/admin/room-types/{id}` - Обновить тип помещения
- `DELETE /api/admin/room-types/{id}` - Удалить тип помещения
- `GET /api/admin/projects` - Список всех проектов
- `GET /api/admin/projects/{id}` - Получить проект
- `GET /api/admin/statistics/appliances` - Статистика электроприборов

## Запуск приложения

1. Убедитесь, что установлены Java 17 и Maven
2. Клонируйте репозиторий
3. Запустите приложение:
   ```bash
   mvn spring-boot:run
   ```
4. Приложение будет доступно по адресу: `http://localhost:8080`
5. H2 Console доступна по адресу: `http://localhost:8080/h2-console`

## Настройка базы данных

По умолчанию используется H2 in-memory база данных. Для использования PostgreSQL:

1. Раскомментируйте соответствующие строки в `application.properties`
2. Создайте базу данных PostgreSQL
3. Обновите настройки подключения

## JWT Аутентификация

Все защищенные endpoints требуют JWT токен в заголовке:
```
Authorization: Bearer <token>
```

Токен получается при регистрации или входе через `/api/auth/register` или `/api/auth/login`.

## Инициализация данных

При первом запуске автоматически создаются роли:
- DESIGNER
- ADMIN

