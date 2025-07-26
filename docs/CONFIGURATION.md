
# ⚙️ ByteTalk Configuration & Deployment Guide

This guide outlines how to configure `config.yml`, build Docker images, and understand ByteTalk's storage system.

---

## 📄 `config.yml` Overview

ByteTalk uses [config.yml](../backend/src/main/resources/config.yml) to define system behavior, including database connections, networking, rate limits, and more.

---

## 💾 Storage System

ByteTalk supports two storage backends:

| Type      | Description                                                                                    | Toggle          |
|-----------|------------------------------------------------------------------------------------------------|-----------------|
| **Memory**| Stores data temporarily, Best for development/testing                                          | `mongo.toggle: false` |
| **MongoDB** | Persists messages and channel and user data to a MongoDB instance, Recommended for production. | `mongo.toggle: true` |

> 🔁 Redis can be used optionally for caching and microservice synchronization via Pub/Sub. Enable with `redis.toggle: true`

---

## 🔑 Multi-node Deployment Note

If you're deploying ByteTalk in a **multi-node microservice architecture**, the following are **required**:

- **Enable Redis**: Redis Pub/Sub is used to sync real-time messages across services. Config redis in `config.yml`.
- **Share JWT Secret**: All services must use the same `secret.txt` file for jwt authentication.


---

## 🐳 Docker Deployment

### 📦 Build & Run Frontend Docker Image

```bash
cd Frontend
docker build -t bytetalk-frontend --build-arg SERVER_WEBSOCKET_URL=ws://localhost:8080 .
docker run -d -p 3000:80 bytetalk-frontend
```

### ⚙️ Build & Run Backend Docker Image

```bash
docker build -t bytetalk-backend .
docker run -d -p 8080:8080 bytetalk-backend
```
---
