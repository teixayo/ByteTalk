import { useState, useEffect, useRef } from "react";
import { useSocket } from "../context/SocketContext";
import { FixedSizeList as List } from "react-window";
import linkifyHtml from "linkify-html";
import DOMPurify from "dompurify";

const convertMessage = (text) => {
  const options = {
    target: "_blank",
    rel: "noopener noreferrer",
    // فقط URLهایی که با http/https شروع می‌شن اجازه می‌دیم
    validate: {
      url: (value) =>
        value.startsWith("http://") || value.startsWith("https://"),
    },
  };

  const linked = linkifyHtml(text, options);

  // پاکسازی HTML از هر نوع کد خطرناک
  return DOMPurify.sanitize(linked);
};

let flag = true;

const Chat = () => {
  const [text, setText] = useState("");
  const {
    socket,
    bulkMessages,
    newMessage,
    sendStatus,
    setSendStatus,
    messages,
    setMessages,
    bulkLength,
    setLoginCheck,
    loginCheck,
  } = useSocket();

  const [isAtBottom, setIsAtBottom] = useState(true);
  const [localMessages, setLocalMessages] = useState([]);
  const debounceTimeout = useRef(null);
  const listRef = useRef(null);
  const [initialScrollDone, setInitialScrollDone] = useState(false);

  useEffect(() => {
    console.log("bulk", bulkMessages.length);
    if (bulkMessages?.length > 0 && listRef.current) {
      console.log("run");
      setMessages([...bulkMessages, ...localMessages]);

      listRef.current.scrollToItem(bulkLength + 1, "start");

      setTimeout(() => {
        setInitialScrollDone(true);
      }, 300);
    }
    if (loginCheck) {
      setTimeout(() => {
        listRef.current.scrollToItem(bulkMessages.length, "end");
        setLoginCheck(false);
      }, 1);
    }
  }, [bulkMessages]);

  useEffect(() => {
    if (newMessage.date) {
      const date = new Date(newMessage.date);
      const originalTime = date.toLocaleTimeString();
      const [time, period] = originalTime.split(" ");
      const shortTime = time.slice(0, 4) + " " + period;

      setMessages((prev) => [
        ...prev,
        {
          content: newMessage.content,
          time: shortTime,
          username: newMessage.username,
        },
      ]);

      setTimeout(() => {
        listRef.current.scrollToItem(messages.length + 3, "end");
      }, 1);
    }
  }, [newMessage]);

  useEffect(() => {
    if (isAtBottom && listRef.current && flag) {
      listRef.current.scrollToItem(bulkMessages.length - 1, "start");

      console.log(bulkMessages.length - 1);
      setTimeout(() => {
        flag = false;
      }, 100);
    }
  }, [messages]);

  const sendMessage = () => {
    const user = localStorage.getItem("username");
    const timestamp = Date.now();
    const date = new Date(timestamp);
    const originalTime = date.toLocaleTimeString();
    const [time, period] = originalTime.split(" ");
    const shortTime = time.slice(0, 4) + " " + period;

    const msg = {
      content: text,
      time: shortTime,
      username: user,
      timecode: timestamp,
    };

    setLocalMessages((prev) => [...prev, msg]);
    setMessages((prev) => [...prev, msg]);

    if (socket && socket.readyState == WebSocket.OPEN) {
      const messagePayload = {
        type: "SendMessage",
        content: text,
      };
      socket.send(JSON.stringify(messagePayload));
    }
    setTimeout(() => {
      listRef.current.scrollToItem(messages.length + 3, "end");
    }, 1);
  };

  const Row = ({ index, style }) => {
    const msg = messages[index];

    return (
      <div style={style} className="flex">
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
        <div>
          <div className="flex mt-0.5">
            <p className="mr-3 ml-1">{msg.username}</p>
            <p className="text-xs mt-1">{msg.time}</p>
          </div>
          <p
            className="mr-10 text-sm"
            dangerouslySetInnerHTML={{ __html: convertMessage(msg.content) }}
          ></p>
        </div>
      </div>
    );
  };

  return (
    <div className="h-screen flex flex-col text-gray-300">
      <div className="flex-1">
        <List
          ref={listRef}
          height={window.innerHeight - 70}
          itemCount={messages.length}
          itemSize={80}
          width={"100%"}
          onItemsRendered={({ visibleStartIndex }) => {
            if (
              visibleStartIndex === 0 &&
              sendStatus &&
              !debounceTimeout.current &&
              initialScrollDone
            ) {
              setSendStatus(false);
              console.log("🟡 کاربر به بالای لیست رسید");

              debounceTimeout.current = setTimeout(() => {
                debounceTimeout.current = null;
                setSendStatus(true);
              }, 1000);

              const firstMessageTimecode = messages[0]?.timecode;
              if (firstMessageTimecode) {
                socket.send(
                  JSON.stringify({
                    type: "RequestBulkMessage",
                    date: firstMessageTimecode - 1,
                  })
                );
              }
            }
          }}
        >
          {Row}
        </List>
      </div>

      <div className="h-20 px-3 pb-2">
        <input
          type="text"
          value={text}
          onChange={(e) => setText(e.target.value)}
          onKeyDown={(e) => {
            if (e.code === "Enter" && e.target.value.trim() !== "") {
              sendMessage();
              setText("");
            }
          }}
          placeholder="Message"
          className="w-full h-full pl-4 pb-1 bg-neutral-700 border border-black rounded-lg"
        />
      </div>
    </div>
  );
};

export default Chat;
