# Prometheus. Grafana

## Сборка java приложения / Установка PostgresDB в otus namespace:
### Сборка java приложения
```
./mvnw clean package
```

### Сборка docker образа
```
docker build --platform linux/amd64 -t user-app:1.2.0 .
```

### Добавление к образу тега с версией
```
docker image tag user-app:1.2.0 gmnvnorlov/ms-arch-tasks-user-app:1.2.0
```

###  Push образа в dockerhub
```
docker push gmnvnorlov/ms-arch-tasks-user-app:1.2.0
```

### Установка PostgresDB в otus namespace
см. в [прошлом ДЗ](../04_helm/README.md)

### Для запуска приложения в k8s
выполнить команду в папке ./manifests
```
kubectl apply -f . -n=otus
```
---
## Инструменты monitoring из prometheus-community (kube-prometheus-stack)
## Новый namespace "otus-monitoring"
```
kubectl create namespace otus-monitoring
```
## Установка monitoring инструментов с помощью helm в otus-monitoring namespace
```
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo update
helm install monitoring-stack -f ./otus-monitoring/prometheus.yaml prometheus-community/kube-prometheus-stack -n otus-monitoring
```
### ServiceMonitor + Grafana/Prometheus UI via ingress
выполнить команду в папке ./manifests
```
kubectl apply -f ./otus-monitoring -n=otus-monitoring
```

---
## Инструменты monitoring из bitnami/prometheus
## Новый namespace "monitoring"
```
kubectl create namespace monitoring
```

## Установка monitoring инструментов с помощью helm

### Установка Prometheus в monitoring namespace
```
helm install prometheus -f ./monitoring/prometheus-values.yaml bitnami/prometheus -n monitoring
```

### Установка Grafana в monitoring namespace
```
helm install grafana -f ./monitoring/grafana-values.yaml bitnami/grafana -n monitoring
```

### Получение пароля для пользователя admin
```
kubectl get secret grafana-admin --namespace monitoring -o jsonpath="{.data.GF_SECURITY_ADMIN_PASSWORD}" | base64 -d
```

### Grafana UI port forward
```
kubectl port-forward svc/grafana 3000:3000 -n monitoring --address 0.0.0.0 &
```

### Grafana/Prometheus UI via ingress
выполнить команду в папке ./manifests
```
kubectl apply -f ./monitoring -n=monitoring
```
---
## Тестирование с помощью Apache Benchmark
ab -n 500 -c 50 http://arch.homework/dumb/a
ab -n 500 -c 50 http://arch.homework/dumb/b

while true; do ab -r -n 10 -c 1 http://arch.homework/dumb/a; ab -r -n 10 -c 1 http://arch.homework/dumb/b; done

### Примеры запросов
http_server_requests_seconds_sum
http_server_requests_seconds_sum{uri='/dumb/a'}
http_server_requests_seconds_sum{uri=~'/dumb/a|/dumb/b'}
http_server_requests_seconds_sum{uri=~'/dumb/.*'}
http_server_requests_seconds_sum{uri=~'/dumb/a',status=~"2.+"}