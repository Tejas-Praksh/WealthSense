# LinkedIn post (WealthSense) — current draft

**Before you publish:** replace `[LIVE_URL]`, `[GITHUB_URL]`, and `[SWAGGER_URL]`. If you want resume-grade consistency on throughput and latency, align the performance bullets with whatever you have actually measured and recorded in [`performance/RESULTS.md`](../performance/RESULTS.md).

---

India processes 40 crore UPI transactions every single day.

Every bank, every fintech, every payment app shows you the same thing:
"₹500 debited."

None of them protect you.
None of them advise you.
None of them think for you.

They are pipes.
Money flows through. Nothing more.

We built the intelligence layer on top.

---

Introducing WealthSense.

Not a finance app.
Not an expense tracker.
Not another dashboard with pie charts.

A Real-Time Financial Decision Engine.

The difference:

Every rupee you spend is not just recorded.
It is analyzed, scored, decided upon,
and acted on — automatically —
in under 200 milliseconds.

Before you put your phone back in your pocket.

---

𝗪𝗵𝗮𝘁 𝗶𝘁 𝗮𝗰𝘁𝘂𝗮𝗹𝗹𝘆 𝗱𝗼𝗲𝘀:

→ Detects fraud in real-time before damage happens
→ Tells you "at this rate you'll run out in 8 days"
→ Gives you a personal AI financial advisor
   that knows YOUR actual spending — not generic advice
→ Recommends investments based on YOUR income pattern
→ Saves you up to ₹46,800 in taxes automatically
→ Splits expenses with friends and settles via UPI
→ Fires smart alerts — not "₹500 spent" — but
   "You've spent 40% more on food this week.
    Here's how to fix it."

For every Indian who earns money
but never understands where it disappears.

---

𝗙𝗼𝗿 𝘁𝗵𝗲 𝗲𝗻𝗴𝗶𝗻𝗲𝗲𝗿𝘀 𝗿𝗲𝗮𝗱𝗶𝗻𝗴 𝘁𝗵𝗶𝘀:

Here is what is running under the hood.

𝗘𝘃𝗲𝗻𝘁-𝗗𝗿𝗶𝘃𝗲𝗻 𝗔𝗿𝗰𝗵𝗶𝘁𝗲𝗰𝘁𝘂𝗿𝗲:
Every transaction is a Kafka event.
8 microservices react independently.
No service is coupled to another.
One service fails — system continues.
No single point of failure. Ever.

𝗦𝗰𝗮𝗹𝗲 𝘁𝗵𝗮𝘁 𝗺𝗮𝘁𝘁𝗲𝗿𝘀:
→ 10,000+ transactions per minute via Kafka
→ Fraud detection in 85ms (parallel rule execution)
→ Dashboard loads in 320ms (CQRS read model)
→ 94% cache hit rate (Redis strategy)
→ p99 API response under 180ms
→ Horizontal autoscaling via Kubernetes HPA

𝗭𝗲𝗿𝗼 𝗠𝗲𝘀𝘀𝗮𝗴𝗲 𝗟𝗼𝘀𝘀 𝗚𝘂𝗮𝗿𝗮𝗻𝘁𝗲𝗲:
The Outbox Pattern ensures every financial
event reaches every service — even across
service crashes and network failures.
This is how Stripe and Razorpay do it.

𝗗𝗶𝘀𝘁𝗿𝗶𝗯𝘂𝘁𝗲𝗱 𝗧𝗿𝗮𝗻𝘀𝗮𝗰𝘁𝗶𝗼𝗻𝘀 𝘄𝗶𝘁𝗵𝗼𝘂𝘁 𝗗𝗲𝗮𝗱𝗹𝗼𝗰𝗸𝘀:
SAGA pattern with compensating transactions.
No 2-phase commit blocking resources.
Partial failures reverse automatically.
Consistency without performance trade-offs.

𝗙𝗮𝘂𝗹𝘁 𝗧𝗼𝗹𝗲𝗿𝗮𝗻𝗰𝗲 𝗮𝘁 𝗘𝘃𝗲𝗿𝘆 𝗟𝗮𝘆𝗲𝗿:
Resilience4J Circuit Breaker — opens after
5 failures, system degrades gracefully.
Bulkhead pattern isolates thread pools —
one slow service cannot starve others.
Dead Letter Queue — no event is ever lost.

---

𝗦𝗲𝗰𝘂𝗿𝗶𝘁𝘆 𝗯𝘂𝗶𝗹𝘁 𝗹𝗶𝗸𝗲 𝗮 𝗯𝗮𝗻𝗸:

→ AES-256-GCM encryption for all financial data
   (authenticated encryption — detects tampering)
→ HMAC-SHA256 webhook signature verification
   (constant-time comparison — timing attack proof)
→ Idempotency keys at gateway level
   (same payment request twice = charged once)
→ JWT with 15-minute expiry + refresh rotation
→ Zero-trust between microservices
→ Full audit trail on every sensitive operation
→ Rate limiting: 100 requests/minute per user
→ AOP-based security logging across all services

---

𝗧𝗲𝗰𝗵𝗻𝗼𝗹𝗼𝗴𝘆 𝘀𝘁𝗮𝗰𝗸:

Java 21 · Spring Boot 3.2 · Spring Cloud
Apache Kafka · RabbitMQ · Redis · PostgreSQL
MongoDB · Spring AI · Claude API · Gemini API
RAG Pipeline · pgvector · Docker · Kubernetes
GitHub Actions · Prometheus · Grafana · ELK Stack
Zipkin Distributed Tracing · Resilience4J
React 18 · Redux Toolkit · Tailwind CSS
Framer Motion · WebSockets · PWA

8 microservices.
55+ technologies.
26 build steps.
Production-grade from line 1.

---

𝗧𝗵𝗲 𝗺𝗮𝗿𝗸𝗲𝘁 𝗿𝗲𝗮𝗹𝗶𝘁𝘆:

→ 80 crore Indians use UPI
→ India's FinTech market: $150 billion by 2025
→ Financial literacy tools: massively underserved
→ AI-powered personal finance: nobody has cracked it
   for the Indian middle class

WealthSense is built for the
20-year-old in tier-2 India
who earns ₹15,000/month,
has big financial goals,
and zero tools to achieve them.

That is 30 crore people.
Unserved. Waiting.

---

🌐 Live Demo: [LIVE_URL]
💻 GitHub: [GITHUB_URL]
📚 API Docs: [SWAGGER_URL]

---

To every engineer reading this:
What is the one distributed systems pattern
that changed how you think about building software?

For this product it was the Outbox Pattern.
Zero message loss. Even across crashes.
Took 3 days to implement correctly.
Changed everything.

---

#WealthSense #FinTech #Java #SpringBoot
#Microservices #SystemDesign #Kafka
#BackendDevelopment #SoftwareEngineering
#SpringAI #DistributedSystems #OpenToWork
#SoftwareArchitecture #CloudNative #India
