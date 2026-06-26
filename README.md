# AI Document Intelligence Platform

> **End-to-end document processing** — upload PDFs, extract structured data using OCR + LLM, and view results on a real-time React dashboard.

[![Java 17](https://img.shields.io/badge/Java-17-007396.svg)](https://www.java.com/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-6DB33F.svg)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18-61DAFB.svg)](https://reactjs.org/)
[![Python](https://img.shields.io/badge/Python-3.11-blue.svg)](https://www.python.org/)
[![AWS](https://img.shields.io/badge/AWS-Textract%20%7C%20S3%20%7C%20Kafka-FF9900.svg)](https://aws.amazon.com/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED.svg)](https://www.docker.com/)

---

## Live Demo

> 🚀 **[Coming soon — deploying to AWS]**
>
> To run locally, follow the [Quick Start](#quick-start) below.

---

## What Problem This Solves

Manual data entry from invoices, contracts, and insurance documents is slow, error-prone, and expensive. This platform automates the full pipeline: a document goes in, structured JSON data comes out — extracted by OCR and validated by an LLM — with zero manual intervention.

**Real-world inspiration:** Built based on AI automation work done at Mutual of Omaha (2025), where integrating OpenAI API reduced repetitive manual processing steps on an internal workflow.

---

## Demo

**Input:** Upload an invoice PDF via the React dashboard

**Extracted output (JSON):**
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

**Dashboard view:** Document status (Uploaded → OCR → Extracted → Complete), extracted fields table, processing history with timestamps.

---

## Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│              AI Document Intelligence Platform                   │
└─────────────────────────────────────────────────────────────────┘

  USER UPLOAD FLOW
  ─────────────────
  React Frontend (TypeScript)
       │  HTTP POST /upload
       ▼
  Upload Service (Spring Boot)
       │  ├─ store file → AWS S3
       │  └─ publish event → Apache Kafka
       ▼
  Processing Service (Spring Boot, Kafka consumer)
       │
       ├──────────────────────────────────┐
       │                                  │
       ▼                                  ▼
  Python OCR Service                 (async, decoupled)
  (FastAPI + AWS Textract)
       │  extracted raw text
       ▼
  LLM Extraction (OpenAI GPT-4o)
       │  structured JSON fields
       ▼
  PostgreSQL (results) + DynamoDB (metadata)
       │
       ▼
  Analytics Service
       │
       ▼
  React Dashboard (real-time status via polling)

  SERVICES
  ────────
  Upload Service    → handles file intake, S3 presigned URLs, Kafka publish
  Processing Service → Kafka consumer, coordinates OCR + extraction steps
  Python OCR Service → FastAPI app calling AWS Textract, text cleaning
  LLM Extraction    → Spring Boot calling OpenAI API, structured JSON output
  Analytics Service → aggregates results, feeds the dashboard
```

---

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

---

## Quick Start

**Prerequisites:** Java 17+, Node.js 18+, Python 3.11+, Docker, AWS CLI configured

### 1. Clone and configure

```bash
git clone https://github.com/Rajeshdevandla/ai-document-intelligence-platform.git
cd ai-document-intelligence-platform
cp .env.example .env
# Add your AWS credentials and OpenAI API key to .env
```

Required environment variables:

| Variable | Description |
|---|---|
| `AWS_ACCESS_KEY_ID` | IAM user access key |
| `AWS_SECRET_ACCESS_KEY` | IAM user secret key |
| `AWS_REGION` | AWS region (e.g. `us-east-1`) |
| `AWS_S3_BUCKET` | S3 bucket for document storage |
| `OPENAI_API_KEY` | OpenAI API key |
| `POSTGRES_URL` | PostgreSQL connection string |

### 2. Start all services

```bash
docker-compose up -d
```

This starts: Kafka, PostgreSQL, Upload Service, Processing Service, Python OCR Service, Analytics Service, and React frontend.

| Service | URL |
|---|---|
| React Dashboard | http://localhost:3000 |
| Upload API | http://localhost:8081/api |
| Python OCR Pipeline | http://localhost:8090/docs |

---

## Project Structure

```
ai-document-intelligence-platform/
├── frontend/                  # React 18 + TypeScript dashboard
├── backend/
│   ├── upload-service/        # Spring Boot — file intake + Kafka publish
│   ├── processing-service/    # Spring Boot — Kafka consumer + orchestration
│   ├── analytics-service/     # Spring Boot — dashboard aggregation
│   └── llm-extraction/        # Spring Boot — OpenAI API integration
├── python-pipeline/           # FastAPI + AWS Textract OCR service
├── infra/
│   ├── docker-compose.yml
│   └── k8s/                   # Kubernetes manifests
├── docs/                      # Architecture diagrams
├── .env.example
└── README.md
```

---

## Key Engineering Decisions

**Kafka for async processing.** Upload and processing are fully decoupled — the upload service returns immediately after storing to S3 and publishing the event. The processing service picks it up asynchronously. This means the UI is never blocked waiting for OCR or LLM calls.

**Python for OCR, Java for orchestration.** AWS Textract has the best Python SDK support. The Java backend handles business logic, routing, and persistence. The two communicate via a clean REST contract, keeping each service in its strongest language.

**LLM for structured extraction.** Raw OCR output is messy. Rather than writing fragile regex parsers for every document type, the extracted text is sent to GPT-4o with a structured output prompt. This handles invoice layouts, contract formats, and table structures without custom parsing logic per document type.

---

## CI/CD

GitHub Actions pipeline on every push:

- Build Java services with Maven
- Run unit and integration tests
- Build Python service and run pytest
- Build Docker images
- Deploy to staging (on merge to `main`)

---

## What I'd Build Next

- **More document types** — add support for bank statements, W-2s, medical records with type-specific extraction prompts
- **Confidence thresholds** — route low-confidence extractions to human review queue
- **Streaming status** — replace polling with WebSocket for real-time dashboard updates
- **Live hosted demo** — deploy to AWS ECS with a public demo endpoint

---

## Related Projects

- [AskDocs AI](https://github.com/Rajeshdevandla/askdocs-ai) — PDF RAG chatbot using Amazon Bedrock and FAISS
- [AgentFlow](https://github.com/Rajeshdevandla/agent-flow) — Multi-agent orchestration system with Constitutional AI safety layer

---

*Built by [Rajesh Kumar](https://rajeshdevandla.github.io) — Full Stack Java & AI Developer | Chicago, IL*
