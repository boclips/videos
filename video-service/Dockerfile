FROM openjdk:11-jre-slim
COPY build/libs/video-service-*.jar /opt/app.jar
WORKDIR /opt
CMD ["java", "-jar", "app.jar"]
