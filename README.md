# MCS AI Platform MVP (IntelliJ IDEA Monorepo)

This project keeps **Spring Boot backend** and **Next.js + Tailwind frontend** inside one repository so you can open the root folder directly in IntelliJ IDEA.

## Structure

- `backend/` - Spring Boot API + MongoDB + JWT + PDF/TXT/MD upload
- `frontend/` - Next.js App Router + Tailwind UI

## What works in this MVP

- User register / login
- Create and list agents
- Upload PDF / TXT / Markdown files
- Split documents into chunks and store them in MongoDB
- Chat with an agent
- Use simple knowledge search over uploaded chunks
- Persist chat sessions and messages
- OpenAI adapter with mock mode enabled by default

## Prerequisites

- Java 21
- Maven 3.9+
- Node.js 20.9+
- MongoDB running locally on `mongodb://localhost:27017`

## Run backend

1. Open the root project in IntelliJ IDEA.
2. Import Maven project from `backend/pom.xml`.
3. Make sure MongoDB is running.
4. Start `AiPlatformApplication`.

Backend runs on:

- `http://localhost:8081`
- Swagger UI: `http://localhost:8081/swagger-ui/index.html`

## Run frontend

1. In IntelliJ terminal:
   ```bash
   cd frontend
   npm install
   cp .env.local.example .env.local
   npm run dev
   ```
2. Open `http://localhost:3000`

## OpenAI real API mode

Backend default is mock mode so you can run everything without an API key.

To switch to real OpenAI responses:

1. Edit `backend/src/main/resources/application.yml`
2. Set:
   ```yaml
   app:
     llm:
       mock-enabled: false
   ```
3. Export your key before starting backend:
   ```bash
   export OPENAI_API_KEY=your_key_here
   ```

## MongoDB collections used

- `users`
- `agents`
- `chat_sessions`
- `chat_messages`
- `document_files`
- `document_chunks`

## Suggested next upgrades

- Replace basic keyword retrieval with MongoDB Atlas Vector Search
- Add Ollama and OpenRouter adapters
- Add agent edit page in frontend
- Add delete agent / delete document
- Add streaming chat responses
