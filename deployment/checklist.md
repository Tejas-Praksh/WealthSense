# Post-deployment checklist

## Backend health (HTTP 200 on `/actuator/health`)

- [ ] api-gateway  
- [ ] user-service  
- [ ] transaction-service  
- [ ] fraud-detection-service  
- [ ] decision-engine-service  
- [ ] notification-service  
- [ ] ai-advisor-service  
- [ ] investment-service  

## Frontend (Vercel)

- [ ] App loads  
- [ ] Register  
- [ ] Login  
- [ ] Dashboard (if present)  
- [ ] Transactions flow  
- [ ] AI advisor (if API keys set)  
- [ ] Investments / goals (if enabled)  

## Integrations

- [ ] JWT auth end-to-end via gateway  
- [ ] Kafka consumers processing (check logs / Upstash metrics)  
- [ ] Redis (idempotency / rate limit)  
- [ ] MongoDB connected (decision + AI services)  
- [ ] Email / notifications (SMTP or provider configured)  

## Documentation and repo hygiene

- [ ] Replace placeholder URLs in root `README.md` with live Vercel + Render URLs  
- [ ] Update benchmark table with measured numbers (optional)  
- [ ] Add screenshots to `docs/` or README  
- [ ] Confirm **no secrets** in git history for this deploy  

## Share

- [ ] Public GitHub repo (if intended)  
- [ ] LinkedIn / portfolio post (`docs/LINKEDIN_POST.md`)  
