import { createContext, useContext, useEffect, useState } from "react";

const SocketContext = createContext();

export const SocketProvider = ({ children }) => {
  const [socket, setSocket] = useState(null);

  useEffect(() => {
    const ws = new WebSocket("ws://localhost:25565");
    ws.onopen = () => console.log("✅ WebSocket connected");
    ws.onclose = () => console.log("❌ WebSocket disconnected");
    ws.onerror = (err) => console.error("WebSocket error", err);
    ws.onmessage = (event) => {
      console.log("📨 Message received:", event.data);
    };

    setSocket(ws);

    return () => ws.close();
  }, []);

  return (
    <SocketContext.Provider value={{ socket }}>
      {children}
    </SocketContext.Provider>
  );
};

export const useSocket = () => useContext(SocketContext);
