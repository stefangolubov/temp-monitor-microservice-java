FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/tempmonitor-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 8000
ENTRYPOINT ["java", "-jar", "app.jar"]