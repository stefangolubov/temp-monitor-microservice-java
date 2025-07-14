# Temp Monitor Microservice

Microservice for monitoring temperature readings, built with FastAPI and PostgreSQL.  
Deployable on Kubernetes using Helm for PostgreSQL and Docker for the app.

## Prerequisites

- [Docker](https://docs.docker.com/get-docker/)
- [kubectl](https://kubernetes.io/docs/tasks/tools/)
- [Helm](https://helm.sh/)
- Kubernetes cluster (local or remote)
- [Git](https://git-scm.com/)

## 1. Deploy PostgreSQL on Kubernetes

```sh
helm repo add bitnami https://charts.bitnami.com/bitnami
helm install my-postgres bitnami/postgresql --set auth.postgresPassword=secret
```

Wait for the pod to be running:
```sh
kubectl get pods
```

## 2. Retrieve PostgreSQL Password

**Linux/Mac:**
```sh
kubectl get secret --namespace default my-postgres-postgresql -o jsonpath="{.data.postgres-password}" | base64 -d
```

**Windows PowerShell:**
```powershell
$pw = kubectl get secret --namespace default my-postgres-postgresql -o jsonpath="{.data.postgres-password}"
[System.Text.Encoding]::UTF8.GetString([Convert]::FromBase64String($pw))
```

## 3. Create the Application Database

Connect to the Postgres pod:
```sh
kubectl exec -it my-postgres-postgresql-0 -- psql -U postgres -d postgres
```

Inside `psql`:
```sql
CREATE DATABASE temp_monitor_db;
\l
```

## 4. Build and Push the Docker Image

```sh
docker build -t <your-dockerhub-username>/temp-monitor-microservice:latest .
docker login
docker push <your-dockerhub-username>/temp-monitor-microservice:latest
```

## 5. Configure and Deploy the App

Update `deployment.yaml` with your image name and database connection string:

```yaml
env:
  - name: DATABASE_URL
    value: postgresql://postgres:<your-db-password>@my-postgres-postgresql.default.svc.cluster.local:5432/temp_monitor_db
```

Apply the deployment and NodePort service:

```sh
kubectl apply -f deployment.yaml
kubectl apply -f service.yaml
```

## 6. Access the Application

Find your node’s IP (for Minikube, use `minikube ip`):

Open in browser:
```
http://<node-ip>:30080/docs
```

## 7. Troubleshooting

Check logs:
```sh
kubectl logs deploy/temp-monitor-microservice
```

---

## 8. Sample Endpoints

- `/init-demo-data` – initialize demo data
- `/locations` – view locations
- `/thermometers` – view thermometers
- `/readings` – view readings

---

## License

MIT