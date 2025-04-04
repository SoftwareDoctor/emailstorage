FROM openjdk:21-jdk-slim

WORKDIR /emailStorage

# Maven
RUN apt-get update && \
    apt-get install -y maven && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Copy the pom.xml file to the working directory
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the source code to the working directory
COPY src ./src

# Build the application (download dependencies and compile)
RUN mvn clean install -DskipTests

# Copy the generated JAR to the working directory
COPY target/emailStorage-0.0.1-SNAPSHOT.jar emailStorage.jar

# Expose the port on which the application runs
EXPOSE 8083

# Command to run the JAR
CMD ["java", "-jar", "emailStorage.jar"]
