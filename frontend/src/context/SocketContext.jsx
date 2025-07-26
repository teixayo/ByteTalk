import { createContext, useContext, useEffect, useRef, useState } from "react";
import toast from "react-hot-toast";
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
  const [activeChat, setActiveChat] = useState(null);

  const [canMessage, setCanMessage] = useState(null);

  const [isFirstBulk, setIsFirstBulk] = useState(true);

  const connectWebSocket = () => {
    const ws = new WebSocket(import.meta.env.VITE_SERVER_WEBSOCKET_URL);
    ws.onopen = () => {
      console.log("✅ WebSocket connected");
      setSocket(ws);
    };

    ws.onmessage = (event) => {
      const data = JSON.parse(event.data);

      console.log("📨 WS received:", data);

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
        console.log("login token::::::::::::::", data.token);
        document.cookie = `token=${data.token}; path=/; SameSite=Lax`;

        // localStorage.setItem("token", data.token);
        // setLoginToken(data.token);
      }

      if (data.type === "BulkMessages") {
        if (location.pathname == "/chat" && data.channel == "global") {
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
          //   });
          //   initialLoaded = true;
          // } else {
          console.log("data.messages.length", data.messages.length);

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
          // حالا فقط یکبار state رو آپدیت کن:
          setBulkMessages((prev) => {
            console.log(prev);
            if (prev[0]) {
              const filteredMsg =
                newMessages[0].channel == prev[0].channel
                  ? [...newMessages, ...prev]
                  : [...newMessages];
              console.log(filteredMsg);
              console.log("123456789");
              return [...filteredMsg];
            } else {
              console.log("filteredMsg neshon nadeeeeeeeeeeeeeeeeee");
              return [...newMessages];
            }
          });
          // }
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
              console.log(newMessages[0].channel);
              console.log("prev:", prev);
              if (prev[0]) {
                console.log(newMessages[0].channel == prev[0].channel);

                if (newMessages[0].channel == prev[0].channel) {
                  setIsFirstBulk(false);

                  const filteredMsg = [...newMessages, ...prev];
                  return [...filteredMsg];
                } else {
                  // if (!isFirstBulk) {
                    setIsFirstBulk(true);
                  // }

                  const filteredMsg = [...newMessages];
                  return [...filteredMsg];
                }
              } else {
                return [...newMessages];
              }
              //  else {
              //   firstTime = true;
              //   console.log("dafafegwegawgehwehw",newMessages);
              //   return [...newMessages];
              // }
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
      console.error("❌ WebSocket error:", err);
      document.cookie =
        "token=; path=/; expires=Thu, 01 Jan 1970 00:00:00 UTC;";
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
        setBulkMessages,
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
        setPrivetChannels,
        setActiveChat,
        activeChat,
        canMessage,
        isFirstBulk,
        setIsFirstBulk,
      }}
    >
      {children}
    </SocketContext.Provider>
  );
};

export const useSocket = () => useContext(SocketContext);
