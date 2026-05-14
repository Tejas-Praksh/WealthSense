#!/bin/bash
set -e

REGION="ap-south-1"
CLUSTER="wealthsense-cluster"
APP_NAME="wealthsense"

echo "🚀 Setting up AWS infrastructure for WealthSense..."
echo "Region: $REGION"
echo ""

# Create ECR repositories for all services
services=(
  "api-gateway"
  "user-service"
  "transaction-service"
  "fraud-detection-service"
  "decision-engine-service"
  "notification-service"
  "ai-advisor-service"
  "investment-service"
)

echo "📦 Creating ECR repositories..."
for service in "${services[@]}"; do
  echo "  Creating: $APP_NAME-$service"
  aws ecr create-repository \
    --repository-name "$APP_NAME-$service" \
    --region "$REGION" \
    --image-scanning-configuration scanOnPush=true \
    --image-tag-mutability MUTABLE \
    2>/dev/null && echo "  ✅ Created $APP_NAME-$service" || echo "  ⏭️  Already exists: $APP_NAME-$service"
done

# Create ECS cluster
echo ""
echo "🎯 Creating ECS cluster: $CLUSTER"
aws ecs create-cluster \
  --cluster-name "$CLUSTER" \
  --region "$REGION" \
  --capacity-providers FARGATE FARGATE_SPOT \
  2>/dev/null && echo "✅ Cluster created" || echo "⏭️  Cluster already exists"

# Create S3 bucket for frontend
BUCKET_NAME="$APP_NAME-frontend-$(aws sts get-caller-identity --query Account --output text)"
echo ""
echo "🪣 Creating S3 bucket: $BUCKET_NAME"
aws s3 mb "s3://$BUCKET_NAME" --region "$REGION" 2>/dev/null || echo "⏭️  Bucket already exists"

# Configure bucket for static website hosting
aws s3 website "s3://$BUCKET_NAME" \
  --index-document index.html \
  --error-document index.html 2>/dev/null || true

echo ""
echo "====================================="
echo "✅ AWS setup complete!"
echo "====================================="
echo ""
echo "📋 Next steps:"
echo "1. Configure these GitHub Secrets:"
echo "   AWS_ACCESS_KEY_ID"
echo "   AWS_SECRET_ACCESS_KEY"
echo "   S3_BUCKET=$BUCKET_NAME"
echo "   CLOUDFRONT_ID (create distribution first)"
echo ""
echo "2. Create ECS task definitions for each service"
echo "3. Push to main branch to trigger first deployment"
echo ""
ECR_REGISTRY=$(aws ecr get-login-password --region $REGION | docker login --username AWS --password-stdin $(aws sts get-caller-identity --query Account --output text).dkr.ecr.$REGION.amazonaws.com 2>/dev/null && aws sts get-caller-identity --query Account --output text).dkr.ecr.$REGION.amazonaws.com
echo "ECR Registry: $ECR_REGISTRY"
