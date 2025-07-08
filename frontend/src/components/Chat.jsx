import { useState, useEffect } from "react";
import { useSocket } from "../context/SocketContext";

const Chat = () => {
  const [text, setText] = useState("");
  const [messages, setMessage] = useState([]);
  const { socket, bulkMessages } = useSocket();
  // const [bulkMessages] = useAtom(bulkMessagesAtom);

  // useEffect(() => {
  //   if (!socket) {
  //     console.log("socket isnt ready");
  //     return;
  //   } else {
  //     console.log("socket is ready");
  //     // setMessages();
  //   }

  //   socket.onmessage = (event) => {
  //     const data = JSON.parse(event.data);
  //     console.log("📨 Message received:", data);

  //     // if (data.type == "BulkMessages") {
  //     //   console.log("bulk: ", data);
  //     //   data.messages.map((msg) => {
  //     //     setMessage((prev) => [...prev, msg.content]);
  //     //   });
  //     // }

  //     if (data.type == "LoginToken") {
  //       console.log("Data: ", data);
  //       localStorage.setItem("token", data.token);
  //       loginWithToken();
  //       return;
  //     }
  //   };
  //   setTimeout(() => {
  //     console.log("login with token is run");
  //     loginWithToken();
  //   }, 1000);
  // }, [socket]);

  useEffect(() => {
    // if (bulkMessages.messages) {
    console.log("bulk: ", bulkMessages);
    bulkMessages.map((msg) => {
      setMessage(bulkMessages);
      console.log(msg);
    });
    // }
  }, [bulkMessages]);

  useEffect(() => {
    if (!socket) {
      console.log("socket isnt ready");
      return;
    }
    if (localStorage.getItem("token")) {
      // loginWithToken()
    }
  }, [socket]);

  // const loginWithToken = () => {
  //   const username = localStorage.getItem("username");
  //   const token = localStorage.getItem("token");
  //   const loginTokenPayload = {
  //     type: "Login",
  //     name: username,
  //     token: token,
  //   };
  //   console.log(loginTokenPayload);
  //   console.log(socket)

  //     socket.send(JSON.stringify(loginTokenPayload));
  // };

  const sendMessage = () => {
    console.log("send message run");
    const user = localStorage.getItem("username");
    const timestamp = Date.now();
    const date = new Date(timestamp);
    setMessage((prev) => [
      ...prev,
      {
        content: text,
        time: `${date.getHours()}:${
          date.getMinutes() == "0" ? "00" : date.getMinutes()
        }`,
        username: user,
      },
    ]);
    console.log("message:", messages);
    if (socket && socket.readyState == WebSocket.OPEN) {
      console.log("websocket is run");
      const messagePayload = {
        type: "SendMessage",
        content: text,
      };
      socket.send(JSON.stringify(messagePayload));
    }
  };

  return (
    <div className="grid grid-cols-11 text-gray-300">
      <div className="bg-neutral-900 col-span-3 h-screen w-full"></div>
      <div className="col-span-8 h-screen w-full relative">
        <div className="absolute bottom-3 left-0 right-0 flex justify-center w-full">
          <input
            type="text"
            value={text}
            onChange={(event) => {
              setText(event.target.value);
            }}
            onKeyDown={(event) => {
              if (
                event.code == "Enter" &&
                event.target.value.replace(/\s/g, "") != ""
              ) {
                setText("");
                sendMessage();
              }
            }}
            placeholder="Message"
            className="w-11/12 h-14 pl-4 pb-1 bg-neutral-700 border border-black rounded-lg "
          />
        </div>
        <div className=" w-full h-full">
          <div className="mt-4 list-disc pl-5">
            {Array.isArray(bulkMessages) &&
              messages.map((msg, i) => (
                <div key={i} className="flex mb-3 ">
                  <svg
                    xmlns="http://www.w3.org/2000/svg"
                    fill="none"
                    viewBox="0 0 24 24"
                    strokeWidth={0.75}
                    stroke="currentColor"
                    className="size-12"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      d="M17.982 18.725A7.488 7.488 0 0 0 12 15.75a7.488 7.488 0 0 0-5.982 2.975m11.963 0a9 9 0 1 0-11.963 0m11.963 0A8.966 8.966 0 0 1 12 21a8.966 8.966 0 0 1-5.982-2.275M15 9.75a3 3 0 1 1-6 0 3 3 0 0 1 6 0Z"
                    />
                  </svg>

                  <div className=" items-center">
                    <div className="flex mt-0.5">
                      <p className="mr-3 ml-1">{msg.username}</p>
                      <p className="text-xs mt-1">{msg.time}</p>
                    </div>
                    <div>
                      <p className="mr-10 text-sm">{msg.content}</p>
                    </div>
                  </div>
                </div>
              ))}
          </div>
        </div>
      </div>
    </div>
  );
};

export default Chat;
