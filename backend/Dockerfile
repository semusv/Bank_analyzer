FROM bellsoft/liberica-openjdk-alpine-musl:17
COPY ../target/bank-analyzer-0.1.0-SNAPSHOT.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]