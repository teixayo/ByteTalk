import { createContext, useContext, useEffect, useRef, useState } from "react";
import toast from "react-hot-toast";

const SocketContext = createContext();

export const SocketProvider = ({ children }) => {
  const [socket, setSocket] = useState(null);
  const reconnectTimeoutRef = useRef(null);
  const [bulkMessages, setBulkMessages] = useState([]);
  const [bulkLength, setBulkLength] = useState(0);

  const [status, setStatus] = useState({});
  const [newMessage, setNewMessage] = useState({});

  const [sendStatus, setSendStatus] = useState(true);

  const [wsReady, setWsReady] = useState(false);

  const [privetChannels, setPrivetChannels] = useState([]);
  const [activeChat, setActiveChat] = useState(null);

  const [canMessage, setCanMessage] = useState(null);

  const [isFirstBulk, setIsFirstBulk] = useState(true);

  const [initialScrollDone, setInitialScrollDone] = useState(false);


  const connectWebSocket = () => {
    const ws = new WebSocket(import.meta.env.VITE_SERVER_WEBSOCKET_URL);
    ws.onopen = () => {
      // console.log("✅ WebSocket connected");
      setSocket(ws);
    };

    ws.onmessage = (event) => {
      const data = JSON.parse(event.data);

      // console.log("📨 WS received:", data);

      if (data.type == "Status") {
        if (data.code === "1000") {
          // setTimeout(() => {
          //   toast.success("Login was successful");
          // }, 1000);
        } else if (data.code === "1001") {
          toast.error("Incorrect username or password");
        } else if (data.code === "1004") {
          setTimeout(() => {
            toast.success("Sign up was successful");
          }, 1000);
        } else if (data.code === "1005") {
          toast.error("This username is already taken");
        } else if (data.code === "1006") {
          toast.error("Invalid username format");
        } else if (data.code === "1007") {
          toast.error("Invalid password format");
        } else if (data.code === "1008") {
          toast.error("Please don't spam.");
        }
        setStatus(data);
      }

      if (data.type === "LoginToken") {
        // console.log("login token::::::::::::::", data.token);
        document.cookie = `token=${data.token}; path=/; SameSite=Lax`;
      }

      if (data.type === "BulkMessages") {
        if (location.pathname == "/chat" && data.channel == "global") {
          setBulkLength(data.messages.length);
          if (data.messages.length < 1) {
            setBulkMessages([]);
            return;
          }

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
                channel: data.channel,
                content: msg.content,
                time: shortTime,
                username: msg.username,
                timecode: msg.date,
              };
            });

          setBulkMessages((prev) => {
            if (prev[0]) {
              if (newMessages[0].channel == prev[0].channel) {
                setIsFirstBulk(false);

                return [...newMessages, ...prev];
              } else {
                setIsFirstBulk(true);

                return [...newMessages];
              }
            } else {
              return [...newMessages];
            }
          });
        } else {
          if (location.pathname == `/chat/${data.channel}`) {
            setBulkLength(data.messages.length);
            if (data.messages.length < 1) {
              setBulkMessages([]);
              return;
            }

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
                  channel: data.channel,
                  content: msg.content,
                  time: shortTime,
                  username: msg.username,
                  timecode: msg.date,
                };
              });

            setBulkMessages((prev) => {
              if (prev[0]) {
                if (newMessages[0].channel == prev[0].channel) {
                  setIsFirstBulk(false);

                  const filteredMsg = [...newMessages, ...prev];
                  return [...filteredMsg];
                } else {
                  setIsFirstBulk(true);

                  const filteredMsg = [...newMessages];
                  return [...filteredMsg];
                }
              } else {
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
        setPrivetChannels(data.channels);
      }

      if (data.type == "CanSendMessage") {
        setCanMessage(data);
      }
    };

    ws.onerror = (err) => {
      // console.error("❌ WebSocket error:", err);
      document.cookie =
        "token=; path=/; expires=Thu, 01 Jan 1970 00:00:00 UTC;";
    };

    ws.onclose = (event) => {
      // console.warn("❌ WebSocket disconnected", event);

      setSocket(null);

      reconnectTimeoutRef.current = setTimeout(() => {
        // console.log("🔁 Trying to reconnect...");

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
        setBulkMessages,
        bulkMessages,
        status,
        newMessage,
        sendStatus,
        setSendStatus,
        bulkLength,
        wsReady,
        setWsReady,
        privetChannels,
        setPrivetChannels,
        setActiveChat,
        activeChat,
        canMessage,
        isFirstBulk,
        setIsFirstBulk,
        initialScrollDone,
        setInitialScrollDone,
      }}
    >
      {children}
    </SocketContext.Provider>
  );
};

export const useSocket = () => useContext(SocketContext);
