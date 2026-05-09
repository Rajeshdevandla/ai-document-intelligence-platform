# DocIQ — AI Document Intelligence Platform

## Project Overview

A production-grade, cloud-native document processing system built to demonstrate enterprise-level full-stack engineering, AI/LLM integration, and cloud architecture design.

## Technical Highlights

- **4 Java Spring Boot microservices** with Kafka event streaming between each stage
- **Python FastAPI pipeline** for OCR using AWS Textract + Tesseract
- **3 LLM providers** (OpenAI GPT-4o, AWS Bedrock Claude 3, Google Gemini) with pluggable interface
- **React 18 + TypeScript** dashboard with real-time analytics (Recharts)
- **AWS infrastructure** provisioned with Terraform (S3, DynamoDB, MSK, ECS, RDS)
- **GitHub Actions CI/CD** with matrix builds, ECR push, ECS rolling deploy

## Why This Project?

Document intelligence is a high-value enterprise use case. This project demonstrates:
- Designing distributed systems with async event streaming
- Integrating multiple cloud AI services with a clean abstraction layer
- Building production-ready code with proper testing, error handling, and observability

## About the Developer

**Rajesh Kumar** — Full Stack Developer (Java · Spring Boot · AWS · React · AI/LLMs)
- 4+ years building enterprise web applications
- Based in Chicago, IL
- Portfolio: https://rajeshdevandla.github.io
- LinkedIn: https://linkedin.com/in/rajeshdevandla
- GitHub: https://github.com/Rajeshdevandla
