import { createContext, useContext, useEffect, useRef, useState } from "react";

const SocketContext = createContext();

export const SocketProvider = ({ children }) => {
  const [socket, setSocket] = useState(null);
  const pingIntervalRef = useRef(null);
  const reconnectTimeoutRef = useRef(null);

  const connectWebSocket = () => {
    const ws = new WebSocket("ws://localhost:25565");

    ws.onopen = () => {
      console.log("✅ WebSocket connected");
      setSocket(ws);

      // هر 5 ثانیه یک پکت Pong بفرست برای زنده موندن
      pingIntervalRef.current = setInterval(() => {
        if (ws.readyState === WebSocket.OPEN) {
          ws.send(JSON.stringify({ type: "Pong" }));
          console.log("📡 Pong sent");
        }
      }, 5000);
    };

    ws.onmessage = (event) => {
      console.log("📨 Message received:", event.data);
    };

    ws.onerror = (err) => {
      console.error("❌ WebSocket error:", err);
    };

    ws.onclose = (event) => {
      console.warn("❌ WebSocket disconnected", event);

      // پاک‌سازی
      clearInterval(pingIntervalRef.current);
      pingIntervalRef.current = null;
      setSocket(null);

      // اتصال مجدد بعد از 2 ثانیه
      reconnectTimeoutRef.current = setTimeout(() => {
        console.log("🔁 Trying to reconnect...");
        connectWebSocket();
      }, 2000);
    };
  };

  useEffect(() => {
    connectWebSocket();

    return () => {
      clearInterval(pingIntervalRef.current);
      clearTimeout(reconnectTimeoutRef.current);
      if (socket) socket.close();
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <SocketContext.Provider value={{ socket }}>
      {children}
    </SocketContext.Provider>
  );
};

export const useSocket = () => useContext(SocketContext);
