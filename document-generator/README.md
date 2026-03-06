# Document Generator

CLI утилита для генерации документов через API сервиса.

## Использование

### Сборка
```bash
mvn clean package
```

### Запуск
```bash
# С конфигурацией по умолчанию
java -jar target/document-generator-0.0.1-SNAPSHOT-jar-with-dependencies.jar

# С кастомным конфигурационным файлом
java -jar target/document-generator-0.0.1-SNAPSHOT-jar-with-dependencies.jar custom-config.properties
```

## Конфигурация

Файл конфигурации (config.properties):
- `document.count` - количество документов для генерации (по умолчанию: 50)
- `api.url` - URL API сервиса (по умолчанию: http://localhost:8080/api/documents)

Пример:
```properties
document.count=100
api.url=http://localhost:8080/api/documents
```

## Работа утилиты

1. Читает параметр N из конфигурационного файла
2. Создает N HTTP POST запросов к API сервиса
3. Каждый запрос содержит JSON с полями `title` и `content`
4. Работает в многопоточном режиме (10 потоков)
5. Логирует процесс генерации
