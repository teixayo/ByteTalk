
# ⚙️ ByteTalk Configuration & Deployment Guide

This guide outlines how to configure `config.yml`, build Docker images, and understand ByteTalk's storage system

---

## 📄 `config.yml` Overview

ByteTalk uses `config.yml` to define system behavior, including database connections, networking, rate limits, and more


---

## 💾 Storage System

ByteTalk supports two storage backends:

| Type         | Description                                                                        | Toggle          |
|--------------|------------------------------------------------------------------------------------|-----------------|
| **In-Memory**| Stores data temporarily, Best for development/testing                              | `mongo.toggle: false` |
| **MongoDB**  | Persists messages and user data to a MongoDB instance. Recommended for production  | `mongo.toggle: true` |

> 🔁 Redis can be used optionally for caching and microservice synchronization via Pub/Sub. Enable with `redis.toggle: true`

---

## 🔑 Multi-node Deployment Note

If you're deploying ByteTalk in a **multi-node microservice architecture**, ensure that **all nodes share the same JWT secret key file (`secret.txt`)**

You can use a shared volume, secret manager, or centralized config server for this purpose.

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

> Make sure `config.yml` and `secret.txt` are mounted or copied inside the container

---

Feel free to contribute improvements or raise issues on [GitHub](https://github.com/teixayo/ByteTalk).
