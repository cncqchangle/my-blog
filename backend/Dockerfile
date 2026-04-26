FROM maven:3.9.11-eclipse-temurin-25 AS build
WORKDIR /workspace

COPY pom.xml ./
RUN mvn -B -q -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -B -q -DskipTests package

FROM eclipse-temurin:25-jre-jammy
WORKDIR /app

ENV TZ=Asia/Shanghai

RUN addgroup --system spring && adduser --system --ingroup spring spring

COPY --from=build /workspace/target/my-blog-0.0.1-SNAPSHOT.jar /app/app.jar
COPY --from=build /workspace/src/main/resources/static /app/static

EXPOSE 8080

USER spring

ENTRYPOINT ["java", "-XX:+UseZGC", "-jar", "/app/app.jar"]
