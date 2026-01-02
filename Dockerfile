# Perform the extraction in a separate builder container
FROM eclipse-temurin:17-jre AS builder
WORKDIR /builder
# This points to the built jar file in the target folder
ARG JAR_FILE=target/*.jar
# Copy the jar file to the working directory and rename it to application.jar
COPY ${JAR_FILE} application.jar
# Extract the jar file using an efficient layout
RUN java -Djarmode=tools -jar application.jar extract --layers --destination extracted

# This is the runtime container
FROM eclipse-temurin:17-jre
WORKDIR /application
# Copy the extracted jar contents from the builder container into the working directory in the runtime container
# Every copy step creates a new docker layer
# This allows docker to only pull the changes it really needs
COPY --from=builder /builder/extracted/dependencies/ ./
COPY --from=builder /builder/extracted/spring-boot-loader/ ./
COPY --from=builder /builder/extracted/snapshot-dependencies/ ./
COPY --from=builder /builder/extracted/application/ ./

# Default environment variables (can be overridden by docker-compose)
ENV SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/hogwarts
ENV SPRING_DATASOURCE_USERNAME=root
ENV SPRING_DATASOURCE_PASSWORD=1234

ENTRYPOINT ["java", "-jar", "application.jar"]

EXPOSE 5001
LABEL authors="ravip"

