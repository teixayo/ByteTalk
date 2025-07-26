import { useEffect, useState, useRef } from "react";
import { useSocket } from "../context/SocketContext";
import { useParams } from "react-router-dom";

import toast from "react-hot-toast";
import TextareaAutosize from "react-textarea-autosize";

const MessageInput = ({
  setInputHeight,
  inputHeight,
  setText,
  text,
  messages,
  setListHeight,
  titleHeight,
}) => {
  const { socket, setPrivetChannels } = useSocket();
  const { userID } = useParams();

  const [isFocused, setIsFocused] = useState(false);
  const [writing, setWriting] = useState(false);
  const inputRef = useRef(null);

  useEffect(() => {
    const handleResize = () => {
      setListHeight(window.innerHeight - inputHeight - titleHeight);
    };
    window.addEventListener("resize", handleResize);
    return () => window.removeEventListener("resize", handleResize);
  }, [inputHeight]);

  const handleInputResize = () => {
    if (inputRef.current) {
      const newHeight = inputRef.current.offsetHeight;
      setInputHeight(newHeight);
      setListHeight(window.innerHeight - newHeight - titleHeight);
    }
  };

  const sendMessage = () => {
    if (socket && socket.readyState == WebSocket.OPEN) {
      const messagePayload = {
        type: "SendMessage",
        channel: userID || "global",
        content: text,
      };
      socket.send(JSON.stringify(messagePayload));
    }
    if (!messages[0]) {
      setPrivetChannels((prev) => [{ name: userID }, ...prev]);
    }
  };

  return (
    <div className="bg-[#1a1a1e] w-full pb-3 px-2">
      <div
        ref={inputRef}
        className={`w-full flex transition-colors duration-200 ${
          isFocused ? "border border-[#303135]" : "border border-transparent"
        } rounded-lg`}
      >
        <TextareaAutosize
          type="text"
          minRows={1}
          maxRows={4}
          value={text}
          onChange={(e) => {
            setText(e.target.value);
            setWriting(e.target.value.trim() !== "");
          }}
          onKeyDown={(e) => {
            if (e.key === "Enter" && !e.shiftKey) {
              e.preventDefault();
              if (text.length > 2000) {
                toast.error("The message should not exceed 2000 characters.");
                return;
              }
              if (text.trim() !== "") {
                sendMessage();
                setWriting(false);
                setText("");
              }
            }
          }}
          onHeightChange={handleInputResize}
          placeholder="Message"
          onFocus={() => setIsFocused(true)}
          onBlur={() => setIsFocused(false)}
          className={`${
            writing ? "rounded-l-lg" : "rounded-lg"
          } w-full h-full pb-4.5 pt-4.5 pl-4 no-scrollbar bg-[#222327] focus:outline-none overflow-y-auto resize-none`}
        />
        {writing && (
          <div
            role="button"
            onClick={() => {
              if (text.length > 2000) {
                toast.error("The message should not exceed 2000 characters.");
                return;
              }
              if (text.trim() !== "") {
                sendMessage();
                setWriting(false);
                setText("");
              }
            }}
            className="flex items-end pb-0.5 cursor-pointer rounded-r-lg bg-[#222327]"
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
        )}
      </div>
    </div>
  );
};

export default MessageInput;
