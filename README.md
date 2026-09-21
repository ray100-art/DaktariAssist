# DaktariAssist — AI clinical second opinion

DaktariAssist is a decision-support tool for doctors and clinical officers in Kenya. A
clinician enters the patient's presentation, vitals and proposed diagnosis. DaktariAssist
reviews the whole picture and replies the way a supportive colleague would: *"the clinical
picture may also be consistent with…"*

It does not replace the clinician. It gives a structured, respectful second opinion that
takes the local disease burden into account: malaria, typhoid, TB, HIV, brucellosis,
meningitis and other conditions common in sub-Saharan Africa.

[![Build](https://github.com/ray100-art/DaktariAssist/actions/workflows/build.yml/badge.svg)](https://github.com/ray100-art/DaktariAssist/actions/workflows/build.yml) ![Java](https://img.shields.io/badge/Java-21-orange) ![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3-6DB33F) ![LLM](https://img.shields.io/badge/LLM-Llama_3.3_70B_via_Groq-555)

## What it returns

For each case, the API returns structured JSON that the UI renders as a report:

- **Consistency rating:** green, amber or red, showing how well the proposed diagnosis fits
  the evidence, with a short explanation.
- **Red flags** in the vitals or symptoms that need attention.
- **Two or three differential diagnoses**, each with a reason to consider it.
- **Three to five recommended tests** to confirm or rule out conditions.
- **One follow-up question** worth asking the patient.

## How it works

```
Browser form ──POST /api/analyse──▶ Spring Boot API ──▶ Groq (Llama 3.3 70B)
      ▲                                  │   system prompt: role, tone, Kenyan context,
      └────────── structured report ◀────┘   strict JSON schema, temperature 0.2
```

- The **system prompt** sets the assistant's tone (collegial, never "the diagnosis is
  wrong"), its Kenyan clinical context, and a strict JSON output schema.
- A **low temperature (0.2)** keeps answers consistent from one case to the next.
- The service **strips any Markdown fences** from the model's reply and parses the JSON into
  typed Java objects before returning it. Errors come back as a structured `error` field.

## Inputs

Age, sex, chief complaint, symptoms, duration, temperature, blood pressure, heart rate,
SpO₂, respiratory rate, medical history, the proposed diagnosis, and an optional question.

## Tech stack

| Layer | Tools |
|---|---|
| Backend | Java 21, Spring Boot 3.3 (Web), Jackson |
| AI | Groq API (OpenAI-compatible), `llama-3.3-70b-versatile` |
| Frontend | HTML, CSS, vanilla JavaScript (served by Spring Boot) |
| Build | Maven (wrapper included) |

## Running locally

**Requirements:** JDK 21 or newer, and a free API key from https://console.groq.com.

```bash
export GROQ_API_KEY=your-groq-api-key     # PowerShell: $env:GROQ_API_KEY="..."
./mvnw spring-boot:run                    # Windows: mvnw.cmd spring-boot:run
```

Open http://localhost:8080/Index.html. `GET /api/health` confirms the server is up.

## API

| Method | Endpoint | Purpose |
|---|---|---|
| `POST` | `/api/analyse` | Analyse a case and return the structured second opinion |
| `GET` | `/api/health` | Health check |

## Important

DaktariAssist is a **decision-support prototype**. It is not a medical device and has not
been clinically validated. Clinical judgement always rests with the treating clinician.

## License

[MIT](LICENSE)
