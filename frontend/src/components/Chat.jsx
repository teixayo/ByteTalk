import { useState, useEffect, useRef } from "react";
import { useSocket } from "../context/SocketContext";
import linkifyHtml from "linkify-html";
import DOMPurify from "dompurify";
import TextareaAutosize from "react-textarea-autosize";
import { VariableSizeList as List } from "react-window";
import { useNavigate } from "react-router-dom";

let flag = true;
let firstRender = true;
const convertMessage = (text) => {
  // تشخیص پیام‌های حاوی کد یا تگ‌های HTML
  const isCode =
    /[<>]/.test(text) ||
    text.includes("function") ||
    text.includes("script") ||
    text.includes("svg") ||
    text.includes("href=");

  if (isCode) {
    // پاک‌سازی کامل و نمایش به عنوان کد
    const sanitized = text
      .replace(/&/g, "&amp;")
      .replace(/</g, "&lt;")
      .replace(/>/g, "&gt;")
      .replace(/\n/g, "<br />");

    return `<div class="message-code">${DOMPurify.sanitize(sanitized, {
      ALLOWED_TAGS: ["br"],
      ALLOWED_ATTR: [],
    })}</div>`;
  }

  // پردازش معمولی برای پیام‌های عادی
  const linkified = linkifyHtml(text, {
    target: "_blank",
    rel: "noopener noreferrer",
    validate: {
      url: (value) =>
        value.startsWith("http://") || value.startsWith("https://"),
    },
  });

  return DOMPurify.sanitize(linkified.replace(/\n/g, "<br />"), {
    ALLOWED_TAGS: ["a", "br"],
    ALLOWED_ATTR: ["href", "target", "rel"],
  });
};

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
    // setLoginCheck,
    // loginCheck,
  } = useSocket();

  const [writing, setWriting] = useState(false);
  const [isAtBottom, setIsAtBottom] = useState(true);
  const [localMessages, setLocalMessages] = useState([]);
  const debounceTimeout = useRef(null);
  const [initialScrollDone, setInitialScrollDone] = useState(false);

  const inputRef = useRef(null);
  const [inputHeight, setInputHeight] = useState(70); // ارتفاع پیش‌فرض
  const [listHeight, setListHeight] = useState(
    window.innerHeight - inputHeight
  );

  const Navigate = useNavigate();
  const rowHeights = useRef({});
  const listRef = useRef();

  // const rowRefs = useRef([]);

  const [selectedUser, setSelectedUser] = useState(null);
  const popupRef = useRef(null);

  // بستن پاپ‌آپ وقتی بیرون از آن کلیک شود
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (popupRef.current && !popupRef.current.contains(event.target)) {
        setSelectedUser(null);
      }
    };

    document.addEventListener("mousedown", handleClickOutside);
    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, []);

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

      console.log(bulkMessages);
      console.log(localMessages);

      listRef.current.scrollToItem(bulkLength, "start");
      if (firstRender) {
        setTimeout(() => {
          listRef.current.scrollToItem(bulkMessages.length, "end");
        }, 100);
        setTimeout(() => {
          listRef.current.scrollToItem(bulkMessages.length, "end");
          console.log("log bede bebinam");

          firstRender = false;
        }, 130);

        setTimeout(() => {
          setInitialScrollDone(true);
        }, 200);
      }
    }
    // if (loginCheck) {
    //   setTimeout(() => {
    //     console.log("koskesh");
    //     listRef.current.scrollToItem(bulkMessages.length, "end");
    //     setLoginCheck(false);
    //   }, 200);
    // }
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

      setMessages((prev) => {
        const newMessages = [
          ...prev,
          {
            content: newMessage.content,
            time: shortTime,
            username: newMessage.username,
            timecode: timestamp, // اضافه کردن timecode
          },
        ];

        if (isAtBottom) {
          setTimeout(() => {
            if (listRef.current) {
              listRef.current.scrollToItem(newMessages.length + 1, "end");
            }
          }, 60);
          setTimeout(() => {
            if (listRef.current) {
              listRef.current.scrollToItem(newMessages.length + 1, "end");
            }
          }, 100);
        }
        // اسکرول پس از به‌روزرسانی state

        return newMessages;
      });
    }
  }, [newMessage]);

  useEffect(() => {
    if (isAtBottom && listRef.current && flag && bulkMessages.length > 0) {
      console.log(bulkMessages.length - 1);
      setTimeout(() => {
        // listRef.current.scrollToItem(bulkMessages.length - 1, "start");
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
      username: localStorage.getItem("username") || "anonymous",
      timecode: timestamp,
    };

    setMessages((prev) => {
      const newMessages = [...prev, msg];

      if (listRef.current) {
        setTimeout(() => {
          console.log("از من چه انتظاری داری؟", newMessages.length);
          listRef.current.scrollToItem(newMessages.length, "end");
        }, 60);
        setTimeout(() => {
          console.log("از من چه انتظاری داری؟", newMessages.length);
          listRef.current.scrollToItem(newMessages.length, "end");
        }, 100);
      }

      return newMessages;
    });

    if (socket && socket.readyState == WebSocket.OPEN) {
      const messagePayload = {
        type: "SendMessage",
        channel: "global",
        content: text,
      };
      socket.send(JSON.stringify(messagePayload));
    }
  };

  // تابع اندازه‌گیری ارتفاع
  const getRowHeight = (index) => {
    // اگر ارتفاع ذخیره شده وجود دارد، از آن استفاده کنید
    if (rowHeights.current[index]) {
      return rowHeights.current[index];
    }

    // ارتفاع پیش‌فرض بر اساس اینکه آیا آواتار نمایش داده می‌شود یا نه
    const isSameUserAsPrevious =
      index > 0 && messages[index - 1].username === messages[index].username;
    return isSameUserAsPrevious ? 50 : 70; // افزایش ارتفاع برای جا دادن زمان
  };

  // تابع برای تنظیم ارتفاع واقعی
  const setRowHeight = (index, height) => {
    if (rowHeights.current[index] !== height) {
      rowHeights.current[index] = height;
      listRef.current.resetAfterIndex(index);
    }
  };

  const Row = ({ index, style }) => {
    const msg = messages[index];
    const rowRef = useRef();
    const isSameUserAsPrevious =
      index > 0 && messages[index - 1].username === msg.username;
    const showAvatar = !isSameUserAsPrevious;

    useEffect(() => {
      if (rowRef.current) {
        const height = rowRef.current.getBoundingClientRect().height;
        setRowHeight(index, height);
      }
    }, [index, msg.content]);

    const handleUserClick = (e) => {
      e.stopPropagation();
      console.log("mealkfdasfa")
      setSelectedUser({
        username: msg.username,
        // میتوانید اطلاعات بیشتر کاربر را اینجا اضافه کنید
      });
    };

    return (
      <div style={style}>
        <div
          ref={rowRef}
          className={`flex p-2 ${isSameUserAsPrevious ? "pt-1" : ""}`}
          style={{ minHeight: showAvatar ? "75px" : "50px" }}
        >
          {/* آواتار (فقط برای اولین پیام) */}
          {showAvatar ? (
            <div className="flex-shrink-0 mr-2">
              <svg
                xmlns="http://www.w3.org/2000/svg"
                fill="none"
                viewBox="0 0 24 24"
                strokeWidth={0.75}
                stroke="currentColor"
                className="size-12" // کاهش سایز آواتار
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  d="M17.982 18.725A7.488 7.488 0 0 0 12 15.75a7.488 7.488 0 0 0-5.982 2.975m11.963 0a9 9 0 1 0-11.963 0m11.963 0A8.966 8.966 0 0 1 12 21a8.966 8.966 0 0 1-5.982-2.275M15 9.75a3 3 0 1 1-6 0 3 3 0 0 1 6 0Z"
                />
              </svg>
            </div>
          ) : (
            <div className="flex-shrink-0 w-12 mr-2"></div> // فضای خالی همتراز با آواتار
          )}

          <div className="flex-1 min-w-0">
            {/* نام کاربر (فقط برای اولین پیام) */}
            {showAvatar && (
              <div className="flex items-center mb-1">
                <span
                  className="font-medium text-sm text-green-400 mr-2 cursor-pointer"
                  onClick={handleUserClick}
                >
                  {msg.username}
                </span>
              </div>
            )}

            {/* متن پیام و زمان در یک خط */}
            <div className="flex items-baseline group">
              <p
                className="break-words whitespace-pre-wrap inline-block max-w-[85%]"
                dangerouslySetInnerHTML={{
                  __html: convertMessage(msg.content).replace(/\n/g, "<br />"),
                }}
              ></p>
              {/* opacity-0 */}
              <span className="text-xs text-gray-400 ml-2  group-hover:opacity-100 transition-opacity">
                {msg.time}
              </span>
            </div>
          </div>
        </div>
      </div>
    );
  };

  const handleScroll = ({ scrollOffset, scrollUpdateWasRequested }) => {
    setTimeout(() => {
      if (scrollUpdateWasRequested) return;

      const list = listRef.current;
      const totalHeight = messages.reduce(
        (sum, _, i) => sum + getRowHeight(i),
        0
      );
      const distanceFromBottom =
        totalHeight - (scrollOffset + list.props.height);

      setIsAtBottom(distanceFromBottom < 50);
      console.log(isAtBottom);
    }, 100);
  };
  return (
    <>
      {selectedUser ? (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div
            ref={popupRef}
            className="bg-gray-800 rounded-lg p-6 max-w-sm w-full mx-4 border border-gray-700"
          >
            <div className="flex items-center mb-4">
              <div className="mr-4">
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  fill="none"
                  viewBox="0 0 24 24"
                  strokeWidth={1.5}
                  stroke="currentColor"
                  className="size-12"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    d="M17.982 18.725A7.488 7.488 0 0 0 12 15.75a7.488 7.488 0 0 0-5.982 2.975m11.963 0a9 9 0 1 0-11.963 0m11.963 0A8.966 8.966 0 0 1 12 21a8.966 8.966 0 0 1-5.982-2.275M15 9.75a3 3 0 1 1-6 0 3 3 0 0 1 6 0Z"
                  />
                </svg>
              </div>
              <div>
                <h3 className="text-xl font-bold text-white">
                  {selectedUser.username}
                </h3>
                <p className="text-gray-400">عضویت از ۱۴۰۲</p>
              </div>
            </div>

            <div className="mt-4">
              <button className="bg-blue-600 hover:bg-blue-700 text-white py-2 px-4 rounded-lg w-full">
                ارسال پیام خصوصی
              </button>
            </div>
          </div>
        </div>
      ) : (
        <div className="h-screen flex flex-col text-gray-300">
          <div className="flex-1 ">
            <List
              ref={listRef}
              height={listHeight}
              itemCount={messages.length}
              onScroll={handleScroll}
              itemSize={getRowHeight} // استفاده از تابع اندازه‌گیری پویا
              width={"100%"}
              estimatedItemSize={120} // ارتفاع تخمینی برای محاسبه اولیه
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
      )}
    </>
  );
};

export default Chat;
