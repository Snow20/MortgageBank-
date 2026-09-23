## 🇬🇧 ENG

### 1. Project Overview & Integrated Architecture

**MortgageBank** is an enterprise-grade application built for the asynchronous processing and risk evaluation of mortgage applications. The system follows a decoupled microservices architecture containerized using Docker Compose and exposed end-to-end through HTTP port `80`.

#### Key Architecture Components:

- **Frontend:** Angular 19 rendered as a Single Page Application (SPA), served by an Nginx web server on port `80`.
- **Reverse Proxy:** Nginx acting as the single entry point, serving Angular static assets and forwarding API requests (`/api/*`) to Spring Boot.
- **Backend REST API:** Spring Boot 3 / Java 21 handling data persistence and event publishing.
- **Persistence Layer:** PostgreSQL 16 relational database.
- **Message Broker:** RabbitMQ 4 for asynchronous risk evaluation using queues and a dedicated consumer.
- **Observability & CI/CD:** Monitored with Dynatrace OneAgent, declarative deployments via Ansible playbooks, and automated pipelines using GitHub Actions.

### 2. Network & Service Flow Diagram

```text
[ Web Browser ]
       │
       ▼ (HTTP :80)
┌────────────────────────────────────────────────────────┐
│ Nginx Container (mortgage-frontend)                    │
│  ├── Static Server   (/ -> Angular 19 SPA)             │
│  └── Reverse Proxy   (/api/* -> http://mortgage-backend:8080)
└────────────────────────┬───────────────────────────────┘
                         │ (Internal Network: mortgagebank_default)
                         ▼
┌────────────────────────────────────────────────────────┐
│ Spring Boot Container (mortgage-backend)               │
│  ├── Spring Security + JWT Authentication             │
│  ├── REST Controller & Service Layer                   │
│  └── Spring Data JPA                                   │
└──────────────┬─────────────────────────┬───────────────┘
               │                         │
               ▼ (JDBC :5432)            ▼ (AMQP :5672)
┌──────────────────────────┐   ┌──────────────────────────┐
│ PostgreSQL 16 Container  │   │ RabbitMQ 4 Container     │
│ (mortgage-postgres)      │   │ (mortgage-rabbitmq)      │
└──────────────────────────┘   └─────────┬────────────────┘
                                         │ (Async Consumption)
                                         ▼
                               ┌──────────────────────────┐
                               │ MortgageEventConsumer    │
                               │ (RiskAssessmentService)  │
                               └──────────────────────────┘
```

### 3. Asynchronous Event Processing with RabbitMQ

To evaluate financial credit risk (Loan-To-Value, LTV calculation):

1. The user submits a mortgage application through the web interface.
2. Spring Boot persists the application record with an initial status of **`PENDING`** into PostgreSQL.
3. The service publishes the `MortgageApplication` domain entity to `mortgage.exchange` using the `mortgage.application.submitted` routing key.
4. Messages are strictly serialized into JSON format via `Jackson2JsonMessageConverter` configured in `RabbitMQConfig`.
5. The `MortgageEventConsumer` receives the deserialized entity, triggers `RiskAssessmentService`, and updates the status to **`APPROVED`** (if `LTV <= 85%`) or **`REJECTED`** (if `LTV > 85%`).

### 4. Network Design & CORS Resolution

All interaction between the browser and backend services occurs under the same origin (*Same-Origin*) on port `80`.

#### Nginx Proxy Configuration (`nginx.conf`):

```nginx
server {
    listen 80;
    server_name localhost;

    location / {
        root /usr/share/nginx/html;
        index index.html;
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://mortgage-backend:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
}
```

**Advantage:** Using `proxy_pass` pointing to the internal Docker hostname `mortgage-backend` keeps the browser on the same origin as the Angular application and avoids relying on permissive CORS headers in the normal production path.

### 5. Deployment Guide & Execution

#### Prerequisites:

- Docker Engine 24+ and Docker Compose v2+.

#### Deployment Steps:

```bash
# 1. Clone the repository
git clone https://github.com/your-user/MortgageBank.git
cd MortgageBank

# 2. Build and run the entire stack
docker-compose up -d --build

# 3. Verify container status
docker-compose ps
```

#### Access Points:

- **Web Application:** `http://localhost`
- **RabbitMQ Management Dashboard:** `http://localhost:15672` (User: `mortgage` / Pass: `mortgagepass`)
- **Direct REST API Endpoint:** `http://localhost:8080/api/mortgages`

### 6. Kubernetes Deployment Reference

> **Note:** Production blueprint for migrating the architecture from Docker Compose to a Kubernetes cluster.

#### Deployment Manifest (`k8s/deployment.yaml`):

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: mortgage-backend
  labels:
    app: mortgage-backend
spec:
  replicas: 2
  selector:
    matchLabels:
      app: mortgage-backend
  template:
    metadata:
      labels:
        app: mortgage-backend
    spec:
      containers:
      - name: backend
        image: mortgagebank-backend:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_DATASOURCE_URL
          value: "jdbc:postgresql://postgres-service:5432/mortgagebank"
        - name: SPRING_RABBITMQ_HOST
          value: "rabbitmq-service"
---
apiVersion: v1
kind: Service
metadata:
  name: mortgage-backend-service
spec:
  type: ClusterIP
  ports:
  - port: 8080
    targetPort: 8080
  selector:
    app: mortgage-backend
```

### 7. Infrastructure as Code with Terraform (Reference)

> **Note:** HCL templates for provisioning cloud network resources and infrastructure dependencies.

#### Terraform Configuration (`terraform/main.tf`):

```hcl
terraform {
  required_version = ">= 1.5.0"

  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "~> 3.0"
    }
  }
}

provider "azurerm" {
  features {}
}

resource "azurerm_resource_group" "rg" {
  name     = "rg-mortgagebank-prod"
  location = "West Europe"
}

resource "azurerm_virtual_network" "vnet" {
  name                = "vnet-mortgagebank"
  address_space       = ["10.0.0.0/16"]
  location            = azurerm_resource_group.rg.location
  resource_group_name = azurerm_resource_group.rg.name
}
```

### 8. Provisioning Azure AKS (Reference)

> **Note:** Configuration blueprint for deploying a managed Azure Kubernetes Service (AKS) cluster integrated with Azure Container Registry (ACR).

#### AKS Cluster (`terraform/aks.tf`):

```hcl
resource "azurerm_kubernetes_cluster" "aks" {
  name                = "aks-mortgagebank-prod"
  location            = azurerm_resource_group.rg.location
  resource_group_name = azurerm_resource_group.rg.name
  dns_prefix          = "mortgagebank-k8s"

  default_node_pool {
    name       = "default"
    node_count = 2
    vm_size    = "Standard_DS2_v2"
  }

  identity {
    type = "SystemAssigned"
  }
}
```

#### AKS Deployment Commands:

```bash
# 1. Login to Azure CLI
az login

# 2. Get AKS cluster credentials
az aks get-credentials --resource-group rg-mortgagebank-prod --name aks-mortgagebank-prod

# 3. Apply Kubernetes manifests
kubectl apply -f k8s/
```

### 9. Technical Recommendations & Key Takeaways

- **Message Serialization:** Always override Spring AMQP's default message converter with `Jackson2JsonMessageConverter` to avoid `MessageConversionException` when passing domain objects between producers and consumers.
- **Service Method Compatibility:** When refactoring REST service logic, maintain alias methods (such as `saveApplication` and `createApplication`) to prevent compilation errors in dependent controllers.
- **TypeScript Export Modifiers:** In Angular/TypeScript, do not use the `public` access modifier on top-level interface declarations (`export interface MortgageApplication`), avoiding `TS1044` syntax errors.


# MortgageBank - Sistema de Gestión Hipotecaria Basado en Microservicios

---

## 🇪🇸 ESP

### 1. Descripción del Proyecto y Arquitectura Integrada

**MortgageBank** es una aplicación empresarial orientada al procesamiento y evaluación asíncrona de solicitudes hipotecarias. El sistema está desacoplado bajo una arquitectura de microservicios contenerizada mediante Docker Compose y expuesta de extremo a extremo a través del puerto HTTP `80`.

#### Componentes de la Arquitectura:

- **Frontend:** Angular 19 renderizado como Single Page Application (SPA) y servido por un servidor Nginx en puerto `80`.
- **Reverse Proxy:** Nginx actuando como punto de entrada único, sirviendo los estáticos de Angular y redirigiendo el tráfico dinámico (`/api/*`) hacia Spring Boot.
- **Backend REST:** Spring Boot 3 / Java 21 gestionando la persistencia de datos y la publicación de eventos.
- **Capa de Persistencia:** Base de datos relacional PostgreSQL 16.
- **Broker de Mensajería:** RabbitMQ 4 para la evaluación asíncrona de riesgos mediante colas y un consumidor dedicado.
- **Observabilidad y CI/CD:** Monitoreo mediante Dynatrace OneAgent, despliegues declarativos con Ansible y flujos de integración continua en GitHub Actions.

---

### 2. Diagrama de Flujo y Red

```text
[ Navegador Web ]
       │
       ▼ (HTTP :80)
┌────────────────────────────────────────────────────────┐
│ Nginx Container (mortgage-frontend)                    │
│  ├── Servidor Estático (/ -> Angular 19 SPA)           │
│  └── Reverse Proxy     (/api/* -> http://mortgage-backend:8080)
└────────────────────────┬───────────────────────────────┘
                         │ (Red Interna: mortgagebank_default)
                         ▼
┌────────────────────────────────────────────────────────┐
│ Spring Boot Container (mortgage-backend)               │
│  ├── Spring Security + JWT Authentication             │
│  ├── REST Controller & Service Layer                   │
│  └── Spring Data JPA                                   │
└──────────────┬─────────────────────────┬───────────────┘
               │                         │
               ▼ (JDBC :5432)            ▼ (AMQP :5672)
┌──────────────────────────┐   ┌──────────────────────────┐
│ PostgreSQL 16 Container  │   │ RabbitMQ 4 Container     │
│ (mortgage-postgres)      │   │ (mortgage-rabbitmq)      │
└──────────────────────────┘   └─────────┬────────────────┘
                                         │ (Consumo Asíncrono)
                                         ▼
                               ┌──────────────────────────┐
                               │ MortgageEventConsumer    │
                               │ (RiskAssessmentService)  │
                               └──────────────────────────┘
```

---

### 3. Procesamiento Asíncrono de Eventos con RabbitMQ

Para la evaluación del riesgo crediticio mediante el cálculo de Loan-To-Value (LTV):

1. El usuario envía la solicitud desde la interfaz gráfica.
2. Spring Boot guarda el expediente con estado inicial **`PENDING`** en PostgreSQL.
3. El servicio publica la entidad `MortgageApplication` en el *Exchange* `mortgage.exchange` con la *Routing Key* `mortgage.application.submitted`.
4. Los mensajes son serializados estrictamente en formato JSON utilizando `Jackson2JsonMessageConverter` configurado en `RabbitMQConfig`.
5. El componente `MortgageEventConsumer` recibe la entidad deserializada, invoca a `RiskAssessmentService` y actualiza el estado del registro a **`APPROVED`** (si `LTV <= 85%`) o **`REJECTED`** (si `LTV > 85%`).

---

### 4. Arquitectura de Red y Solución CORS

Toda la interacción entre el navegador y los servicios backend se realiza bajo el mismo origen (*Same-Origin*) en el puerto `80`.

#### Configuración de Nginx (`nginx.conf`):

```nginx
server {
    listen 80;
    server_name localhost;

    location / {
        root /usr/share/nginx/html;
        index index.html;
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://mortgage-backend:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
}
```

**Ventaja:** Al utilizar `proxy_pass` hacia el nombre de host de Docker `mortgage-backend`, el navegador accede al backend a través del mismo origen que sirve Angular. Esto evita depender de configuraciones CORS permisivas en la ruta normal de producción.

---

### 5. Guía de Despliegue y Ejecución

#### Requisitos Previos:

- Docker Engine 24+ y Docker Compose v2+.

#### Pasos para Desplegar la Aplicación:

```bash
# 1. Clonar el repositorio
git clone https://github.com/tu-usuario/MortgageBank.git
cd MortgageBank

# 2. Construir y levantar la pila completa
docker-compose up -d --build

# 3. Verificar el estado de los contenedores
docker-compose ps
```

#### Acceso a las Interfaces:

- **Aplicación Web:** `http://localhost`
- **Consola de Administración RabbitMQ:** `http://localhost:15672` (Usuario: `mortgage` / Clave: `mortgagepass`)
- **Endpoint REST Directo:** `http://localhost:8080/api/mortgages`

---

### 6. Despliegue en Kubernetes (Referencial)

> **Nota:** Esta sección sirve como plantilla de producción para migrar la arquitectura desde Docker Compose hacia un clúster de Kubernetes (k8s).

#### Manifiesto de Despliegue (`k8s/deployment.yaml`):

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: mortgage-backend
  labels:
    app: mortgage-backend
spec:
  replicas: 2
  selector:
    matchLabels:
      app: mortgage-backend
  template:
    metadata:
      labels:
        app: mortgage-backend
    spec:
      containers:
      - name: backend
        image: mortgagebank-backend:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_DATASOURCE_URL
          value: "jdbc:postgresql://postgres-service:5432/mortgagebank"
        - name: SPRING_RABBITMQ_HOST
          value: "rabbitmq-service"
---
apiVersion: v1
kind: Service
metadata:
  name: mortgage-backend-service
spec:
  type: ClusterIP
  ports:
  - port: 8080
    targetPort: 8080
  selector:
    app: mortgage-backend
```

---

### 7. Infraestructura como Código con Terraform (Referencial)

> **Nota:** Código preparado como referencia para aprovisionar los recursos de red y las dependencias en la nube mediante HashiCorp Configuration Language (HCL).

#### Configuración de Terraform (`terraform/main.tf`):

```hcl
terraform {
  required_version = ">= 1.5.0"

  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "~> 3.0"
    }
  }
}

provider "azurerm" {
  features {}
}

resource "azurerm_resource_group" "rg" {
  name     = "rg-mortgagebank-prod"
  location = "West Europe"
}

resource "azurerm_virtual_network" "vnet" {
  name                = "vnet-mortgagebank"
  address_space       = ["10.0.0.0/16"]
  location            = azurerm_resource_group.rg.location
  resource_group_name = azurerm_resource_group.rg.name
}
```

---

### 8. Aprovisionamiento en Azure AKS (Referencial)

> **Nota:** Esquema de configuración para desplegar un clúster gestionado de Azure Kubernetes Service (AKS) integrado con Azure Container Registry (ACR).

#### Clúster de AKS (`terraform/aks.tf`):

```hcl
resource "azurerm_kubernetes_cluster" "aks" {
  name                = "aks-mortgagebank-prod"
  location            = azurerm_resource_group.rg.location
  resource_group_name = azurerm_resource_group.rg.name
  dns_prefix          = "mortgagebank-k8s"

  default_node_pool {
    name       = "default"
    node_count = 2
    vm_size    = "Standard_DS2_v2"
  }

  identity {
    type = "SystemAssigned"
  }
}
```

#### Comandos de Conexión y Despliegue en AKS:

```bash
# 1. Autenticarse en Azure CLI
az login

# 2. Descargar las credenciales del clúster de AKS
az aks get-credentials --resource-group rg-mortgagebank-prod --name aks-mortgagebank-prod

# 3. Aplicar los manifiestos de Kubernetes
kubectl apply -f k8s/
```

---

### 9. Recomendaciones Técnicas y Lecciones Aprendidas

- **Serialización de Mensajes:** Es indispensable sobrescribir el convertidor por defecto de Spring AMQP con `Jackson2JsonMessageConverter` para evitar errores `MessageConversionException` al pasar objetos de dominio entre productor y consumidor.
- **Compatibilidad de Métodos en Capa de Servicios:** Al refactorizar la lógica del servicio REST, mantener firmas alias (como `saveApplication` y `createApplication`) evita fallos de enlace en los controladores.
- **Uso de Modificadores en TypeScript:** En Angular/TypeScript no debe utilizarse el modificador de acceso `public` en la declaración raíz de interfaces (`export interface MortgageApplication`), evitando errores del compilador `TS1044`.

