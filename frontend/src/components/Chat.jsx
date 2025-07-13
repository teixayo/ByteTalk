import { useState, useEffect, useRef } from "react";
import { useSocket } from "../context/SocketContext";
import { FixedSizeList as List } from "react-window";
import linkifyHtml from "linkify-html";
import DOMPurify from "dompurify";
import TextareaAutosize from "react-textarea-autosize";

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

  const [writing, setWriting] = useState(false);
  const [isAtBottom, setIsAtBottom] = useState(true);
  const [localMessages, setLocalMessages] = useState([]);
  const debounceTimeout = useRef(null);
  const listRef = useRef(null);
  const [initialScrollDone, setInitialScrollDone] = useState(false);

  const inputRef = useRef(null);
  const [inputHeight, setInputHeight] = useState(70); // ارتفاع پیش‌فرض
  const [listHeight, setListHeight] = useState(
    window.innerHeight - inputHeight
  );

  useEffect(() => {
    const handleResize = () => {
      setListHeight(window.innerHeight - inputHeight);
    };
    window.addEventListener("resize", handleResize);
    return () => window.removeEventListener("resize", handleResize);
  }, [inputHeight]);

  const handleInputResize = () => {
    if (inputRef.current) {
      const newHeight = inputRef.current.offsetHeight;
      setInputHeight(newHeight);
      setListHeight(window.innerHeight - newHeight);
    }
  };

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
      const timestamp = Date.now(newMessage.date);
      const date = new Date(timestamp);

      const shortTime = date.toLocaleTimeString("en-US", {
        hour: "2-digit",
        minute: "2-digit",
        hour12: true,
      });

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
    const timestamp = Date.now();
    const date = new Date(timestamp);

    const shortTime = date.toLocaleTimeString("en-US", {
      hour: "2-digit",
      minute: "2-digit",
      hour12: true,
    });

    const msg = {
      content: text,
      time: shortTime,
      username: localStorage.getItem("username"),
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
      <div style={style} className="flex p-2">
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
      <div className="flex-1 ">
        <List
          ref={listRef}
          height={listHeight}
          itemCount={messages.length}
          itemSize={75}
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

      <div className=" flex bg-neutral-700 w-full">
        <TextareaAutosize
          type="text"
          ref={inputRef}
          minRows={1}
          maxRows={4}
          value={text}
          onChange={(e) => {
            setText(e.target.value);
            e.target.value == "" || e.target.value.trim() == ""
              ? setWriting(false)
              : setWriting(true);
          }}
          onKeyDown={(e) => {
            if (e.key === "Enter" && !e.shiftKey) {
              e.preventDefault();
              if (text.trim() !== "") {
                sendMessage();
                setWriting(false);
                setText("");
              }
            }
          }}
          onHeightChange={handleInputResize}
          placeholder="Message"
          className="w-12/12 h-full pb-4.5 pt-4.5 pl-4 no-scrollbar bg-neutral-700 border-0 focus:outline-none overflow-y-auto  focus:ring-0 scrollbar-none resize-none"
        />
        {writing ? (
          <div
            role="button"
            onClick={() => {
              if (text.trim() !== "") {
                sendMessage();
                setWriting(false);
                setText("");
              }
            }}
            className="flex items-end cursor-pointer"
          >
            <svg
              xmlns="http://www.w3.org/2000/svg"
              fill="none"
              viewBox="0 0 24 24"
              strokeWidth={1.5}
              stroke="currentColor"
              className="size-7 mx-4 mb-3.25"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                d="M6 12 3.269 3.125A59.769 59.769 0 0 1 21.485 12 59.768 59.768 0 0 1 3.27 20.875L5.999 12Zm0 0h7.5"
              />
            </svg>
          </div>
        ) : null}
      </div>
    </div>
  );
};

export default Chat;
