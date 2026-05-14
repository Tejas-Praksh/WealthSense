# MongoDB Atlas (free M0)

## Steps

1. Go to [mongodb.com/atlas](https://www.mongodb.com/atlas).
2. **Create** a free **M0** cluster (512 MB).
3. Region: **AWS ap-south-1** (Mumbai) or closest to Render region.
4. **Database access** → create user `wealthsense` (strong password).
5. **Network access** → for a quick demo you may use `0.0.0.0/0` (tighten to Render outbound IPs for real production).
6. **Connect** → Drivers → copy **SRV** connection string.

Example:

```text
mongodb+srv://wealthsense:<PASSWORD>@cluster0.xxxxx.mongodb.net/wealthsense?retryWrites=true&w=majority
```

Set `MONGO_URI` (or per-service URI used in `application.yml`) on Render for:

- `decision-engine-service`
- `ai-advisor-service`

## Collections

JPA is not used for Mongo here; Spring Data creates collections on first write. Typical names align with your packages:

- `decisions` (or as created by decision engine)
- `conversations` / chat documents (AI advisor)
- Any embedding or insight collections your services persist

Use **Atlas Search** / indexes later if you add RAG at scale.

## Cost

M0 is **free forever** within Atlas limits; monitor storage and connections.
