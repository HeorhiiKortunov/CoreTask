FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY target/maven-0.0.1-SNAPSHOT.jar /app/coretask.jar
ENTRYPOINT ["java", "-jar", "coretask.jar"]