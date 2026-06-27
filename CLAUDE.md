# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

`niah-generator` is a Spring Boot 4.1.0 application (Java 21) intended to generate Needle-In-A-Haystack (NIAH) evaluation datasets for LLM benchmarking. The project is a skeleton with the core structure in place but application logic not yet implemented.

## Commands

```bash
# Build
./mvnw clean package

# Run
./mvnw spring-boot:run

# Run tests
./mvnw test

# Run a single test class
./mvnw test -Dtest=NiahGeneratorApplicationTests

# Build without tests
./mvnw clean package -DskipTests
```

## Architecture

- **Entry point**: `src/main/java/com/heptafox/niah/NiahGeneratorApplication.java`
- **Package root**: `com.heptafox.niah`
- **Config**: `src/main/resources/application.yaml`
- **Stack**: Spring Boot 4.1.0, Spring MVC (webmvc), Bean Validation, Actuator

Dependencies include `spring-boot-starter-webmvc` (REST API), `spring-boot-starter-validation` (request validation), and `spring-boot-starter-actuator` (health/metrics endpoints).
