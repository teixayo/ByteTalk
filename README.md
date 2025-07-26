# ByteTalk

**ByteTalk** is a real-time chat application that supports global and private messaging, designed with scalability and microservice architecture in mind. It features a Java-based backend and a React/Tailwind frontend. ByteTalk utilizes Redis for pub/sub communication and caching, and supports both in-memory and MongoDB storage for flexible and efficient message persistence.

---

## ⚙️ Tech Stack

**Backend**
- Java 
- Netty
- WebSocket
- Redis
- MongoDB
- JWT for authentication
- JUnit & Testcontainers 
- GitHub Actions CI/CD
- Docker
  
**Frontend**
- React 19 (with Hooks)
- Vite
- JavaScript (ES6+)
- Tailwind CSS
- React Router v7
- Formik & Yup (for form handling and validation)
- React-Hot-Toast (for user notifications)
- React-Textarea-Autosize (chat input)
- React-Window (virtualized message list)

---

## 🚀 Features

- **Global Chat**: Broadcast messages to all connected users
- **Private Chat**: One‑to‑one direct messaging
- **Microservice Sync**: Redis Pub/Sub is used for real-time synchronization and message broadcasting across microservices
- **Authentication**: Secure login/signup via JWT and password hashing (SHA256)
- **Configurable Storage**: Supports both in‑memory (for development) and MongoDB (for production)
- **Automated Testing**: Unit and integration tests with JUnit and Testcontainers
  
---

## 💻 Installation & Local Development

1. **Prerequisites**
    - Java 21
    - Node.js ≥16

2. **Clone the repo**
   ```bash
   git clone https://github.com/teixayo/ByteTalk.git
   cd ByteTalk
   ```

3. **Backend Setup**
   ```bash
   ./gradlew shadowJar
   java -jar backend/build/libs/backend-1.0-SNAPSHOT-ByteTalk.jar
    ```

4. **Frontend Setup**
   ```bash
   cd frontend
   npm install
   npm run dev
   ```

5. **Access the App**  
   Open `http://localhost:8080` (or your configured port) in your browser.

---

---

## 🔧 Configuration

See the [Configuration Guide](docs/configuration.md) for environment variables, Redis/MongoDB setup, port settings, and storage options.

## 🧪 Testing

- **Unit Tests:** JUnit (backend)
- **Integration Tests:** JUnit + Testcontainers (spins up temporary Redis & MongoDB)
- Run all tests with:
  ```bash
  ./gradlew test
  ```

---

## 🚧 Roadmap & Future Plans

- **Chain Encryption**: End‑to‑end encryption for all messages.
- **Media Support**: Camera, voice messages with encryption.
- **P2P Mode**: Direct, decentralized peer‑to‑peer connections.

---

## 👨‍💻 Contributors

- Backend: [Ali Nikbakht](https://github.com/teixayo)
- Frontend: [Adel Nouri](https://github.com/AdelNouri)

---

## 📄 License

[MIT License](LICENSE)

--- 
