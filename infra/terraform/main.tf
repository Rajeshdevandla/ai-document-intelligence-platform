terraform {
  required_version = ">= 1.5.0"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.30"
    }
  }
  backend "s3" {
    bucket = "dociq-terraform-state"
    key    = "ai-doc-platform/terraform.tfstate"
    region = "us-east-1"
  }
}

provider "aws" {
  region = var.aws_region
  default_tags {
    tags = {
      Project     = var.project_name
      Environment = var.environment
      ManagedBy   = "terraform"
      Owner       = "rajesh-kumar"
    }
  }
}

# ─────────────────────────────────────────────
# S3 — Document Storage
# ─────────────────────────────────────────────
resource "aws_s3_bucket" "documents" {
  bucket = "${var.project_name}-uploads-${var.environment}"
}

resource "aws_s3_bucket_versioning" "documents" {
  bucket = aws_s3_bucket.documents.id
  versioning_configuration { status = "Enabled" }
}

resource "aws_s3_bucket_server_side_encryption_configuration" "documents" {
  bucket = aws_s3_bucket.documents.id
  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"
    }
  }
}

resource "aws_s3_bucket_public_access_block" "documents" {
  bucket                  = aws_s3_bucket.documents.id
  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

resource "aws_s3_bucket_lifecycle_configuration" "documents" {
  bucket = aws_s3_bucket.documents.id
  rule {
    id     = "archive-old-documents"
    status = "Enabled"
    transition {
      days          = 90
      storage_class = "STANDARD_IA"
    }
    transition {
      days          = 365
      storage_class = "GLACIER"
    }
  }
}

# ─────────────────────────────────────────────
# DynamoDB — Document Metadata
# ─────────────────────────────────────────────
resource "aws_dynamodb_table" "document_metadata" {
  name           = "${var.project_name}-metadata-${var.environment}"
  billing_mode   = "PAY_PER_REQUEST"
  hash_key       = "documentId"

  attribute {
    name = "documentId"
    type = "S"
  }
  attribute {
    name = "uploadedBy"
    type = "S"
  }
  attribute {
    name = "status"
    type = "S"
  }

  global_secondary_index {
    name            = "uploadedBy-index"
    hash_key        = "uploadedBy"
    projection_type = "ALL"
  }

  global_secondary_index {
    name            = "status-index"
    hash_key        = "status"
    projection_type = "ALL"
  }

  point_in_time_recovery { enabled = true }
  server_side_encryption  { enabled = true }

  ttl {
    attribute_name = "expiresAt"
    enabled        = true
  }
}

# ─────────────────────────────────────────────
# MSK (Managed Kafka)
# ─────────────────────────────────────────────
resource "aws_msk_cluster" "dociq" {
  cluster_name           = "${var.project_name}-kafka-${var.environment}"
  kafka_version          = "3.5.1"
  number_of_broker_nodes = 3

  broker_node_group_info {
    instance_type   = "kafka.m5.large"
    client_subnets  = var.private_subnet_ids
    security_groups = [aws_security_group.msk.id]
    storage_info {
      ebs_storage_info { volume_size = 100 }
    }
  }

  encryption_info {
    encryption_in_transit { client_broker = "TLS" }
  }

  logging_info {
    broker_logs {
      cloudwatch_logs {
        enabled   = true
        log_group = aws_cloudwatch_log_group.msk.name
      }
    }
  }
}

resource "aws_security_group" "msk" {
  name   = "${var.project_name}-msk-sg"
  vpc_id = var.vpc_id

  ingress {
    from_port   = 9092
    to_port     = 9096
    protocol    = "tcp"
    cidr_blocks = [var.vpc_cidr]
  }
}

resource "aws_cloudwatch_log_group" "msk" {
  name              = "/aws/msk/${var.project_name}"
  retention_in_days = 14
}

# ─────────────────────────────────────────────
# RDS PostgreSQL — Analytics
# ─────────────────────────────────────────────
resource "aws_db_instance" "analytics" {
  identifier             = "${var.project_name}-analytics-${var.environment}"
  engine                 = "postgres"
  engine_version         = "15.4"
  instance_class         = "db.t3.medium"
  allocated_storage      = 20
  max_allocated_storage  = 100
  db_name                = "dociq_analytics"
  username               = var.db_username
  password               = var.db_password
  multi_az               = var.environment == "prod"
  skip_final_snapshot    = var.environment != "prod"
  storage_encrypted      = true
  backup_retention_period = 7
  deletion_protection    = var.environment == "prod"
}

# ─────────────────────────────────────────────
# ECS Cluster
# ─────────────────────────────────────────────
resource "aws_ecs_cluster" "dociq" {
  name = "${var.project_name}-cluster-${var.environment}"

  setting {
    name  = "containerInsights"
    value = "enabled"
  }
}

resource "aws_cloudwatch_log_group" "services" {
  for_each = toset([
    "upload-service", "processing-service",
    "extraction-service", "analytics-service", "python-pipeline"
  ])
  name              = "/ecs/${var.project_name}/${each.key}"
  retention_in_days = 30
}
