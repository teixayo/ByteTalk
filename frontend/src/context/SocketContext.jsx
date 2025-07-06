import { createContext, useContext, useEffect, useRef, useState } from "react";

const SocketContext = createContext();

export const SocketProvider = ({ children }) => {
  const [socket, setSocket] = useState(null);
  const reconnectTimeoutRef = useRef(null);
  const [bulkMessages, setBulkMessages] = useState([]);
  const [loginToken, setLoginToken] = useState(null);
  const [status, setStatus] = useState({})


  const connectWebSocket = () => {
    const ws = new WebSocket("ws://localhost:25565");

    ws.onopen = () => {
      console.log("✅ WebSocket connected");
      setSocket(ws);
    };

    ws.onmessage = (event) => {
      const data = JSON.parse(event.data);

      console.log("📨 WS received:", data);

      if (data.type == "Status") {
        setStatus(data)
      }

      if (data.type === "LoginToken") {
        console.log("login token::::::::::::::", data.token)
        localStorage.setItem("token", data.token);
        setLoginToken(data.token);
      }

      if (data.type === "BulkMessages") {
        setBulkMessages(data);
      }
    };

    ws.onerror = (err) => {
      console.error("❌ WebSocket error:", err);
    };

    ws.onclose = (event) => {
      console.warn("❌ WebSocket disconnected", event);

      setSocket(null);

      reconnectTimeoutRef.current = setTimeout(() => {
        console.log("🔁 Trying to reconnect...");
        connectWebSocket();
      }, 1000);
    };
  };

  useEffect(() => {
    connectWebSocket();

    return () => {
      clearTimeout(reconnectTimeoutRef.current);
      if (socket) socket.close();
    };
  }, []);

  return (
    <SocketContext.Provider
      value={{ socket, bulkMessages, loginToken, status }}
    >
      {children}
    </SocketContext.Provider>
  );
};

export const useSocket = () => useContext(SocketContext);
