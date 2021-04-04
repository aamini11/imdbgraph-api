#FROM maven:3.6.3-openjdk-15 AS build
#COPY src src
#COPY pom.xml .
#RUN mvn clean package

#https://spring.io/blog/2018/11/08/spring-boot-in-a-container
#https://docs.docker.com/develop/develop-images/multistage-build/
FROM openjdk:16-jdk-alpine AS build
COPY mvnw .
COPY .mvn .mvn
COPY src src
COPY pom.xml .
RUN ./mvnw clean package

FROM openjdk:16-jdk-alpine
ARG JAR_FILE=target/*.jar
COPY --from=build ${JAR_FILE} app.jar
ENTRYPOINT ["java", "--enable-preview", "-jar", "/app.jar"]