FROM openjdk:8-alpine
EXPOSE 8080
WORKDIR /app
COPY . /app
RUN ./mvnw package -DskipTests
ENTRYPOINT ["java","-jar","target/tortuga-api-0.0.1.1-SNAPSHOT.jar"]
