# Проектная работа

## Общая схема взаимодействия компонентов

![Общая схема взаимодействия компонентов](saga.drawio.png)

### Реализация идемпотентности:
Методы, отмеченные аннотацией @Idempotent, контролируют уникальность запросов с помощью HTTP заголовка 'Idempotency-Key' в формате UUID:
- POST /api/cartservice/public/line-item (newCustomerCartLineItem)
- POST /api/checkoutservice/public/checkout (checkout)
- POST /api/orderservice/private/order (createOrder)
- POST /api/stockservice/private/reserve (reserveProducts)
- POST /api/stockservice/private/reserve/handover (orderHandover)
- POST /api/deliveryservice/private/delivery (newDelivery)
- POST /api/notificationservice/private/notification (notifyCustomer)

Обработка аннотаций @Idempotent реализована в библиотеке microservice-chassis (в папке chassis-lib).

## Сборка и развертывание

### Сборка библиотеки microservice-chassis (в папке chassis-lib)
```
./mvnw clean install
```

### Сборка java приложения, docker образа authservice (в папке auth-service)

```
./mvnw clean package

docker build --platform linux/amd64 -t authservice:3.0.1 .

docker image tag authservice:3.0.1 gmnvnorlov/ms-arch-tasks-authservice:3.0.1

docker push gmnvnorlov/ms-arch-tasks-authservice:3.0.1
```

### Сборка java приложения, docker образа profileservice (в папке profile-service)

```
./mvnw clean package

docker build --platform linux/amd64 -t profileservice:3.0.3 .

docker image tag profileservice:3.0.3 gmnvnorlov/ms-arch-tasks-profileservice:3.0.3

docker push gmnvnorlov/ms-arch-tasks-profileservice:3.0.3
```

### Сборка java приложения, docker образа checkoutservice (в папке checkout-service)

```
./mvnw clean package

docker build --platform linux/amd64 -t checkoutservice:3.1.0 .

docker image tag checkoutservice:3.1.0 gmnvnorlov/ms-arch-tasks-checkoutservice:3.1.0

docker push gmnvnorlov/ms-arch-tasks-checkoutservice:3.1.0
```

### Сборка java приложения, docker образа orderservice (в папке order-service)

```
./mvnw clean package

docker build --platform linux/amd64 -t orderservice:3.3.1 .

docker image tag orderservice:3.3.1 gmnvnorlov/ms-arch-tasks-orderservice:3.3.1

docker push gmnvnorlov/ms-arch-tasks-orderservice:3.3.1
```

### Сборка java приложения, docker образа cartservice (в папке cartservice-service)

```
./mvnw clean package

docker build --platform linux/amd64 -t cartservice:3.0.1 .

docker image tag cartservice:3.0.1 gmnvnorlov/ms-arch-tasks-cartservice:3.0.1

docker push gmnvnorlov/ms-arch-tasks-cartservice:3.0.1
```

### Сборка java приложения, docker образа stockservice (в папке stockservice-service)

```
./mvnw clean package

docker build --platform linux/amd64 -t stockservice:3.1.2 .

docker image tag stockservice:3.1.2 gmnvnorlov/ms-arch-tasks-stockservice:3.1.2

docker push gmnvnorlov/ms-arch-tasks-stockservice:3.1.2
```

### Сборка java приложения, docker образа notificationservice (в папке notificationservice-service)

```
./mvnw clean package

docker build --platform linux/amd64 -t notificationservice:2.0.2 .

docker image tag notificationservice:2.0.2 gmnvnorlov/ms-arch-tasks-notificationservice:2.0.2

docker push gmnvnorlov/ms-arch-tasks-notificationservice:2.0.2

```
### Сборка java приложения, docker образа productservice (в папке productservice-service)

```
./mvnw clean package

docker build --platform linux/amd64 -t productservice:2.1.0 .

docker image tag productservice:2.1.0 gmnvnorlov/ms-arch-tasks-productservice:2.1.0

docker push gmnvnorlov/ms-arch-tasks-productservice:2.1.0
```

### Сборка java приложения, docker образа deliveryservice (в папке deliveryservice-service)

```
./mvnw clean package

docker build --platform linux/amd64 -t deliveryservice:2.0.1 .

docker image tag deliveryservice:2.0.1 gmnvnorlov/ms-arch-tasks-deliveryservice:2.0.1

docker push gmnvnorlov/ms-arch-tasks-deliveryservice:2.0.1
```

### Для запуска приложения в k8s

```
kubectl apply -f ./manifests -n=otus
```

## Monitoring setup

### Новый namespace "monitoring"

```
kubectl create namespace monitoring
```

### Установка monitoring инструментов с помощью helm в monitoring namespace

```
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo update
helm install monitoring-stack -f ./monitoring/prometheus.yaml prometheus-community/kube-prometheus-stack -n monitoring
```

## RDB (Postgres) setup

### Новый namespace "database"

```
kubectl create namespace database
```

### Установка RDB с помощью helm в database namespace

```
helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo update
helm install pgdb -f ./database/pgdb-values.yaml bitnami/postgresql -n database
```
default [values.yaml](https://github.com/bitnami/charts/blob/main/bitnami/postgresql/values.yaml)

### ServiceMonitor + Grafana/Prometheus UI via ingress (grafana creds: admin / prom-operator)

```
kubectl apply -f ./monitoring/manifests -n=monitoring
```

### Загрузка товаров
```
curl -X POST http://arch.homework/api/stockservice/public/load
```


## [Postman коллекция](postman)
```
newman run "SAGA by idempotent API with auth.postman_collection.json" > newman_output.txt
```
