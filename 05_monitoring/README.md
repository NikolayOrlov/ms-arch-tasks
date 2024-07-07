# Prometheus. Grafana

## Сборка java приложения / Установка PostgresDB в otus namespace:
### Сборка java приложения
```
./mvnw clean package
```

### Сборка docker образа
```
docker build --platform linux/amd64 -t user-app:1.1.0 .
```

### Добавление к образу тега с версией
```
docker image tag user-app:1.1.0 gmnvnorlov/ms-arch-tasks-user-app:1.1.0
```

###  Push образа в dockerhub
```
docker push gmnvnorlov/ms-arch-tasks-user-app:1.1.0
```

### Установка PostgresDB в otus namespace
см. в [прошлом ДЗ](../04_helm/README.md)

### Для запуска приложения в k8s
выполнить команду в папке ./manifests
```
kubectl apply -f . -n=otus
```

## Новый namespace "monitoring"
```
kubectl create namespace monitoring
```

## Установка monitoring инструментов с помощью helm

### Установка Prometheus в monitoring namespace
```
helm install prometheus -f prometheus-values.yaml bitnami/prometheus -n monitoring
```

### Установка Grafana в monitoring namespace
```
helm install grafana -f grafana-values.yaml bitnami/grafana -n monitoring
```

### Получение пароля для пользователя admin
```
kubectl get secret grafana-admin --namespace monitoring -o jsonpath="{.data.GF_SECURITY_ADMIN_PASSWORD}" | base64 -d
```

### Grafana UI port forward
```
kubectl port-forward svc/grafana 3000:3000 -n monitoring --address 0.0.0.0 &
```

