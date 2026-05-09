output "s3_bucket_name" {
  description = "Name of the S3 bucket for document uploads"
  value       = aws_s3_bucket.documents.bucket
}

output "dynamodb_table_name" {
  description = "DynamoDB table name for document metadata"
  value       = aws_dynamodb_table.document_metadata.name
}

output "ecs_cluster_arn" {
  description = "ECS cluster ARN"
  value       = aws_ecs_cluster.dociq.arn
}

output "rds_endpoint" {
  description = "RDS PostgreSQL endpoint"
  value       = aws_db_instance.analytics.endpoint
  sensitive   = true
}

output "msk_bootstrap_brokers" {
  description = "MSK Kafka bootstrap brokers (TLS)"
  value       = aws_msk_cluster.dociq.bootstrap_brokers_tls
  sensitive   = true
}
