FROM maven:3-jdk-8 AS build

COPY src /home/app/src
COPY pom.xml /home/app

RUN mvn -f /home/app/pom.xml clean install

FROM openjdk:8

MAINTAINER CyberArk Software
RUN mkdir -p /app
COPY --from=build /home/app/target /app
