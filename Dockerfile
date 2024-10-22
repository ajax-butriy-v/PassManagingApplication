FROM amazoncorretto:17-alpine3.17 AS builder
WORKDIR /workspace/app


FROM builder AS pass-manager-svc-builder
COPY pass-manager-svc/build/libs/*.jar ./pass-manager-svc.jar
EXPOSE 8081
CMD ["java", "-jar", "./pass-manager-svc.jar"]

FROM builder AS gateway-builder
COPY gateway/build/libs/*.jar ./gateway.jar
EXPOSE 8080
CMD ["java", "-jar", "./gateway.jar"]
