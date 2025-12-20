# Настройка PostgreSQL - Решение проблем с подключением

## Ошибка: "пароль пользователя postgres неверный"

Эта ошибка означает, что пароль в `application.properties` не совпадает с паролем пользователя `postgres` в PostgreSQL.

## Решение

### Вариант 1: Узнать/изменить пароль пользователя postgres

#### Windows (через командную строку):
```bash
# Подключитесь к PostgreSQL (может запросить пароль)
psql -U postgres

# Если не знаете пароль, попробуйте сбросить его:
# 1. Найдите файл pg_hba.conf (обычно в C:\Program Files\PostgreSQL\XX\data\)
# 2. Измените метод аутентификации для local на "trust"
# 3. Перезапустите PostgreSQL
# 4. Подключитесь без пароля и установите новый пароль:
ALTER USER postgres WITH PASSWORD 'новый_пароль';
```

#### Linux/macOS:
```bash
# Попробуйте подключиться
sudo -u postgres psql

# Или если знаете пароль
psql -U postgres

# Установить новый пароль
ALTER USER postgres WITH PASSWORD 'новый_пароль';
```

### Вариант 2: Создать нового пользователя с известным паролем

```sql
-- Подключитесь к PostgreSQL (как суперпользователь)
CREATE USER electro_user WITH PASSWORD 'electro123';
ALTER USER electro_user CREATEDB;
GRANT ALL PRIVILEGES ON DATABASE electrodb TO electro_user;
```

Затем измените в `application.properties`:
```properties
spring.datasource.username=electro_user
spring.datasource.password=electro123
```

### Вариант 3: Использовать pgAdmin (графический интерфейс)

1. Откройте pgAdmin
2. Подключитесь к серверу PostgreSQL
3. Правый клик на "Login/Group Roles" → "Create" → "Login/Group Role"
4. Укажите имя и пароль
5. Во вкладке "Privileges" дайте права на создание баз данных
6. Обновите `application.properties` с новыми данными

## Проверка подключения

Перед запуском приложения проверьте подключение:

```bash
psql -U postgres -d electrodb
# или
psql -U ваш_пользователь -d electrodb
```

Если подключение успешно, значит данные верны.

## Создание базы данных

Убедитесь, что база данных `electrodb` создана:

```sql
CREATE DATABASE electrodb;
```

## Обновление application.properties

После того как узнаете правильный пароль, обновите файл:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/electrodb
spring.datasource.username=postgres  # или ваш_пользователь
spring.datasource.password=ваш_правильный_пароль
```

## Быстрое решение (если забыли пароль)

### Windows:
1. Остановите службу PostgreSQL
2. Найдите файл `pg_hba.conf` в папке данных PostgreSQL
3. Измените строку:
   ```
   # было:
   host    all             all             127.0.0.1/32            md5
   # станет:
   host    all             all             127.0.0.1/32            trust
   ```
4. Запустите службу PostgreSQL
5. Подключитесь без пароля: `psql -U postgres`
6. Установите новый пароль: `ALTER USER postgres WITH PASSWORD 'postgres';`
7. Верните `md5` в `pg_hba.conf` и перезапустите службу

### Linux/macOS:
```bash
sudo -u postgres psql
ALTER USER postgres WITH PASSWORD 'postgres';
\q
```



