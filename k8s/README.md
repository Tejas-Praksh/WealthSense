# Kubernetes Deployment Guide

## Prerequisites

- `kubectl` configured against your cluster (for example AWS EKS or minikube).
- Container images built and available as referenced in `k8s/services/*.yml` (for example `wealthsense/api-gateway:latest`).
- TLS secret `wealthsense-tls` in namespace `wealthsense` for `api.wealthsense.app` before applying ingress (create with cert-manager or `kubectl create secret tls`).
- **RabbitMQ**: `configmap.yml` sets `RABBITMQ_HOST: rabbitmq`. Deploy a RabbitMQ service named `rabbitmq` in this namespace, or adjust the ConfigMap to your broker address.
- **Zipkin**: tracing endpoints assume Zipkin is reachable at host `zipkin` if you enable full tracing in-cluster.

## Deploy everything

```bash
kubectl apply -f k8s/namespace.yml
kubectl apply -f k8s/configmap.yml
kubectl apply -f k8s/secrets.yml
kubectl apply -f k8s/infrastructure/
kubectl apply -f k8s/services/
kubectl apply -f k8s/ingress/
kubectl apply -f k8s/hpa/
```

Replace placeholder values in `k8s/secrets.yml` (or create the secret from a local env file) before workloads that depend on `REDIS_PASSWORD` or `POSTGRES_PASSWORD` can pass readiness checks.

## Verify deployment

```bash
kubectl get pods -n wealthsense
kubectl get services -n wealthsense
kubectl get hpa -n wealthsense
```

## Scale manually

```bash
kubectl scale deployment api-gateway --replicas=5 -n wealthsense
```

## View logs

```bash
kubectl logs -f deployment/api-gateway -n wealthsense
```

## Rollback

```bash
kubectl rollout undo deployment/api-gateway -n wealthsense
```

## Monitor

```bash
kubectl top pods -n wealthsense
kubectl describe hpa -n wealthsense
```

## Validate manifests (client dry-run)

From the repository root, with a **reachable Kubernetes API** in your current context (minikube, EKS, Docker Desktop K8s, and so on):

```bash
kubectl apply --dry-run=client --validate=false -f k8s/namespace.yml
kubectl apply --dry-run=client --validate=false -f k8s/configmap.yml
kubectl apply --dry-run=client --validate=false -f k8s/secrets.yml
kubectl apply --dry-run=client --validate=false -f k8s/infrastructure/
kubectl apply --dry-run=client --validate=false -f k8s/services/
kubectl apply --dry-run=client --validate=false -f k8s/ingress/
kubectl apply --dry-run=client --validate=false -f k8s/hpa/
```

`--validate=false` avoids OpenAPI schema download from the server. Some `kubectl` versions still contact the cluster for resource discovery; if your kubeconfig points at a stopped endpoint, start the cluster or switch context before running these commands.

## Frontend

`k8s/ingress/ingress.yml` routes `app.wealthsense.app` to Service `frontend`. `k8s/services/frontend-deployment.yml` is a minimal placeholder using `wealthsense/frontend:latest`; build and push that image or point the deployment to your real SPA image.
