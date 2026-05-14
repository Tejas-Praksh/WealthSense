# Free-tier deployment (Step 25)

Zero-cost stack: **Render** (APIs), **Vercel** (frontend), **Supabase** (Postgres + optional storage), **MongoDB Atlas**, **Upstash** (Redis + Kafka), **GitHub Actions**, **Grafana Cloud** (optional), **UptimeRobot** (keep Render awake for demos).

| Guide | Purpose |
|-------|---------|
| [supabase-setup.md](supabase-setup.md) | Postgres project + JDBC |
| [supabase-schema.sql](supabase-schema.sql) | Initial tables (run in SQL editor) |
| [upstash-setup.md](upstash-setup.md) | Redis + Kafka |
| [mongodb-setup.md](mongodb-setup.md) | Atlas cluster |
| [render-setup.md](render-setup.md) | Docker web services |
| [vercel-setup.md](vercel-setup.md) | Frontend build |
| [uptimerobot-setup.md](uptimerobot-setup.md) | Health pings |
| [all-env-vars.md](all-env-vars.md) | Secret / env matrix |
| [grafana-cloud-setup.md](grafana-cloud-setup.md) | Metrics stack |
| [checklist.md](checklist.md) | Post-deploy verification |

**Live URLs** are issued by Render and Vercel after you connect accounts — copy them into the root `README.md` and frontend `VITE_*` variables.
