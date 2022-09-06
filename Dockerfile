FROM eclipse-temurin:11-jdk-alpine as builder
WORKDIR /app

# Copy maven executable to the image
COPY mvnw .
COPY .mvn .mvn

# Copy the pom.xml file
COPY pom.xml .

# Fix permissions for mvnw executable
RUN chmod +x mvnw

# Download all dependecies
RUN ./mvnw dependency:go-offline -B

# Copy the project source
COPY ./src ./src
COPY ./pom.xml ./pom.xml

RUN ./mvnw compile assembly:single -DskipTests

FROM eclipse-temurin:11-jre-alpine
WORKDIR /app

# Copy builded application to the stage
COPY --from=builder /app/target/TimingBot.jar /app/app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]