FROM gcr.io/distroless/java17-debian11:nonroot-arm64
WORKDIR /app
COPY target/queryStatusTracking-0.0.1-SNAPSHOT.jar queryStatusTracking-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java","-jar","queryStatusTracking-0.0.1-SNAPSHOT.jar"]