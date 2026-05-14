# Git Branch Strategy

## Branch Model

```
main       → production (protected, no direct push)
develop    → staging    (protected, requires 1 approval)
feature/*  → new features (branch from develop)
hotfix/*   → emergency fixes (branch from main)
```

## Rules

- **Direct push to `main`: BLOCKED** (branch protection rule)
- PRs require **1 approval** before merge
- **All CI checks must pass** before merge is allowed
- Squash merge to `main` and `develop` (clean history)

## Branch Naming

```
feature/add-split-expenses
feature/fix-dashboard-charts
hotfix/fix-otp-validation
chore/update-dependencies
```

## Commit Convention

```
feat:     New feature
fix:      Bug fix
docs:     Documentation only
test:     Adding/updating tests
refactor: Code improvement without feature change
perf:     Performance improvement
chore:    Build/config/dependency changes
ci:       CI/CD pipeline changes
```

**Examples:**
```
feat(split): add WhatsApp invite viral loop
fix(auth): resolve JWT expiry edge case
test(fraud): add unit tests for ML rule engine
ci(github): add rollback workflow
```

## Workflow

```bash
# Start new feature
git checkout develop
git pull origin develop
git checkout -b feature/your-feature-name

# Work and commit
git add .
git commit -m "feat(scope): description"

# Push and create PR
git push origin feature/your-feature-name
# Open PR → develop on GitHub

# After merge to develop → staging auto-deploys
# After merge to main    → production auto-deploys
```

## Emergency Hotfix

```bash
# Branch from main
git checkout main
git pull origin main
git checkout -b hotfix/critical-fix

# Fix, commit, push
git commit -m "fix: critical payment bug"
git push origin hotfix/critical-fix

# Open PRs to BOTH main AND develop
```
