FROM openjdk:11.0.3-jdk-slim
ARG JAR_FILE
COPY target/${JAR_FILE} app.jar
VOLUME /tmp
ENTRYPOINT ["java","-jar", "app.jar"]
EXPOSE 8000
