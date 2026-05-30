# AI Document Intelligence Platform

An end-to-end document processing system that uploads PDFs, extracts text using OCR, sends the content to an LLM API for structured data extraction, and displays results on a React dashboard.

Built as a portfolio project to explore how Java microservices, Python OCR pipelines, and LLM APIs can be combined into a practical workflow — inspired by AI automation work done at Mutual of Omaha (2025).

## What It Does

- User uploads a PDF or image via the React frontend
- File is stored in AWS S3 and an event is published to Kafka
- A Python service runs OCR on the document using AWS Textract
- Extracted text is sent to OpenAI API to pull out structured fields (dates, amounts, names, etc.)
- Results are stored in PostgreSQL and displayed on a React dashboard
- Dashboard shows document status, extracted fields, and processing history

## Tech Stack

| Layer | Technology |
|---|---|
| Frontend | React 18, TypeScript |
| Backend | Java 17, Spring Boot 3.x |
| OCR | Python 3.11, FastAPI, AWS Textract |
| LLM | OpenAI API (GPT-4o) |
| Messaging | Apache Kafka |
| Database | PostgreSQL, AWS DynamoDB |
| Cloud | AWS S3, Lambda, SQS, EC2 |
| DevOps | Docker, Docker Compose, GitHub Actions |

## How It Works

```
React UI → Upload Service (Spring Boot) → S3 + Kafka
                                         ↓
                              Processing Service (Spring Boot)
                                         ↓
                              Python OCR Service (AWS Textract)
                                         ↓
                              LLM Extraction (OpenAI API)
                                         ↓
                              PostgreSQL → Analytics Service
                                         ↓
                              React Dashboard
```

## Services

- **Upload Service** — handles file upload, generates S3 presigned URLs, publishes Kafka event
- **Processing Service** — Kafka consumer, coordinates OCR and extraction steps
- **Python OCR Pipeline** — FastAPI app that calls AWS Textract and cleans extracted text
- **LLM Extraction** — sends cleaned text to OpenAI API, returns structured JSON
- **Analytics Service** — aggregates results for the dashboard

## Running Locally

Prerequisites: Java 17+, Node.js 18+, Python 3.11+, Docker, AWS CLI configured

```bash
git clone https://github.com/Rajeshdevandla/ai-document-intelligence-platform.git
cd ai-document-intelligence-platform
cp .env.example .env
# Add your AWS credentials and OpenAI API key to .env
docker-compose up -d
```

Frontend: `http://localhost:3000`
Upload API: `http://localhost:8081/api`
Python Pipeline: `http://localhost:8090/docs`

## Sample Extraction Output

```json
{
  "document_type": "Invoice",
  "vendor": "Acme Corp",
  "invoice_date": "2024-03-15",
  "total_amount": 4590.00,
  "line_items": [
    { "description": "Cloud Services", "amount": 4590.00 }
  ],
  "confidence_score": 0.94
}
```

## What I Applied Here

- Splitting responsibilities across small, focused Spring Boot services
- Kafka for async, decoupled event flow between upload and processing
- Integrating a Python FastAPI service with a Java backend
- Calling OpenAI API from a Spring Boot service and parsing the structured response
- React dashboard with real-time status updates via polling
- GitHub Actions CI to build and test on every push

## Background

Inspired by real work at Mutual of Omaha (2025) where I integrated the OpenAI API to automate an internal workflow and reduce repetitive manual processing steps. This project explores that idea more fully as a standalone system.

---

**Rajesh Kumar** — Full Stack Java Developer | Chicago, IL
[Portfolio](https://rajeshdevandla.github.io) · [GitHub](https://github.com/Rajeshdevandla)
