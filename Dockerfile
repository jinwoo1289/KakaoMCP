FROM eclipse-temurin:21-jdk

WORKDIR /app
COPY . .

RUN chmod +x ./gradlew
RUN ./gradlew clean bootJar

EXPOSE 8080
CMD ["sh", "-c", "java -jar build/libs/*.jar"]
