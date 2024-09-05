FROM amazoncorretto:17-alpine3.17
WORKDIR /workspace/app

COPY /build/libs/*.jar ./app.jar
EXPOSE 8080
CMD ["java", "-jar", "./app.jar"]
