# Remento - Flashcard Learning App

Remento is a cross-platform flashcard learning app focused on manual flashcard creation, clean organization, spaced repetition, and lightweight AI support.

## Project Structure

- `backend/` - Spring Boot REST API (Kotlin)
- `frontend/` - React + TypeScript web application
- `app/` - Legacy bootstrap code (can be removed later)
- `utils/` - Shared utilities
- `telemetry/` - Telemetry facade module

## Tech Stack

### Backend
- **Kotlin** 2.2.20
- **Spring Boot** 3.4.1
- **PostgreSQL**
- **Spring Data JPA**
- **Kotlinx Serialization**

### Frontend
- **React** 18.3
- **TypeScript** 5.5
- **Vite** 5.4

## Setup Instructions

### Prerequisites
- JDK 21
- PostgreSQL 12+
- Node.js 18+ and npm

### Backend Setup

**Option 1: Using PostgreSQL (Recommended for production-like setup)**

1. **Start PostgreSQL with Docker Compose**:
   ```bash
   docker-compose up -d
   ```
   
   Or manually create the database:
   ```sql
   CREATE DATABASE remento;
   CREATE USER remento WITH PASSWORD 'remento';
   GRANT ALL PRIVILEGES ON DATABASE remento TO remento;
   ```

2. **Run the backend**:
   ```bash
   ./gradlew :backend:bootRun
   ```

The backend will start on `http://localhost:8080`

### Frontend Setup

1. **Install dependencies**:
   ```bash
   cd frontend
   npm install
   ```

2. **Start development server**:
   ```bash
   npm run dev
   ```
   
   The frontend will start on `http://localhost:3000`

## Development

### Backend
- Run tests: `./gradlew :backend:test`
- Build: `./gradlew :backend:build`
- Run: `./gradlew :backend:bootRun`

### Frontend
- Development: `npm run dev`
- Build: `npm run build`
- Preview: `npm run preview`

## API Endpoints

### Subjects
- `GET /api/subjects` – list subjects with topic summaries
- `GET /api/subjects/{id}` – get a subject with its topics
- `POST /api/subjects` – create a subject
- `PUT /api/subjects/{id}` – rename a subject
- `DELETE /api/subjects/{id}` – remove a subject and its topics

### Topics
- `GET /api/subjects/{subjectId}/topics` – list topics for a subject
- `GET /api/topics/{topicId}` – get a topic with its study passage and flashcards
- `POST /api/subjects/{subjectId}/topics` – create a topic (study passage limit: 2000 chars)
- `PUT /api/topics/{topicId}` – update topic name or study passage
- `DELETE /api/topics/{topicId}` – delete a topic

### Flashcards
- `GET /api/topics/{topicId}/flashcards` – list flashcards in a topic
- `POST /api/topics/{topicId}/flashcards` – create a flashcard (enforces unique question per topic)
- `DELETE /api/topics/{topicId}/flashcards/{flashcardId}` – delete a flashcard

## Architecture

The backend follows a clean architecture pattern:
- **Domain Layer**: Entities (`Subject`, `Topic`, `Flashcard`)
- **Repository Layer**: Data access interfaces
- **Service Layer**: Business logic with interfaces for extensibility
- **Controller Layer**: REST API endpoints
- **DTO Layer**: Data transfer objects for API communication

The frontend is structured for easy migration to React Native/Expo:
- API client is abstracted and can be swapped for mobile implementations
- Components are reusable and can be adapted to React Native components

## Next Steps

- [ ] Implement spaced repetition system
- [ ] Add flashcard quiz interface
- [ ] Add topic summaries
- [ ] Prepare for mobile (React Native/Expo)
- [ ] Add AI features (GPT integration)
