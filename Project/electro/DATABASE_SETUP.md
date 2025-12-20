# Настройка базы данных PostgreSQL (Code First)

## Code First подход

Проект использует **Code First** подход - все таблицы создаются автоматически на основе Entity классов при запуске приложения. Вам нужно создать только пустую базу данных.

## Шаги настройки

### 1. Установка PostgreSQL

Если PostgreSQL не установлен:
- **Windows**: https://www.postgresql.org/download/windows/
- **macOS**: `brew install postgresql` или https://www.postgresql.org/download/macosx/
- **Linux**: 
  ```bash
  sudo apt-get update
  sudo apt-get install postgresql postgresql-contrib
  ```

### 2. Создание пустой базы данных

**ВАЖНО**: Создайте только пустую базу данных. Таблицы создадутся автоматически!

#### Вариант 1: Через psql (командная строка)
```bash
# Подключитесь к PostgreSQL
psql -U postgres

# Создайте базу данных
CREATE DATABASE electrodb;

# Выйдите
\q
```

#### Вариант 2: Через pgAdmin (графический интерфейс)
1. Откройте pgAdmin
2. Правый клик на "Databases" → "Create" → "Database"
3. Имя: `electrodb`
4. Нажмите "Save"

#### Вариант 3: Через командную строку (одна команда)
```bash
psql -U postgres -c "CREATE DATABASE electrodb;"
```

### 3. Настройка в application.properties

Проверьте настройки в `application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/electrodb
spring.datasource.username=postgres
spring.datasource.password=ваш_пароль
```

**Если у вас другой пароль для пользователя postgres**, измените его в `application.properties`.

### 4. Запуск приложения

После создания пустой базы данных просто запустите приложение:

```bash
mvn spring-boot:run
```

**Что произойдет автоматически:**
- ✅ Spring Boot подключится к базе данных
- ✅ Hibernate создаст все таблицы на основе Entity классов
- ✅ Создадутся все связи между таблицами
- ✅ Инициализируются роли (DESIGNER, ADMIN) через DataInitializer
- ✅ Создастся администратор по умолчанию (username: admin, password: admin123)

### 5. Проверка

После запуска проверьте, что таблицы созданы:

```sql
psql -U postgres -d electrodb
\dt  -- список таблиц
```

Вы должны увидеть таблицы:
- users
- roles
- user_roles
- projects
- rooms
- room_types
- appliances
- project_appliances
- floor_plans
- walls
- wall_openings
- electrical_points
- electrical_symbols
- placement_rules

## Автоматическое обновление схемы

Благодаря `spring.jpa.hibernate.ddl-auto=update`:
- При добавлении новых Entity классов - создадутся новые таблицы
- При изменении существующих Entity - обновятся существующие таблицы
- **Данные сохраняются** при обновлении схемы

## Важно

- ❌ **НЕ создавайте таблицы вручную** - они создадутся автоматически
- ✅ **Создайте только пустую базу данных** `electrodb`
- ✅ При изменении Entity классов схема обновится автоматически

---

## H2 (для разработки)

Если хотите использовать H2 вместо PostgreSQL, раскомментируйте соответствующие строки в `application.properties` и закомментируйте настройки PostgreSQL.

---

## Переключение между базами данных

Можно использовать профили Spring Boot для удобного переключения:

### application-dev.properties (H2)
```properties
spring.datasource.url=jdbc:h2:file:./data/electrodb
spring.datasource.driverClassName=org.h2.Driver
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
```

### application-prod.properties (PostgreSQL)
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/electrodb
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```

Запуск с профилем:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
# или
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

