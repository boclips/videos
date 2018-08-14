FROM openjdk:8-jre-slim
COPY build/libs/video-ingestor-*.jar /opt/app.jar
WORKDIR /opt
CMD ["java", "-jar", "app.jar"]
