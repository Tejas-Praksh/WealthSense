# Vercel (free frontend)

## Steps

1. [vercel.com](https://vercel.com) → **Add New** → **Project** → **Import** your GitHub repo.
2. **Root directory:** `wealthsense-frontend`
3. **Framework preset:** Vite (auto-detected from `vite.config`).
4. **Build command:** `npm run build`
5. **Output directory:** `dist`

## Environment variables (Production)

Set in Vercel → Project → Settings → Environment Variables:

| Name | Example value |
|------|----------------|
| `VITE_API_URL` | `https://wealthsense-api-gateway.onrender.com` |
| `VITE_WS_URL` | `wss://wealthsense-api-gateway.onrender.com` (if WebSocket/STOMP is enabled behind same host) |
| `VITE_APP_NAME` | `WealthSense` |
| `VITE_APP_ENV` | `production` |

Redeploy after changing env vars.

## GitHub Actions → Vercel

Use repository secrets:

- `VERCEL_TOKEN`
- `VERCEL_ORG_ID`
- `VERCEL_PROJECT_ID`

See `.github/workflows/deploy-free.yml`.

## Domain

- Default: `*.vercel.app` subdomain (free).
- Optional: add a custom domain in Vercel and point DNS.

## Previews

Vercel creates **preview deployments** per PR by default when integrated with GitHub.
