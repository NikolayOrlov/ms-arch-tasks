# Работа с Helm-ом

## Сборка и запуск локально java приложения с REST API

### java 21
```
./mvnw clean package
```

### Сборка docker образа
```
docker build --platform linux/amd64 -t user-app:1.0.0 .
```
### Конфигурирование
Определить IP адрес PostgresDB, доступный запускаемому docker container-у, в файле ./local.env для имени 'postgres_url' environment variable.

### Запуск контейнера из собранного локально образа
```
docker run -d -p 8080:8080 --env-file ./local.env --name user-app-container user-app:1.0.0
```

### Доступ к REST API и healthcheck из браузера
- http://localhost:8080/swagger-ui/index.html
- http://localhost:8080/actuator/health

## Работа с dockerhub

### Добавление к образу тега с версией
```
docker image tag user-app:1.0.0 gmnvnorlov/ms-arch-tasks-user-app:1.0.0
```

###  Push образа в dockerhub
```
docker push gmnvnorlov/ms-arch-tasks-user-app:1.0.0
```

### Запуск контейнера из образа, полученного из dockerhub
```
docker run -d -p 8080:8080 --env-file ./local.env --name user-app-container gmnvnorlov/ms-arch-tasks-user-app:1.0.0
```




