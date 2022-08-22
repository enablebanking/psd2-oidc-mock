FROM eclipse-temurin:18

RUN mkdir /opt/app
COPY build/libs/oidc-mock-0.0.1-SNAPSHOT.jar /opt/app/app.jar
CMD ["java", "-jar", "/opt/app/app.jar"]
