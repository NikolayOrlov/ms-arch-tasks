# Основы работы с Docker

## Сборка и запуск локально

### Сборка docker образа
```
docker build --platform linux/amd64 -t app:1.0.0 .
```

### Запуск контейнера из собранного локально образа
```
docker run -d -p 8000:8000 --name app-container app:1.0.0
```

## Работа с dockerhub

### Добавление к образу тега с версией
```
docker image tag app:1.0.0 gmnvnorlov/ms-arch-tasks-app:1.0.0
```

###  Push образа в dockerhub
```
docker push gmnvnorlov/ms-arch-tasks-app:1.0.0
```

### Запуск контейнера из образа, полученного из dockerhub
```
docker run -d -p 8000:8000 --name app-container gmnvnorlov/ms-arch-tasks-app:1.0.0
```

---

## docker registry в общем случае

### Добавление к образу тега с версией
```
docker image tag app:1.0.0 host:port/namespace/repository:version_tag
```

### Push образа в реестр
```
docker push host:port/namespace/repository:version_tag
```

### Запуск контейнера из образа, полученного из реестра
```
docker run -d -p 8000:8000 --name app-container host:port/namespace/repository:version_tag
```



