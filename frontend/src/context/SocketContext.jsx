import { createContext, useContext, useEffect, useRef, useState } from "react";
const SocketContext = createContext();

let initialLoaded = false;
let firstTime = false;

export const SocketProvider = ({ children }) => {
  const [socket, setSocket] = useState(null);
  const reconnectTimeoutRef = useRef(null);
  const [bulkMessages, setBulkMessages] = useState([]);
  const [bulkLength, setBulkLength] = useState(0);

  const [loginToken, setLoginToken] = useState(null);
  const [status, setStatus] = useState({});
  const [newMessage, setNewMessage] = useState({});

  // const [loginCheck, setLoginCheck] = useState(false);
  const [sendStatus, setSendStatus] = useState(true);

  const [wsReady, setWsReady] = useState(false);

  const [privetChannels, setPrivetChannels] = useState([]);
  const [activeChat, setActiveChat] = useState(null)

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
        if (data.code == "1002") {
          // if (!initialLoaded) {
          //   data.messages.map((msg) => {
          //     const date = new Date(msg.date);
          //     const shortTime = date.toLocaleTimeString("en-US", {
          //       hour: "2-digit",
          //       minute: "2-digit",
          //       hour12: true,
          //     });
          //     if (msg.content != "") {
          //       setBulkMessages((prev) => [
          //         ...prev,
          //         {
          //           content: msg.content,
          //           time: shortTime,
          //           username: msg.username,
          //           timecode: msg.date,
          //         },
          //       ]);
          //     }
          // };
          // }
        }
        setStatus(data);
      }

      if (data.type === "LoginToken") {
        console.log("login token::::::::::::::", data.token);
        localStorage.setItem("token", data.token);
        setLoginToken(data.token);
      }

      if (data.type === "BulkMessages") {
        if (location.pathname == "/chat" && data.channel == "global") {
          if (!initialLoaded) {
            data.messages.map((msg) => {
              const date = new Date(msg.date);

              const shortTime = date.toLocaleTimeString("en-US", {
                hour: "2-digit",
                minute: "2-digit",
                hour12: true,
              });

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
            setBulkLength(data.messages.length);
            if (data.messages.length < 1) return;

            const newMessages = data.messages
              .filter((msg) => msg.content !== "")
              .map((msg) => {
                const date = new Date(msg.date);

                const shortTime = date.toLocaleTimeString("en-US", {
                  hour: "2-digit",
                  minute: "2-digit",
                  hour12: true,
                });

                return {
                  content: msg.content,
                  time: shortTime,
                  username: msg.username,
                  timecode: msg.date,
                };
              });
            // حالا فقط یکبار state رو آپدیت کن:
            setBulkMessages((prev) => [...newMessages, ...prev]);
          }
        } else {
          if (location.pathname == `/chat/${data.channel}`) {
            setBulkLength(data.messages.length);
            if (data.messages.length < 1) return;

            const newMessages = data.messages
              .filter((msg) => msg.content !== "")
              .map((msg) => {
                const date = new Date(msg.date);

                const shortTime = date.toLocaleTimeString("en-US", {
                  hour: "2-digit",
                  minute: "2-digit",
                  hour12: true,
                });

                return {
                  content: msg.content,
                  time: shortTime,
                  username: msg.username,
                  timecode: msg.date,
                };
              });
            // حالا فقط یکبار state رو آپدیت کن:
            setBulkMessages((prev) => {
              if (firstTime) {
                return [...newMessages, ...prev];
              } else {
                firstTime = true;
                console.log(newMessages);
                return [...newMessages];
              }
            });
          }
        }
      }

      if (data.type == "SendMessage") {
        setNewMessage(data);
      }

      if (data.type == "UserPrivateChannels") {
        const allChannels = data.channels;
        console.log(allChannels);
        setPrivetChannels(allChannels);
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
      }, 600);
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
        bulkLength,
        // setLoginCheck,
        // loginCheck,
        wsReady,
        setWsReady,
        privetChannels,
        setActiveChat,
        activeChat
      }}
    >
      {children}
    </SocketContext.Provider>
  );
};

export const useSocket = () => useContext(SocketContext);
