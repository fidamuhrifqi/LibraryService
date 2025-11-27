FROM eclipse-temurin:21-jdk-jammy

WORKDIR /libraryApp

COPY target/*.jar libraryApp.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "libraryApp.jar"]
