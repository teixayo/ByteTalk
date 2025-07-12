import { createContext, useContext, useEffect, useRef, useState } from "react";

const SocketContext = createContext();

let initialLoaded = false;

export const SocketProvider = ({ children }) => {
  const [socket, setSocket] = useState(null);
  const reconnectTimeoutRef = useRef(null);
  const [bulkMessages, setBulkMessages] = useState([]);
  const [bulkLength, setBulkLength] = useState(0)

  const [loginToken, setLoginToken] = useState(null);
  const [status, setStatus] = useState({});
  const [newMessage, setNewMessage] = useState({});

  const [messages, setMessages] = useState([]);


  const [sendStatus, setSendStatus] = useState(true);

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
        setStatus(data);
      }

      if (data.type === "LoginToken") {
        console.log("login token::::::::::::::", data.token);
        localStorage.setItem("token", data.token);
        setLoginToken(data.token);
      }

      if (data.type === "BulkMessages") {
        if (!initialLoaded) {
          data.messages.map((msg) => {
            const date = new Date(msg.date);
            const originalTime = date.toLocaleTimeString();
            const [time, period] = originalTime.split(" ");
            const shortTime = time.slice(0, 4) + " " + period;

            if (msg.content != "") {
              setBulkMessages((prev) => [
                ...prev,
                {
                  content: msg.content,
                  time: shortTime,
                  username: msg.username,
                  timecode: msg.date,
                },
              ]);
            }
          });
          console.log(initialLoaded);
          initialLoaded = true;
        } else {
          console.log(data.messages.length);
          setBulkLength(data.messages.length)
          if (data.messages.length < 1) return;

          const newMessages = data.messages
            .filter((msg) => msg.content !== "")
            .map((msg) => {
              const date = new Date(msg.date);
              const originalTime = date.toLocaleTimeString();
              const [time, period] = originalTime.split(" ");
              const shortTime = time.slice(0, 4) + " " + period;

              return {
                content: msg.content,
                time: shortTime,
                username: msg.username,
                timecode: msg.date,
              };
            });
          console.log(newMessages);
          // حالا فقط یکبار state رو آپدیت کن:
          setBulkMessages((prev) => [...newMessages.reverse(), ...prev]);
        }
      }

      if (data.type == "SendMessage") {
        setNewMessage(data);
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
      value={{
        socket,
        bulkMessages,
        loginToken,
        status,
        newMessage,
        sendStatus,
        setSendStatus,
        messages,
        setMessages,
        bulkLength
      }}
    >
      {children}
    </SocketContext.Provider>
  );
};

export const useSocket = () => useContext(SocketContext);
