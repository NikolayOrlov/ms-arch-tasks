FROM openjdk:21-slim
WORKDIR /usr/app
ADD ./target/*.jar ./app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "./app.jar"]
