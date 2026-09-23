import requests

SERVICES = {
    "Backend": "http://localhost:8080/api/health",
    "Frontend": "http://localhost"
}


def check_service(name, url):

    try:

        response = requests.get(
            url,
            timeout=5
        )

        if response.ok:

            print(
                f"[OK] {name}: "
                f"{response.status_code}"
            )

        else:

            print(
                f"[ERROR] {name}: "
                f"{response.status_code}"
            )

    except requests.RequestException as error:

        print(
            f"[DOWN] {name}: {error}"
        )


def main():

    print("MortgageBank Health Check")
    print("=========================")

    for name, url in SERVICES.items():

        check_service(
            name,
            url
        )


if __name__ == "__main__":
    main()