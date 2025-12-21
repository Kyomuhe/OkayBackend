FROM eclipse-temurin:17-jdk

ARG JAR_FILE=target/*.jar

COPY ${JAR_FILE} app.jar

EXPOSE 7071

ENTRYPOINT ["java", "-jar", "/app.jar"]