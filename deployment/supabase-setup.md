# Supabase setup (PostgreSQL, free tier)

Use Supabase as **managed PostgreSQL** for demos. For strict microservice isolation in production, use **separate Supabase projects** or **schemas per service**; below assumes **one project** with all tables in `public` (simplest free demo).

## Steps

1. Go to [supabase.com](https://supabase.com) and sign in.
2. **New project** → name e.g. `wealthsense`, pick a region close to users (e.g. Mumbai if available).
3. Save credentials (Settings → API / Database):
   - **Project URL**
   - **Anon key** / **service role key** (never expose service role in the browser)
   - **Database password** (set at project creation)
   - **Connection string** (Settings → Database → URI)

## JDBC URL for Spring Boot

Format:

```text
jdbc:postgresql://db.<PROJECT_REF>.supabase.co:5432/postgres?sslmode=require
```

Use env vars (Render / local):

- `POSTGRES_URL` — full JDBC URL above  
- `POSTGRES_USER` — `postgres`  
- `POSTGRES_PASSWORD` — your DB password  

## Apply schema

1. Open **SQL Editor** in Supabase.
2. Paste and run `deployment/supabase-schema.sql`.

Or run from CI after review (do not run destructive SQL on production without backup).

## Storage (optional)

Supabase **Storage** can hold exports or static assets; app code today is API-first. Enable buckets only if you add upload features.

## Notes

- Free tier has connection and compute limits — use **connection pooling** (Supabase pooler / PgBouncer) for many Render instances.
- Rotate keys from the Supabase dashboard; never commit secrets to git.
