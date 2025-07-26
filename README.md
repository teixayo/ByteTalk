# ByteTalk

**ByteTalk** is a real-time chat application that supports global and private messaging, designed with scalability and microservice architecture in mind. It features a Java-based backend and a React/Tailwind frontend. ByteTalk utilizes Redis for pub/sub communication and caching, and supports both in-memory and MongoDB storage for flexible and efficient message persistence.

---

## ⚙️ Tech Stack

**Backend**
- Java 
- Netty
- WebSocket for real‑time messaging
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
- react-hot-toast (for user notifications)
- react-textarea-autosize (chat input)
- react-window (virtualized message list)

---

## 🚀 Features

- **Global Chat**: Broadcast messages to all connected users
- **Private Chat**: One‑to‑one direct messaging
- **Authentication**: Secure login/signup via JWT and password hashing (SHA256)
- **Configurable Storage**: Supports both in‑memory (for development) and MongoDB (for production)
- **Automated Testing**: Unit and integration tests with JUnit and Testcontainers
  
---

## 🏗️ Architecture

- Frontend and backend live in separate folders within the same repository.
- Real‑time messaging powered by WebSocket connections managed by Netty on the server.
- Messages are first published via Redis pub/sub; then persisted to MongoDB (if enabled).
- Authentication uses JWTs; user credentials are hashed securely before storage.
- The frontend uses React and communicates with the backend through WebSocket and REST APIs.
- UI is designed with Tailwind CSS and supports responsive layouts for both desktop and mobile.
  
---

## 💻 Installation & Local Development

1. **Prerequisites**
    - Java 21
    - Node.js ≥16

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
   Open `http://localhost:8080` (or your configured port) in your browser.

---

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
