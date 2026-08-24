"""API compatibility tests for the FastAPI and Pydantic dependency set."""

from fastapi.testclient import TestClient

from main import app


client = TestClient(app)


def test_health_endpoint_contract():
    response = client.get("/health")

    assert response.status_code == 200
    assert response.json() == {
        "status": "UP",
        "service": "python-pipeline",
        "version": "1.0.0",
    }


def test_process_endpoint_rejects_invalid_payload():
    response = client.post("/process", json={})

    assert response.status_code == 422
    detail = response.json()["detail"]
    missing_fields = {item["loc"][-1] for item in detail if item["type"] == "missing"}
    assert {"documentId", "s3Bucket", "s3Key", "contentType"} <= missing_fields
