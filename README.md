# ByteTalk 🔌💬

**ByteTalk** is a high-performance, cross-platform real-time message system built using Java, designed for both scalability and reliability. It supports Android and Desktop clients, WebSocket-based communication, Redis message syncing, and database flexibility with MongoDB/PostgreSQL. The project is designed to handle high message throughput while maintaining efficient and secure communication.

## ✨ Features

- **Real-Time Messaging**: Uses WebSocket for low-latency communication.
- **Cross-Platform**: Website (React) and Desktop client (Tauri) and Android client (React Native).
- **Scalable Architecture**: Redis synchronization for message syncing across multiple instances.
- **Flexible Database Support**: Uses MongoDB for user data storage.
- **Authentication**: Basic token-based authentication for user security.
- **CI/CD Pipeline**: Automated testing and deployment with Docker and GitHub Actions.
- **Full Test Coverage**: Unit and integration tests for key components.

## 📦 Tech Stack

- **Backend**: Java, Netty, WebSocket, Redis, MongoDB/PostgreSQL
- **Client**: Website (React), Android (ReactNative), Desktop (Tauri)
- **Database**: MongoDB, Redis, ElasticSearch
- **Testing**: JUnit, TestContainer
- **CI/CD**: GitHub Actions, Docker
- **Authentication**: Token-based (JWT-like mechanism, custom implementation)
