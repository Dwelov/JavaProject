
import requests
import uuid

email = f"user_{uuid.uuid4().hex[:8]}@example.com"
url = "http://127.0.0.1:8080/api/auth/signup"
payload = {
    "fullName": "Test User",
    "email": email,
    "password": "password123"
}

print(f"Trying to signup with: {email}")
response = requests.post(url, json=payload)
print(f"Status Code: {response.status_code}")
print(f"Response: {response.json()}")
