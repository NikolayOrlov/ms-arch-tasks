# Prometheus. Grafana

## Сборка и запуск локально java приложения с REST API, Работа с helm / Установка PostgresDB в otus namespace из прошлого ДЗ

## Новый namespace "monitoring"
```
kubectl create namespace monitoring
```

## Установка monitoring инструментов с помощью helm

### Установка Prometheus в monitoring namespace
```
helm install prometheus bitnami/prometheus -n monitoring
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

