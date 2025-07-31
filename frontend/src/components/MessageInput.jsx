import { useEffect, useState, useRef } from "react";
import { useSocket } from "../context/SocketContext";
import { useParams, useLocation } from "react-router-dom";

import EmojiPicker from "emoji-picker-react";

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
  listHeight
}) => {
  const [emojiBox, setEmojiBox] = useState(false);
  const [isFocused, setIsFocused] = useState(true);
  const [writing, setWriting] = useState(false);
  const [isRTL, setIsRTL] = useState(false);
  const [emojiPosition, setEmojiPosition] = useState({ top: 0 });

  const { socket, setPrivetChannels } = useSocket();
  const { userID } = useParams();

  const inputRef = useRef(null);
  const textareaRef = useRef(null);

  const location = useLocation();

  useEffect(() => {
    setEmojiBox(false);
  }, [location]);

  useEffect(() => {
    if (!emojiBox || !textareaRef.current) return;

    const rect = textareaRef.current.getBoundingClientRect();
    setEmojiPosition({
      top: rect.top - (listHeight - 4), // چون emoji picker height=600px + margin
    });
  }, [emojiBox, inputHeight]);

  useEffect(() => {
    const handleResize = () => {
      setListHeight(window.innerHeight - inputHeight - titleHeight);
    };
    window.addEventListener("resize", handleResize);
    return () => window.removeEventListener("resize", handleResize);
  }, [inputHeight]);

  useEffect(() => {
    const textarea = textareaRef.current;

    const handleBlur = () => {
      textarea?.focus();
    };

    textarea?.addEventListener("blur", handleBlur);
    textarea?.focus();

    return () => {
      textarea?.removeEventListener("blur", handleBlur);
    };
  }, []);

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

  const selectedEmoji = (e) => {
    setText((prev) => {
      return `${prev}${e.emoji}`;
    });
  };
  return (
    <div className="bg-[#1a1a1e] w-full pb-3 px-2">
      <div
        className={`fixed z-50 right-0 mr-2`}
        style={{
          top: emojiPosition.top,
          opacity: emojiBox ? 1 : 0,
          pointerEvents: emojiBox ? "auto" : "none",
        }}
      >
        <div
          className={`transform transition-all duration-300 ease-in-out 
    ${emojiBox ? "translate-x-0 opacity-100" : "translate-x-full opacity-0"}`}
        >
          <EmojiPicker
            theme="dark"
            onEmojiClick={(e) => {
              selectedEmoji(e);
              setWriting(true);
            }}
            autoFocusSearch={false}
            searchDisabled={true}
            open={true}
            emojiStyle="native"
            height={listHeight - 17}
          />
        </div>
      </div>
      <div
        ref={inputRef}
        className={`w-full flex transition-colors duration-200 ${
          isFocused ? "border border-[#303135]" : "border border-transparent"
        } rounded-lg`}
      >
        <div className="hidden sm:hidden-none sm:flex items-end pb-0.5 bg-[#222327] pl-4 rounded-l-lg">
          <svg
            xmlns="http://www.w3.org/2000/svg"
            fill="none"
            viewBox="0 0 24 24"
            strokeWidth={1}
            stroke="currentColor"
            className={`${
              emojiBox
                ? "text-blue-500 hover:text-white"
                : "hover:text-blue-500"
            } size-8 cursor-pointer transition-colors duration-200 mb-3`}
            onClick={() => setEmojiBox(!emojiBox)}
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              d="M15.182 15.182a4.5 4.5 0 0 1-6.364 0M21 12a9 9 0 1 1-18 0 9 9 0 0 1 18 0ZM9.75 9.75c0 .414-.168.75-.375.75S9 10.164 9 9.75 9.168 9 9.375 9s.375.336.375.75Zm-.375 0h.008v.015h-.008V9.75Zm5.625 0c0 .414-.168.75-.375.75s-.375-.336-.375-.75.168-.75.375-.75.375.336.375.75Zm-.375 0h.008v.015h-.008V9.75Z"
            />
          </svg>
        </div>

        <TextareaAutosize
          ref={textareaRef}
          type="text"
          dir={isRTL ? "rtl" : "ltr"}
          minRows={1}
          maxRows={4}
          value={text}
          onChange={(e) => {
            const value = e.target.value;
            setText(value);
            setWriting(value.trim() !== "");
            const firstChar = value.trim().charAt(0);
            setIsRTL(() => {
              return /[\u0600-\u06FF]/.test(firstChar) || false;
            }); // Persian or Arabic
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
                setIsRTL(false);
                setWriting(false);
                setText("");
              }
            }
          }}
          onHeightChange={handleInputResize}
          placeholder="Message"
          onFocus={() => setIsFocused(true)}
          className={`${
            writing ? "" : "rounded-r-lg"
          } w-full h-full pb-4.5 pt-4.5 pl-4 no-scrollbar bg-[#222327] focus:outline-none overflow-y-auto resize-none `}
        />
        {writing && (
          <div
            role="button"
            onClick={() => {
              if (text.length > 2000) {
                toast.error("The message should not exceed 2000 characters.");
                return;
              }
              if (text.trim() != "") {
                sendMessage();
                setIsRTL(false);
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
              className="size-7 mx-4 mb-3.25 cursor-pointer hover:text-blue-500 transition-colors duration-200"
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
