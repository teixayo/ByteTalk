# ByteTalk

**ByteTalk** is a real‑time chat application featuring global and private (peer‑to‑peer) messaging. It’s built with a Java backend and a React/Tailwind frontend, and supports both in‑memory and MongoDB storage for message persistence.

---

## 🎯 Purpose

ByteTalk enables users to communicate instantly in both public (global) channels and private conversations. It’s designed to be lightweight, secure, and easily deployable—perfect for teams, communities, or as a foundation for more specialized chat solutions.

---

## ⚙️ Tech Stack

**Backend**
- Java 21
- Netty (TCP framework)
- WebSocket for real‑time messaging
- Redis (in‑memory message broker and pub/sub)
- MongoDB (persistent storage)
- JWT (JSON Web Tokens) for authentication
- JUnit & Testcontainers for testing
- GitHub Actions CI/CD
- Docker for containerization

**Frontend**
- React (with Vite)
- Tailwind CSS

---

## 🚀 Features

- **Global Chat**: Broadcast messages to all connected users.
- **Private Chat**: One‑to‑one direct messaging.
- **Group Chat**: Create and join group channels for team conversations.
- **Authentication**: Secure login/signup via JWT and password hashing.
- **Configurable Storage**: Supports both in‑memory (for development) and MongoDB (for production).
- **Automated Testing**: Unit and integration tests with JUnit and Testcontainers.

---

## 🏗️ Architecture

- Frontend and backend live in separate folders within the same repository.
- Real‑time messaging powered by WebSocket connections managed by Netty on the server.
- Messages are first published via Redis pub/sub; then persisted to MongoDB (if enabled).
- Authentication uses JWTs; user credentials are hashed securely before storage.

---

## 💻 Installation & Local Development

1. **Prerequisites**
    - Java 21
    - Node.js ≥16
    - Docker & Docker Compose (optional, but recommended)

2. **Clone the repo**
   ```bash
   git clone https://github.com/your-username/ByteTalk.git
   cd ByteTalk
   ```

3. **Backend Setup**
    - Edit `backend/config.yml` to set your desired port (defaults to `8080`) and MongoDB/Redis connection details.
    - Build and run with Maven:
      ```bash
      cd backend
      ./mvnw clean package
      java -jar target/bytetalk-backend.jar
      ```

4. **Frontend Setup**
   ```bash
   cd frontend
   npm install
   npm run dev
   ```

5. **Access the App**  
   Open `http://localhost:5173` (or your configured Vite port) in your browser.

---

## ☁️ Deployment

- **Platform:** VPS
- **Containerization:** Docker & Docker Compose
- **CI/CD:** GitHub Actions automatically builds and pushes Docker images, then deploys to your VPS.

---

## 🧪 Testing

- **Unit Tests:** JUnit (backend)
- **Integration Tests:** JUnit + Testcontainers (spins up temporary Redis & MongoDB)
- Run all tests with:
  ```bash
  cd backend
  ./mvnw test
  ```

---

## 🚧 Roadmap & Future Plans

- **Chain Encryption**: End‑to‑end encryption for all messages.
- **Media Support**: Camera, voice messages with encryption.
- **P2P Mode**: Direct, decentralized peer‑to‑peer connections.

---

## 🤝 Contributing

Contributions are welcome! Please fork the repo, create a feature branch, and open a pull request. For major changes, open an issue first to discuss your ideas.

---

## 📄 License

[MIT License](LICENSE)

---

*Built with ❤️ by [Your Name](https://github.com/your-username)*  