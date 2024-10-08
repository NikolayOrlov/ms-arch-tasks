# Backend for frontends. Apigateway

## Сборка java приложения, docker образа authservice (в папке auth-service)

```
./mvnw clean package

docker build --platform linux/amd64 -t authservice:1.0.0 .

docker image tag authservice:1.0.0 gmnvnorlov/ms-arch-tasks-authservice:1.0.0

docker push gmnvnorlov/ms-arch-tasks-authservice:1.0.0
```

## Сборка java приложения, docker образа profileservice (в папке profile-service)

```
./mvnw clean package

docker build --platform linux/amd64 -t profileservice:1.0.0 .

docker image tag profileservice:1.0.0 gmnvnorlov/ms-arch-tasks-profileservice:1.0.0

docker push gmnvnorlov/ms-arch-tasks-profileservice:1.0.0
```

## Для запуска приложения в k8s
выполнить команду в папке ./manifests
```
kubectl apply -f . -n=otus
```

## [Postman коллекция](postman)
```
newman run "api gateway auth.postman_collection.json" > newman_output.txt
```