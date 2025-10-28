
# Patient Management System - Microservices

This project is a complete, microservices-based application for managing patient data. It demonstrates a modern, distributed architecture using Java, Spring Boot, Docker, gRPC, and a Zookeeper-less Kafka (Kraft) setup.

## 1. System Architecture

The application is composed of several independent services that communicate via REST APIs, gRPC, and asynchronous events. An API Gateway acts as the single entry point for all client requests, providing routing and centralized security.

![project-architecture.png](/patient-management/project-architecture.png)

### Request Flow Explained
1.  A client authenticates against the **Auth Service** via the **API Gateway** to get a JWT.
2.  The client then makes requests to other services (e.g., Patient Service) by passing the JWT in the `Authorization` header.
3.  The **API Gateway** intercepts these requests, validates the JWT with the **Auth Service**, and then routes the request to the appropriate downstream service.
4.  When a new patient is created, the **Patient Management Service** persists the data, makes a synchronous gRPC call to the **Billing Service**, and publishes an asynchronous event to Kafka.
5.  The **Analytics Service**, which constantly listens to the Kafka topic, consumes this event for processing. It is not called directly by any other service.

## 2. Services Overview

| Service | Port(s) | Primary Responsibility |
| :--- | :--- | :--- |
| **API Gateway** | `4004` | Routes all incoming traffic, enforces security with JWT validation. |
| **Authentication Service** | `4005` | Handles user login, issues and validates JSON Web Tokens (JWT). |
| **Patient Management Service**| `4000` | Provides CRUD operations for patient records. |
| **Billing Service** | `9001` | Exposes a gRPC endpoint to create patient billing accounts. |
| **Analytics Service** | `4002` | Consumes patient events from Kafka for analytical purposes. |

## 3. Technology Stack

*   **Backend**: Java 21, Spring Boot 3.x
*   **Gateway**: Spring Cloud Gateway
*   **Security**: Spring Security, JSON Web Tokens (JWT)
*   **Database**: PostgreSQL, Spring Data JPA
*   **Messaging**: Apache Kafka (in KRaft mode, without Zookeeper)
*   **RPC**: gRPC with Protocol Buffers
*   **Containerization**: Docker & Docker Compose
*   **Build Tool**: Maven

## 4. Getting Started: Running the Full System

The entire application stack is orchestrated to run on a unified Docker network.

### Prerequisites
*   [Git](https://git-scm.com/)
*   [Docker](https://www.docker.com/products/docker-desktop/) and Docker Compose (v20.10.0+ recommended)

### Setup Instructions

1.  **Clone the Repository**
    ```bash
    git clone <your-repository-url>
    cd <your-repository-name>
    ```

2.  **Create an Environment File**
    Create a file named `.env` in the root directory. This file will provide the configuration for all services.
    ```bash
    # Example .env content
    # JWT SECRET (Must be Base64 encoded)
    JWT_SECRET=TXlTdXBlclNlY3JldEtleUZvckp3dEVuY29kaW5nX0NoYW5nZVRoaXMxMjMh

    # Auth Service DB
    AUTH_DB_USER=admin_user
    AUTH_DB_PASSWORD=password
    AUTH_DB_NAME=db

    # Patient Service DB
    PATIENT_DB_USER=admin_user
    PATIENT_DB_PASSWORD=password
    PATIENT_DB_NAME=db

    # Kafka Cluster ID (Generate a new one with `kafka-storage.sh random-uuid`)
    KAFKA_CLUSTER_ID=l_AbCDeFgHiJkLmNoPqRsA
    ```

3.  **Build and Run with Docker Compose**
    From the root directory, run the following command. This will build the Docker images for each service and start all containers on a shared internal network.

    ```bash
    docker-compose up --build
    ```
    To stop the application, press `Ctrl+C` and then run `docker-compose down`.

### `docker-compose.yml`

This file orchestrates the entire application. All services are connected to a common `internal` network, allowing them to communicate using their service names.

```yaml
version: '3.8'

services:
  # --- Infrastructure ---
  auth-service-db:
    image: postgres:14
    container_name: auth-service-db
    environment:
      POSTGRES_USER: ${AUTH_DB_USER}
      POSTGRES_PASSWORD: ${AUTH_DB_PASSWORD}
      POSTGRES_DB: ${AUTH_DB_NAME}
    ports: [ "5433:5432" ]
    networks: [ internal ]

  patient-service-db:
    image: postgres:14
    container_name: patient-service-db
    environment:
      POSTGRES_USER: ${PATIENT_DB_USER}
      POSTGRES_PASSWORD: ${PATIENT_DB_PASSWORD}
      POSTGRES_DB: ${PATIENT_DB_NAME}
    ports: [ "5432:5432" ]
    networks: [ internal ]

  kafka:
    image: confluentinc/cp-kafka:latest
    container_name: kafka
    ports: [ "29092:29092" ]
    environment:
      KAFKA_NODE_ID: 1
      KAFKA_PROCESS_ROLES: 'broker,controller'
      KAFKA_CONTROLLER_QUORUM_VOTERS: '1@kafka:9093'
      KAFKA_LISTENERS: 'INTERNAL://:9092,EXTERNAL://:29092,CONTROLLER://:9093'
      KAFKA_ADVERTISED_LISTENERS: 'INTERNAL://kafka:9092,EXTERNAL://localhost:29092'
      KAFKA_INTER_BROKER_LISTENER_NAME: 'INTERNAL'
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: 'CONTROLLER:PLAINTEXT,INTERNAL:PLAINTEXT,EXTERNAL:PLAINTEXT'
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      KAFKA_TRANSACTION_STATE_LOG_MIN_ISR: 1
      KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR: 1
      CLUSTER_ID: ${KAFKA_CLUSTER_ID}
    networks: [ internal ]

  # --- Application Services ---
  auth-service:
    build: ./auth-service
    container_name: auth-service
    depends_on: [ auth-service-db ]
    ports: [ "4005:4005" ]
    environment:
      - SPRING_DATASOURCE_URL=jdbc:postgresql://auth-service-db:5432/db
      - SPRING_DATASOURCE_USERNAME=${AUTH_DB_USER}
      - SPRING_DATASOURCE_PASSWORD=${AUTH_DB_PASSWORD}
      - JWT_SECRET=${JWT_SECRET}
    networks: [ internal ]

  billing-service:
    build: ./billing-service
    container_name: billing-service
    ports: [ "4001:4001", "9001:9001" ]
    networks: [ internal ]

  patient-service:
    build: ./patient-service
    container_name: patient-service
    depends_on: [ patient-service-db, kafka, billing-service ]
    ports: [ "4000:4000" ]
    environment:
      - SPRING_DATASOURCE_URL=jdbc:postgresql://patient-service-db:5432/db
      - SPRING_DATASOURCE_USERNAME=${PATIENT_DB_USER}
      - SPRING_DATASOURCE_PASSWORD=${PATIENT_DB_PASSWORD}
      - SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9092
      - BILLING_SERVICE_ADDRESS=billing-service
      - BILLING_SERVICE_GRPC_PORT=9001
    networks: [ internal ]

  analytics-service:
    build: ./analytics-service
    container_name: analytics-service
    depends_on: [ kafka ]
    environment:
      - SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9092
    networks: [ internal ]

  api-gateway:
    build: ./api-gateway
    container_name: api-gateway
    depends_on: [ auth-service, patient-service ]
    ports: [ "4004:4004" ]
    environment:
      - AUTH_SERVICE_URL=http://auth-service:4005
    networks: [ internal ]

networks:
  internal:
    driver: bridge
```

## 5. Dockerization Strategy

All Java-based services in this project share a consistent, multi-stage `Dockerfile` pattern for optimized and lean container images.

*   **Builder Stage**: Uses a full Maven JDK image to build the application from source and download dependencies.
*   **Runner Stage**: Uses a slim OpenJDK image and copies only the final `.jar` file from the builder stage, resulting in a smaller and more secure production image.

**Example `Dockerfile` Template:**
```dockerfile
# Stage 1: Build the application
FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package

# Stage 2: Create the final runtime image
FROM openjdk:21-jdk-slim AS runner
WORKDIR /app
# The JAR name is the only part that changes between services
COPY --from=builder /app/target/your-service-name-0.0.1-SNAPSHOT.jar ./app.jar
EXPOSE <port>
ENTRYPOINT ["java","-jar","app.jar"]
```

## 6. How to Use the System (Example Workflow)

**Note:** This workflow requires a user to exist in the `auth-service-db`. You can connect to the database on `localhost:5433` and manually insert a user with a BCrypt-hashed password.

### Step 1: Log in to get a JWT
Send a `POST` request to the API Gateway's authentication endpoint.
```bash
curl -X POST http://localhost:4004/auth/login \
-H "Content-Type: application/json" \
-d '{ "email": "your-user@example.com", "password": "your-password" }'
```
The response will contain your JWT. Copy this token.

### Step 2: Create a New Patient
Send a `POST` request to the patient endpoint, including the JWT in the `Authorization` header.
```bash
# Replace <YOUR_JWT_TOKEN> with the token from Step 1
export TOKEN=<YOUR_JWT_TOKEN>

curl -X POST http://localhost:4004/api/patients \
-H "Content-Type: application/json" \
-H "Authorization: Bearer $TOKEN" \
-d '{
    "name": "Jane Doe",
    "email": "jane.doe@example.com",
    "address": "456 Oak Ave, Othertown, USA",
    "dateOfBirth": "1992-05-20"
}'
```

This single API call triggers the full communication flow between the services as described in the architecture section.

## 7. Individual Service Docu
For more detailed information about a specific service, including its API endpoints, configuration, and internal logic, please refer to the `README.md` file within each service's directory:
*   [API Gateway](./api-gateway/README.md)
*   [Authentication Service](./auth-service/README.md)
*   [Patient Management Service](./patient-service/README.md)
*   [Billing Service](./billing-service/README.md)
*   [Analytics Service](./analytics-service/README.md)
