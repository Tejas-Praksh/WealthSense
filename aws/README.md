# AWS Deployment Guide — WealthSense

## Free Tier Services Used

| Service | Usage | Free Tier |
|---------|-------|-----------|
| EC2 t2.micro | ECS host | 750 hrs/month |
| RDS db.t3.micro | PostgreSQL | 750 hrs/month |
| ElastiCache | Redis | 750 hrs/month |
| S3 | Frontend hosting | 5 GB |
| CloudFront | CDN | 1 TB transfer |
| ECR | Docker images | 500 MB |
| ECS | Orchestration | Free (pay EC2) |

## Initial Setup

```bash
# 1. Install and configure AWS CLI
aws configure
# Enter: Access Key, Secret Key, region: ap-south-1

# 2. Run setup script
bash aws/setup.sh

# 3. Configure GitHub Secrets (see below)

# 4. Push to develop → staging auto-deploys
# 5. Push to main    → production auto-deploys
```

## GitHub Secrets Required

Set these at: GitHub → Repo → Settings → Secrets → Actions

### AWS
| Secret | Description |
|--------|-------------|
| `AWS_ACCESS_KEY_ID` | IAM user access key |
| `AWS_SECRET_ACCESS_KEY` | IAM user secret key |
| `S3_BUCKET` | Frontend S3 bucket name |
| `CLOUDFRONT_ID` | CloudFront distribution ID |
| `PRODUCTION_API_URL` | https://api.yourdomain.com |
| `PRODUCTION_WS_URL` | wss://api.yourdomain.com |

### Docker Hub (for staging)
| Secret | Description |
|--------|-------------|
| `DOCKER_USERNAME` | Docker Hub username |
| `DOCKER_PASSWORD` | Docker Hub access token |

### Staging Server
| Secret | Description |
|--------|-------------|
| `STAGING_HOST` | IP or hostname |
| `STAGING_USER` | SSH username |
| `STAGING_SSH_KEY` | Private SSH key |

### Application
| Secret | Description |
|--------|-------------|
| `JWT_SECRET` | Min 256-bit secret |
| `CLAUDE_API_KEY` | Anthropic API key |
| `POSTGRES_PASSWORD` | DB password |
| `REDIS_PASSWORD` | Redis password |
| `ENCRYPTION_KEY` | AES encryption key |
| `GRAFANA_PASSWORD` | Grafana admin password |
| `SLACK_WEBHOOK` | Slack webhook URL |

## Deployment Flow

```
feature/* → PR → develop → staging (auto)
develop   → PR → main    → production (auto)
manual: rollback.yml → rollback to any SHA
```

## Cost Estimates

| Environment | Services | Cost |
|-------------|----------|------|
| Development | Free tier all | $0/month |
| Staging | 1 x t2.micro | ~$5/month |
| Production light | 2 x t2.micro | ~$20/month |
| Production medium | 4 x t3.small | ~$50/month |

## Rollback (One Click)

1. GitHub → Actions → "Rollback Production"
2. Click "Run workflow"
3. Enter the commit SHA to rollback to
4. Optionally specify services (or "all")
5. Click "Run workflow"

Done in ~3 minutes.
