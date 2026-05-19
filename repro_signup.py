
import requests

url = "http://localhost:8080/api/auth/signup"
payload = {
    "fullName": "Test User",
    "email": "test2@example.com",
    "password": "short"
}

response = requests.post(url, json=payload)
print(f"Status Code: {response.status_code}")
print(f"Response: {response.json()}")
