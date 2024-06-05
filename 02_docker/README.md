# Основы работы с Docker

### Сборка docker образа

- docker build -t app .

### Запуск контейнера из собранного образа
- docker run -d -p 8000:8000 --name app-container app
