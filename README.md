# ByteTalk 🔌💬

**ByteTalk** is a high-performance, cross-platform real-time message system built using Java, designed for both scalability and reliability. It supports Android and Desktop clients, WebSocket-based communication, Redis message syncing, and database flexibility with MongoDB/PostgreSQL. The project is designed to handle high message throughput while maintaining efficient and secure communication.

## ✨ Features

- **Real-Time Messaging**: Uses WebSocket for low-latency communication.
- **Cross-Platform**: Desktop client (Java ImGui) and Android client (Java).
- **Scalable Architecture**: Redis synchronization for message syncing across multiple instances.
- **Flexible Database Support**: Choose between MongoDB and PostgreSQL for user data storage.
- **Authentication**: Basic token-based authentication for secure messaging.
- **CI/CD Pipeline**: Automated testing and deployment with Docker and GitHub Actions.
- **Full Test Coverage**: Unit and integration tests for key components.

## 📦 Tech Stack

- **Backend**: Java, Netty, WebSocket, Redis, MongoDB/PostgreSQL
- **Client**: Android (Java), Desktop (Java with ImGui)
- **Database**: MongoDB, PostgreSQL (configurable)
- **Testing**: JUnit, Mockito
- **CI/CD**: GitHub Actions, Docker
- **Authentication**: Token-based (JWT-like mechanism, custom implementation)
