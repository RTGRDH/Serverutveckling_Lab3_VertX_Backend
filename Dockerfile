FROM openjdk:8
COPY target/vertx-codeone-conduit-1.0-SNAPSHOT-*.jar /vertx-codeone-conduit-fat.jar
CMD ["java", "-jar", "/vertx-codeone-conduit-fat.jar"]