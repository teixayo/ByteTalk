import { useState, useEffect } from "react";
import { useSocket } from "../context/SocketContext";

const Chat = () => {
  const [text, setText] = useState("");
  const [message, setMessage] = useState([]);
  const { socket } = useSocket();

  useEffect(() => {
    socket.onmessage = (event) => {
      console.log(JSON.parse(event.data));
    };
  }, [socket]);

  const sendMessage = () => {
    console.log("send message run");
    setMessage((prev) => [...prev, text]);
    if (socket && socket.readyState == WebSocket.OPEN) {
      console.log("websocket is run");
      const messagePayload = {
        type: "SendMessage",
        content: text,
      };
      showMessages();
      socket.send(JSON.stringify(messagePayload));
    }
  };

  const showMessages = () => {
    socket.onmessage = (event) => {
      console.log(JSON.parse(event));
    };
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
            {message.map((msg, i) => (
              <p key={i}>{msg}</p>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};

export default Chat;
