# Build stage
FROM eclipse-temurin:22-jdk-alpine as build
WORKDIR /workspace/app

# Copy maven/gradle files
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

# Add executable permission to mvnw
RUN chmod +x mvnw

# Build project
RUN ./mvnw clean package -DskipTests

# Run stage
FROM eclipse-temurin:22-jdk-alpine
COPY --from=build /workspace/app/target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]