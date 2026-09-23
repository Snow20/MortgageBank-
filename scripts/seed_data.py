import requests

URL = "http://localhost:8080/api/mortgages"

mortgage = {

    "customerId": 1,

    "propertyValue": 220000,

    "downPayment": 44000,

    "termYears": 30,

    "interestRate": 3.10
}


response = requests.post(
    URL,
    json=mortgage,
    timeout=10
)


print("Status:", response.status_code)

print(
    response.json()
)