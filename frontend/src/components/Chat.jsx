import { useState, useEffect, useRef } from "react";
import { useSocket } from "../context/SocketContext";

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
  } = useSocket();

  const scrollContainerRef = useRef(null);
  const bottomRef = useRef(null);
  const [isAtBottom, setIsAtBottom] = useState(true);

  const [didUserScroll, setDidUserScroll] = useState(false);

  const [localMessages, setLocalMessages] = useState([]);

const debounceTimeout = useRef(null);

  useEffect(() => {
    console.log("bulk: ", bulkMessages);
    if (bulkMessages?.length > 0) {
      setMessages([...bulkMessages, ...localMessages]);
      const container = scrollContainerRef.current;
      const prevScrollHeight = container.scrollHeight;
      setTimeout(() => {
        const newScrollHeight = container.scrollHeight;
        container.scrollTop =
          newScrollHeight - prevScrollHeight + container.scrollTop;
      }, 0);

      // ریست مجدد وضعیت تا دوباره اجازه ارسال داشته باشیم
      setSendStatus(true);
    }
  }, [bulkMessages]);

  useEffect(() => {
    if (newMessage.date) {
      const date = new Date(newMessage.date);
      const originalTime = date.toLocaleTimeString();
      const [time, period] = originalTime.split(" ");
      const shortTime = time.slice(0, 4) + " " + period;
      console.log(shortTime);
      setMessages((prev) => [
        ...prev,
        {
          content: newMessage.content,
          time: shortTime,
          username: newMessage.username,
        },
      ]);
    }
  }, [newMessage]);

  useEffect(() => {
    if (isAtBottom) {
      bottomRef.current?.scrollIntoView({ behavior: "smooth" });
    }
  }, [messages]);

  const handleScroll = () => {
    const container = scrollContainerRef.current;
    if (!container) return;

    const atBottom =
      container.scrollHeight - container.scrollTop - container.clientHeight <
      50;
    setIsAtBottom(atBottom);

    // فقط وقتی کاربر واقعاً اسکرول کرد
      if (!didUserScroll) setDidUserScroll(true);
    if (!sendStatus) return;
    // رسیدن به بالا
    if (didUserScroll && container.scrollTop < 200) {
      setSendStatus(false);
      
      console.log("🟡 کاربر دستی به بالای لیست رسید");
      if (debounceTimeout.current) return;

    debounceTimeout.current = setTimeout(() => {
      debounceTimeout.current = null;
    }, 1000); // تا ۱ ثانیه بعد دیگه اجازه اجرا نمی‌ده

      const firstMessageTimecode = messages[0].timecode;

      const prevMessagesPayload = {
        type: "RequestBulkMessage",
        date: firstMessageTimecode - 1,
      };
      socket.send(JSON.stringify(prevMessagesPayload));
    }
  };

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

    console.log(bulkMessages);
    if (socket && socket.readyState == WebSocket.OPEN) {
      const messagePayload = {
        type: "SendMessage",
        content: text,
      };
      socket.send(JSON.stringify(messagePayload));
    }
  };

  return (
    <div className="h-screen flex flex-col text-gray-300">
      <div
        className="flex-1 overflow-y-auto px-4 py-4 space-y-3 mb-20"
        ref={scrollContainerRef}
        onScroll={handleScroll}
      >
        {messages.map((msg, i) => (
          <div key={i} className="flex">
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
              <p className="mr-10 text-sm">{msg.content}</p>
            </div>
          </div>
        ))}
        <div ref={bottomRef} />
      </div>

      <div className="fixed bottom-0 left-0 right-0 px-3 pb-5.5">
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
          className="w-full h-14 pl-4 pb-1 bg-neutral-700 border border-black rounded-lg"
        />
      </div>
    </div>
  );
};

export default Chat;
