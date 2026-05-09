<div align="center">

# 🧠 AI Document Intelligence Platform

**Enterprise-grade AI-powered document processing system**

[![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=java)](https://openjdk.org/projects/jdk/17/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2-6DB33F?style=for-the-badge&logo=spring-boot)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18-61DAFB?style=for-the-badge&logo=react)](https://reactjs.org/)
[![Kafka](https://img.shields.io/badge/Kafka-3.6-231F20?style=for-the-badge&logo=apache-kafka)](https://kafka.apache.org/)
[![AWS](https://img.shields.io/badge/AWS-Cloud-FF9900?style=for-the-badge&logo=amazon-aws)](https://aws.amazon.com/)
[![Python](https://img.shields.io/badge/Python-3.11-3776AB?style=for-the-badge&logo=python)](https://www.python.org/)
[![Docker](https://img.shields.io/badge/Docker-Containerized-2496ED?style=for-the-badge&logo=docker)](https://www.docker.com/)
[![License](https://img.shields.io/badge/License-MIT-green.svg?style=for-the-badge)](LICENSE)

*Processes PDFs and images → OCR → LLM Extraction → Structured JSON → Analytics Dashboard*

[🚀 Live Demo](#demo) • [📖 API Docs](docs/api-contracts.md) • [🏗️ Architecture](#architecture) • [🛠️ Setup](#setup)

</div>

---

## 📌 What This System Does

| Step | Technology | Description |
|------|-----------|-------------|
| 1️⃣ Upload | React + S3 | Drag-and-drop PDF/image upload via presigned URLs |
| 2️⃣ Store | AWS S3 + DynamoDB | File stored securely; metadata persisted |
| 3️⃣ Stream | Apache Kafka | `document.uploaded` event triggers async pipeline |
| 4️⃣ OCR | AWS Textract / Tesseract | Raw text extracted from documents |
| 5️⃣ Extract | GPT-4o / Claude 3 / Gemini | Structured JSON data extracted by LLM |
| 6️⃣ Store Results | PostgreSQL + DynamoDB | Structured fields stored for querying |
| 7️⃣ Analyze | Spring Boot Analytics Service | Metrics, trends, anomaly detection |
| 8️⃣ Visualize | React Dashboard | Charts, document viewer, confidence scores |

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                        REACT FRONTEND (TypeScript)                   │
│    Upload Page │ Status Page │ Analytics Dashboard │ Document Viewer  │
└──────────────────────────┬──────────────────────────────────────────┘
                           │ REST / JWT
         ┌─────────────────┼─────────────────┐
         ▼                 ▼                 ▼
┌─────────────┐  ┌──────────────────┐  ┌──────────────────┐
│  Upload     │  │  Processing      │  │  Analytics       │
│  Service    │  │  Service         │  │  Service         │
│  :8081      │  │  (Kafka Consumer)│  │  :8084           │
└──────┬──────┘  └────────┬─────────┘  └──────────────────┘
       │                  │
       ▼                  ▼
┌─────────────────────────────────┐    ┌──────────────────────────────┐
│     Apache Kafka (Event Bus)    │    │     LLM Extraction Service   │
│  document.uploaded              │───▶│     :8083                    │
│  document.ocr.completed         │    │  OpenAI GPT-4o               │
│  document.extracted             │    │  AWS Bedrock Claude          │
│  document.failed                │    │  Google Gemini               │
└─────────────────────────────────┘    └──────────────────────────────┘
         │                                        │
         ▼                                        ▼
┌──────────────────┐              ┌───────────────────────────────────┐
│  Python Pipeline │              │         AWS Infrastructure        │
│  OCR Processing  │              │  S3 │ DynamoDB │ Lambda │ SQS    │
│  Text Cleaning   │              │  EC2/EKS │ CloudWatch │ SNS      │
└──────────────────┘              └───────────────────────────────────┘
         │                                        │
         ▼                                        ▼
┌──────────────────────────────────────────────────────────────────┐
│                         Data Layer                                │
│           DynamoDB (metadata)  │  PostgreSQL (analytics)         │
└──────────────────────────────────────────────────────────────────┘
```

---

## 📂 Repository Structure

```
ai-document-intelligence-platform/
├── backend/
│   ├── upload-service/           # Handles file upload, presigned URLs, Kafka publish
│   ├── processing-service/       # Kafka consumer, orchestrates OCR + extraction
│   ├── extraction-service/       # LLM integration (OpenAI, Bedrock, Gemini)
│   ├── analytics-service/        # Metrics aggregation, dashboard APIs
│   └── common-libs/              # Shared DTOs, exceptions, Kafka config
├── frontend/                     # React 18 + TypeScript + Tailwind CSS
│   ├── src/
│   │   ├── components/           # Reusable UI components
│   │   ├── pages/                # Upload, Status, Dashboard, Viewer
│   │   ├── services/             # API clients
│   │   └── hooks/                # Custom React hooks
├── python-pipeline/
│   ├── ocr/                      # AWS Textract + Tesseract OCR
│   ├── preprocessing/            # Text cleaning, normalization
│   └── main.py                   # FastAPI server exposing /process endpoint
├── infra/
│   ├── terraform/                # AWS infrastructure as code
│   └── k8s/                      # Kubernetes manifests
├── docs/
│   ├── api-contracts.md          # OpenAPI specs for all services
│   └── portfolio.md              # Developer portfolio page
├── .github/workflows/
│   └── ci-cd.yml                 # GitHub Actions: build → test → dockerize → deploy
├── docker-compose.yml            # Local development orchestration
└── README.md
```

---

## 🧠 LLM Extraction Output

Every processed document produces a structured JSON payload:

```json
{
  "document_id": "doc-a1b2c3d4",
  "document_type": "Invoice",
  "entities": [
    { "type": "ORGANIZATION", "value": "Acme Corp", "confidence": 0.97 },
    { "type": "PERSON",       "value": "John Smith", "confidence": 0.94 }
  ],
  "dates": [
    { "label": "invoice_date", "value": "2024-03-15" },
    { "label": "due_date",     "value": "2024-04-15" }
  ],
  "totals": {
    "subtotal": 4250.00,
    "tax":       340.00,
    "total":    4590.00,
    "currency": "USD"
  },
  "line_items": [
    { "description": "Cloud Infrastructure Services", "quantity": 1, "unit_price": 4250.00 }
  ],
  "summary": "Invoice from Acme Corp to John Smith for cloud infrastructure services totaling $4,590 due April 15, 2024.",
  "anomalies": [],
  "llm_model":      "gpt-4o",
  "confidence_score": 0.96,
  "processing_time_ms": 1847
}
```

---

## 🚀 Quick Start

### Prerequisites
- Java 17+, Maven 3.9+
- Node.js 20+, npm 10+
- Python 3.11+
- Docker & Docker Compose
- AWS CLI configured

### 1. Clone & Configure

```bash
git clone https://github.com/Rajeshdevandla/ai-document-intelligence-platform.git
cd ai-document-intelligence-platform
cp .env.example .env
# Edit .env with your AWS credentials and API keys
```

### 2. Start All Services (Docker Compose)

```bash
docker-compose up -d
```

This starts: Kafka, Zookeeper, PostgreSQL, all 4 Java microservices, Python pipeline, and React frontend.

### 3. Access

| Service | URL |
|---------|-----|
| React Frontend | http://localhost:3000 |
| Upload Service API | http://localhost:8081/api |
| Processing Service | http://localhost:8082/api |
| Extraction Service | http://localhost:8083/api |
| Analytics Service | http://localhost:8084/api |
| Kafka UI | http://localhost:9000 |
| Python Pipeline | http://localhost:8090/docs |

---

## 📊 Analytics Dashboard Features

- **Total documents processed** — real-time counter
- **Processing time distribution** — histogram by document type
- **Error rate trending** — per-hour failure breakdown  
- **Document type breakdown** — pie chart (Invoice, Contract, Receipt, etc.)
- **LLM confidence scores** — distribution and outlier flagging
- **Trend charts** — 7-day / 30-day volume graphs

---

## 🧪 Testing

```bash
# Backend — JUnit 5 + Mockito
cd backend/upload-service && mvn test

# Frontend — Jest + React Testing Library + Cypress
cd frontend && npm test
cd frontend && npx cypress run

# Python pipeline — PyTest
cd python-pipeline && pytest tests/ -v --cov=.
```

---

## 🎥 Demo Script (for Recruiters)

1. **Upload** a PDF invoice via the drag-and-drop UI
2. **Watch** real-time Kafka event streaming in the status page
3. **View** the OCR raw text output panel
4. **See** the LLM extraction JSON with confidence scores
5. **Explore** the analytics dashboard (charts, filters, anomaly alerts)
6. **Review** the architecture diagram in `/infra/`

---

## 👨‍💻 About the Developer

<table>
<tr>
<td>

**Rajesh Kumar** — Full Stack Developer

🎯 **4+ years** building enterprise Java, Spring Boot, AWS & React systems  
📍 **Chicago, IL**  
🔗 **Portfolio:** [rajeshdevandla.github.io](https://rajeshdevandla.github.io)  
💼 **LinkedIn:** [linkedin.com/in/rajeshdevandla](https://linkedin.com/in/rajeshdevandla)  
🐙 **GitHub:** [github.com/Rajeshdevandla](https://github.com/Rajeshdevandla)

**Core Skills:** Java 17 · Spring Boot · AWS · React · Kafka · PostgreSQL · Docker · Kubernetes · LLM APIs

</td>
</tr>
</table>

---

## 🛠️ Tech Stack

| Layer | Technology |
|-------|-----------|
| Frontend | React 18, TypeScript, Tailwind CSS, Recharts, React Query |
| Backend | Java 17, Spring Boot 3.2, Spring Kafka, Spring Security |
| LLM | OpenAI GPT-4o, AWS Bedrock (Claude 3), Google Gemini |
| OCR | AWS Textract, Tesseract, Python 3.11 |
| Messaging | Apache Kafka 3.6, Zookeeper |
| Database | AWS DynamoDB, PostgreSQL 15, Redis |
| Cloud | AWS S3, Lambda, EC2/EKS, CloudWatch, SQS, SNS |
| DevOps | Docker, Kubernetes, GitHub Actions, Terraform |
| Testing | JUnit 5, Mockito, Cypress, PyTest, Testcontainers |

---

## 📄 License

MIT License — see [LICENSE](LICENSE) for details.

---

<div align="center">

⭐ **Star this repo** if it helped you or impressed you!

*Built with ❤️ by [Rajesh Kumar](https://rajeshdevandla.github.io)*

</div>
