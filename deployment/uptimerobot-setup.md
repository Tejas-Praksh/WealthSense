# UptimeRobot (free HTTP monitors)

Use UptimeRobot to **wake** sleeping Render free services and get basic uptime stats.

## Steps

1. [uptimerobot.com](https://uptimerobot.com) — free account.
2. **Add new monitor** → type **HTTP(s)**.
3. **URL:** `https://<your-service>.onrender.com/actuator/health`
4. **Interval:** **14 minutes** (below Render’s typical idle window; do not hammer sub-minute intervals).
5. Repeat for:
   - API Gateway
   - Each microservice you expose publicly (or only gateway if others are private later).

## Notes

- Monitors reduce cold starts for **demo** traffic; they are **not** a production SLA solution.
- Prefer **paid Render** or **always-on** for real production.
- If health returns **503** during startup, set monitor **threshold** / retries in UptimeRobot to avoid false alerts.
