import { createContext, useContext, useEffect, useRef, useState } from "react";

const SocketContext = createContext();

export const SocketProvider = ({ children }) => {
  const [socket, setSocket] = useState(null);
  const reconnectTimeoutRef = useRef(null);

  const connectWebSocket = () => {
    const ws = new WebSocket("ws://localhost:25565");

    ws.onopen = () => {
      console.log("✅ WebSocket connected");
      setSocket(ws);
    };

    ws.onmessage = (event) => {
      console.log("📨 Message received:", event.data);
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
    <SocketContext.Provider value={{ socket }}>
      {children}
    </SocketContext.Provider>
  );
};

export const useSocket = () => useContext(SocketContext);
