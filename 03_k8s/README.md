# Базовые сущности Кubernetes

### Для развертывания контейнеризированного приложения, полученного в прошлом ДЗ  

## [Установить kubectl локально.](https://kubernetes.io/docs/tasks/tools/install-kubectl-linux/)

## [Сконфигурировать доступ в кластер k8s.](https://kubernetes.io/docs/tasks/access-application-cluster/configure-access-multiple-clusters/)

## В файле /etc/hosts добавить запись
```
<k8s_host_ip>   arch.homework  
```
где <k8s_host_ip> - IP адрес, на котором доступен k8s


## Новый namespace "otus"
```
kubectl create namespace otus
```

## Для запуска приложения в k8s выполнить команду в папке ./manifests
```
kubectl apply -f . -n=otus
```

## Проверить доступность запущенного приложения
```
curl http://arch.homework/health
```

## Для удаления приложения выполнить команду в папке ./manifests
```
kubectl delete -f . -n=otus
```
