# RESTful API

## Общая схема взаимодействия компонентов

![Общая схема взаимодействия компонентов](SAGA.drawio.png)

## Сборка и развертывание

### Сборка java приложения, docker образа checkoutservice (в папке checkout-service)

```
./mvnw clean package

docker build --platform linux/amd64 -t checkoutservice:2.0.0 .

docker image tag checkoutservice:2.0.0 gmnvnorlov/ms-arch-tasks-checkoutservice:2.0.0

docker push gmnvnorlov/ms-arch-tasks-checkoutservice:2.0.0
```

### Сборка java приложения, docker образа orderservice (в папке order-service)

```
./mvnw clean package

docker build --platform linux/amd64 -t orderservice:2.0.1 .

docker image tag orderservice:2.0.1 gmnvnorlov/ms-arch-tasks-orderservice:2.0.1

docker push gmnvnorlov/ms-arch-tasks-orderservice:2.0.1
```

### Сборка java приложения, docker образа cartservice (в папке cartservice-service)

```
./mvnw clean package

docker build --platform linux/amd64 -t cartservice:2.0.0 .

docker image tag cartservice:2.0.0 gmnvnorlov/ms-arch-tasks-cartservice:2.0.0

docker push gmnvnorlov/ms-arch-tasks-cartservice:2.0.0
```

### Сборка java приложения, docker образа stockservice (в папке stockservice-service)

```
./mvnw clean package

docker build --platform linux/amd64 -t stockservice:2.0.1 .

docker image tag stockservice:2.0.1 gmnvnorlov/ms-arch-tasks-stockservice:2.0.1

docker push gmnvnorlov/ms-arch-tasks-stockservice:2.0.1
```

### Для запуска приложения в k8s
выполнить команду в папке ./manifests
```
kubectl apply -f . -n=otus
```

## [Postman коллекция](postman)
```
newman run RESTful.postman_collection.json > newman_output.txt
```