# Quick Start Guide

## Prerequisites
- JDK 21
- Node.js 18+ and npm
- Docker Desktop (for PostgreSQL)

## Quick Start

1. **Start PostgreSQL**:
   ```bash
   docker-compose up -d
   ```

2. **Start Backend**:
   ```bash
   ./gradlew :backend:bootRun
   ```

3. **Start Frontend** (in a new terminal):
   ```bash
   cd frontend
   npm install
   npm run dev
   ```

4. **Open Browser**:
   - Frontend: http://localhost:3000
   - Backend API root: http://localhost:8080/api/subjects

## Testing the Application

1. Create a **Subject** (e.g., "Science")
2. Select the subject and add a **Topic** with a study passage
3. Select the topic to view its deck
4. Click "+ Add Flashcard" to add questions/answers
5. Delete a flashcard to verify the hierarchy updates automatically

## Troubleshooting

- **Backend won't start**: Make sure port 8080 is not in use
- **Frontend won't start**: Make sure port 3000 is not in use
- **Database connection errors**: Ensure PostgreSQL (docker-compose) is running
- **CORS errors**: Ensure backend is running on port 8080

