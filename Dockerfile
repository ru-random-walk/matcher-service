FROM openjdk:21-jdk
EXPOSE 8080
ADD build/libs/matcher-service*.jar app.jar
CMD ["java", "-jar", "/app.jar"]