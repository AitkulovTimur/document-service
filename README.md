# Document Service Project

Многомодульный Maven проект для управления документами с API сервисом и CLI утилитой для генерации документов.

## Структура проекта

```
document-service-parent
├── pom.xml (parent)
├── document-service (Spring Boot API)
└── document-generator (CLI утилита)
```

## Модули

### document-service
Spring Boot приложение с REST API для управления документами.

**Возможности:**
- REST API для CRUD операций с документами
- Валидация документов
- Интеграция с базой данных через JPA
- Миграции Liquibase
- OpenAPI/Swagger документация

### document-generator
CLI утилита для массовой генерации документов через API.

**Возможности:**
- Чтение параметров из конфигурационного файла
- Многопоточная генерация документов
- HTTP запросы к API сервиса
- Логирование процесса

## Быстрый старт

### Сборка проекта
```bash
mvn clean package
```

### Запуск API сервиса
```bash
cd document-service
mvn spring-boot:run
```

API будет доступно по адресу: http://localhost:8080

### Запуск генератора документов
```bash
cd document-generator
java -jar target/document-generator-0.0.1-SNAPSHOT-jar-with-dependencies.jar
```

## Конфигурация генератора

Файл `config.properties` в модуле `document-generator`:

```properties
document.count=50
api.url=http://localhost:8080/api/documents
```

## Технологии

- Java 21
- Spring Boot 4.0.3
- Maven
- PostgreSQL
- Liquibase
- MapStruct
- Lombok
- OpenAPI
- Apache HttpClient
- JUnit 5
- TestContainers
