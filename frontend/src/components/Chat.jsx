import { useState, useEffect } from "react";
import { useSocket } from "../context/SocketContext";

import { useAtom } from "jotai";
import { bulkMessagesAtom } from "../atoms/chatAtoms";

const Chat = () => {
  const [text, setText] = useState("");
  const [message, setMessage] = useState([]);
  const { socket } = useSocket();
  const [bulkMessages] = useAtom(bulkMessagesAtom);
  // const [flag, setFlag] = useState(true);

  useEffect(() => {
    if (!socket) {
      console.log("socket isnt ready");
      return;
    } else {
      console.log("socket is ready");
      setMessages();
    }

    const username = localStorage.getItem("username");
    const token = localStorage.getItem("token");
    console.log(username, token);

    const loginTokenPayload = {
      type: "Login",
      name: username,
      token: token,
    };
    console.log(loginTokenPayload)
    socket.send(JSON.stringify(loginTokenPayload));
    socket.onmessage = (event) => {
      const data = JSON.parse(event.data);
      console.log("📨 Message received:", data);

      if (data.type == "LoginToken") {
        console.log("Data: ", data);
        localStorage.setItem("token", data.token);
      }
    };
  }, [socket]);

  // useEffect(() => {
  //   if (!socket) {
  //     console.log("socket isnt ready");
  //     return;
  //   } else {
  //     console.log("socket is ready");
  //     // setMessages();
  //     const username = localStorage.getItem("username");
  //     const token = localStorage.getItem("token");
  //     console.log(username, token);
  
  //     const loginTokenPayload = {
  //       type: "Login",
  //       username: username,
  //       token: token,
  //     };
  //     console.log(loginTokenPayload);
  //     socket.send(JSON.stringify(loginTokenPayload));
  //   }
  // }, []);

  useEffect(() => {
    // if (flag) {
    //   setMessages();
    // }
  }, [bulkMessages]);

  const sendMessage = () => {
    console.log("send message run");
    setMessage((prev) => [...prev, text]);
    // setMessages();
    console.log(message);
    if (socket && socket.readyState == WebSocket.OPEN) {
      console.log("websocket is run");
      const messagePayload = {
        type: "SendMessage",
        content: text,
      };
      // showMessages();
      socket.send(JSON.stringify(messagePayload));
    }
  };

  const setMessages = () => {
    console.log(bulkMessages);
    console.log(message);
    // if (flag) {
    // console.log(flag);
    bulkMessages.map((msg) => {
      console.log(msg.content);
      setMessage((prev) => [...prev, msg.content]);
    });
    console.log("bulkMessages:", bulkMessages);
    // }
  };

  return (
    <div className="grid grid-cols-9 text-gray-300">
      <div className="bg-neutral-900 col-span-2 h-screen w-full"></div>
      <div className="col-span-7 h-screen w-full relative">
        <div className="absolute bottom-3 left-0 right-0 flex justify-center w-full">
          <input
            type="text"
            value={text}
            onChange={(event) => {
              setText(event.target.value);
            }}
            onKeyDown={(event) => {
              if (event.code == "Enter") {
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
            {/* {flag ? setMessages() : null}
            {flag ? setFlag(false) : null} */}

            {Array.isArray(message) &&
              message.map((msg, i) => <p key={i}>{msg}</p>)}
          </div>
        </div>
      </div>
    </div>
  );
};

export default Chat;
