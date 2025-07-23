import { useState, useEffect, useRef } from "react";
import { useSocket } from "../context/SocketContext";
import linkifyHtml from "linkify-html";
import DOMPurify from "dompurify";
import TextareaAutosize from "react-textarea-autosize";
import { VariableSizeList as List } from "react-window";
import { useNavigate, useParams } from "react-router-dom";
import toast from "react-hot-toast";
import Sidebar from "./Sidebar";

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

let flag2 = false;
const PrivetChat = () => {
  const [text, setText] = useState("");
  const {
    socket,
    bulkMessages,
    newMessage,
    sendStatus,
    setSendStatus,
    bulkLength,
    // setLoginCheck,
    // loginCheck,
    activeChat,
    setPrivetChannels,
    canMessage,
  } = useSocket();

  const [writing, setWriting] = useState(false);
  const [isAtBottom, setIsAtBottom] = useState(true);
  const [localMessages, setLocalMessages] = useState([]);
  const debounceTimeout = useRef(null);
  const [initialScrollDone, setInitialScrollDone] = useState(false);
  const [messages, setMessages] = useState([]);

  const inputRef = useRef(null);
  const [inputHeight, setInputHeight] = useState(58);
  const [titleHeight, setTitleHight] = useState(64); // ارتفاع پیش‌فرض
  const [listHeight, setListHeight] = useState(
    window.innerHeight - titleHeight - titleHeight
  );
  const rowHeights = useRef({});
  const listRef = useRef();

  // const rowRefs = useRef([]);
  const navigate = useNavigate();
  const { userID } = useParams();

  const [selectedUser, setSelectedUser] = useState(null);
  const popupRef = useRef(null);

  const [validUser, setValidUser] = useState(true);
  const [isFocused, setIsFocused] = useState(false);

  const [isMobileSidebar, setIsMobileSidebar] = useState(true);
  const [isSidebarOpen, setIsSidebarOpen] = useState(false);
  const [haveOpacity ,setHaveOpacity ] = useState(false)

  // بستن پاپ‌آپ وقتی بیرون از آن کلیک شود
  useEffect(() => {
    if (socket.readyState == WebSocket.OPEN) {
      socket.send(
        JSON.stringify({
          type: "RequestBulkMessage",
          date: -1,
          channel: userID,
        })
      );

      socket.send(
        JSON.stringify({
          type: "CanSendMessage",
          channel: userID,
        })
      );
    }

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
    console.log(canMessage);
    if (!canMessage) return;
    if (canMessage.status) {
      setValidUser(true);
    } else {
      setValidUser(false);
    }
  }, [canMessage]);

  useEffect(() => {
    console.log("active chat: ", activeChat);
  }, [activeChat]);

  useEffect(() => {
    const handleResize = () => {
      setListHeight(window.innerHeight - inputHeight - titleHeight);
      console.log(window.innerHeight - inputHeight - titleHeight);
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

  useEffect(() => {
    console.log("bulk", bulkMessages.length);
    if (flag2) {
      if (bulkMessages?.length > 0 && listRef.current) {
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

            firstRender = false;
          }, 130);

          setTimeout(() => {
            setInitialScrollDone(true);
          }, 200);
        }
      }
    } else {
      flag2 = true;
      setMessages([]);
    }

    // if (loginCheck) {
    //   setTimeout(() => {
    //     listRef.current.scrollToItem(bulkMessages.length, "end");
    //     setLoginCheck(false);
    //   }, 200);
    // }
  }, [bulkMessages]);

  useEffect(() => {
    if (newMessage.date) {
      if (
        newMessage.channel == localStorage.getItem("username") ||
        newMessage.channel == userID
      ) {
        const timestamp = Date.now();

        const date = new Date(newMessage.date);
        const shortTime = date.toLocaleTimeString("en-US", {
          hour: "2-digit",
          minute: "2-digit",
          hour12: true,
        });

        const msg = {
          content: newMessage.content,
          time: shortTime,
          username: newMessage.username,
          timecode: timestamp, // اضافه کردن timecode
        };

        setMessages((prev) => {
          const newMessages = [...prev, msg];

          if (listRef.current) {
            setTimeout(() => {
              listRef.current.scrollToItem(newMessages.length, "end");
            }, 60);
            setTimeout(() => {
              listRef.current.scrollToItem(newMessages.length, "end");
            }, 100);
          }
          // اسکرول پس از به‌روزرسانی state
          return newMessages;
        });

        setLocalMessages((prev) => {
          return [...prev, msg];
        });
      } else {
        const username = localStorage.getItem("username");
        if (newMessage.channel == "global" || newMessage.channel == username)
          return;
        console.log(newMessage);
        setPrivetChannels((prev) => {
          const prevChannels = prev;
          console.log(prevChannels);
          const isRepetitive = prevChannels.find((item) => {
            return item.name == newMessage.channel;
          });
          if (!isRepetitive) {
            return [{ name: newMessage.channel }, ...prev];
          } else {
            return [...prev];
          }
        });
      }
    }
  }, [newMessage]);

  useEffect(() => {
    if (isAtBottom && listRef.current && flag && bulkMessages.length > 0) {
      setTimeout(() => {
        // listRef.current.scrollToItem(bulkMessages.length - 1, "start");
        flag = false;
      }, 100);
    }
  }, [messages]);

  const sendMessage = () => {
    // const timestamp = Date.now();
    // const date = new Date(timestamp);
    // const shortTime = date.toLocaleTimeString("en-US", {
    //   hour: "2-digit",
    //   minute: "2-digit",
    //   hour12: true,
    // });

    // const msg = {
    //   content: text,
    //   time: shortTime,
    //   username: localStorage.getItem("username") || "anonymous",
    //   timecode: timestamp,
    // };

    // setMessages((prev) => {
    //   const newMessages = [...prev, msg];

    //   if (listRef.current) {
    //     setTimeout(() => {
    //       listRef.current.scrollToItem(newMessages.length, "end");
    //     }, 60);
    //     setTimeout(() => {
    //       listRef.current.scrollToItem(newMessages.length, "end");
    //     }, 100);
    //   }

    //   return newMessages;
    // });

    // setLocalMessages((prev) => [...prev, msg]);

    if (socket && socket.readyState == WebSocket.OPEN) {
      const messagePayload = {
        type: "SendMessage",
        channel: userID,
        content: text,
      };
      console.log("messagePayload", messagePayload);
      socket.send(JSON.stringify(messagePayload));
    }
    if (!messages[0]) {
      setPrivetChannels((prev) => [{ name: userID }, ...prev]);
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
      console.log("mealkfdasfa");
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
          style={{ minHeight: showAvatar ? "70px" : "45px" }}
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
                className="size-11" // کاهش سایز آواتار
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  d="M17.982 18.725A7.488 7.488 0 0 0 12 15.75a7.488 7.488 0 0 0-5.982 2.975m11.963 0a9 9 0 1 0-11.963 0m11.963 0A8.966 8.966 0 0 1 12 21a8.966 8.966 0 0 1-5.982-2.275M15 9.75a3 3 0 1 1-6 0 3 3 0 0 1 6 0Z"
                />
              </svg>
            </div>
          ) : (
            <div className="flex-shrink-0 w-12 mr-1"></div> // فضای خالی همتراز با آواتار
          )}

          <div className="flex-1 min-w-0">
            {/* نام کاربر (فقط برای اولین پیام) */}
            {showAvatar && (
              <div className="flex items-center mb-1">
                <span
                  className="font-medium text-sm mr-2 cursor-pointer"
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

  if (!validUser) {
    console.log(validUser);

    navigate(-1);
    return null;
  }

  return (
    <>
      <div className="h-screen grid grid-cols-7 xl:grid-cols-5 text-gray-300">
        <Sidebar
          isMobileSidebar={isMobileSidebar}
          setIsMobileSidebar={setIsMobileSidebar}
          isSidebarOpen={isSidebarOpen}
          setIsSidebarOpen={setIsSidebarOpen}
          setHaveOpacity={setHaveOpacity}
        />
        <div className={`${haveOpacity? "opacity-50" : null} grid col-span-7 sm:col-span-5 xl:col-span-4`}>
          <div className="h-14 flex justify-center sm:justify-start items-center bg-[#1a1a1e] border-b border-[#29292d]">
            <button
              className="absolute left-3.5 cursor-pointer sm:hidden"
              onClick={() => {
                setIsMobileSidebar(true);
                setIsSidebarOpen(true);
                setHaveOpacity(true);
                console.log(isMobileSidebar);
                console.log(isSidebarOpen);
              }}
            >
              <svg
                xmlns="http://www.w3.org/2000/svg"
                fill="none"
                viewBox="0 0 24 24"
                strokeWidth={1.25}
                stroke="currentColor"
                className="size-8"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  d="M3.75 6.75h16.5M3.75 12h16.5m-16.5 5.25h16.5"
                />
              </svg>
            </button>
            {userID ? (
              <svg
                xmlns="http://www.w3.org/2000/svg"
                fill="none"
                viewBox="0 0 24 24"
                strokeWidth={0.5}
                stroke="currentColor"
                className="size-11 text-white ml-2 mr-2.5"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  d="M17.982 18.725A7.488 7.488 0 0 0 12 15.75a7.488 7.488 0 0 0-5.982 2.975m11.963 0a9 9 0 1 0-11.963 0m11.963 0A8.966 8.966 0 0 1 12 21a8.966 8.966 0 0 1-5.982-2.275M15 9.75a3 3 0 1 1-6 0 3 3 0 0 1 6 0Z"
                />
              </svg>
            ) : (
              <svg
                xmlns="http://www.w3.org/2000/svg"
                fill="none"
                viewBox="0 0 24 24"
                strokeWidth={1.25}
                stroke="currentColor"
                className="size-8"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  d="M18 18.72a9.094 9.094 0 0 0 3.741-.479 3 3 0 0 0-4.682-2.72m.94 3.198.001.031c0 .225-.012.447-.037.666A11.944 11.944 0 0 1 12 21c-2.17 0-4.207-.576-5.963-1.584A6.062 6.062 0 0 1 6 18.719m12 0a5.971 5.971 0 0 0-.941-3.197m0 0A5.995 5.995 0 0 0 12 12.75a5.995 5.995 0 0 0-5.058 2.772m0 0a3 3 0 0 0-4.681 2.72 8.986 8.986 0 0 0 3.74.477m.94-3.197a5.971 5.971 0 0 0-.94 3.197M15 6.75a3 3 0 1 1-6 0 3 3 0 0 1 6 0Zm6 3a2.25 2.25 0 1 1-4.5 0 2.25 2.25 0 0 1 4.5 0Zm-13.5 0a2.25 2.25 0 1 1-4.5 0 2.25 2.25 0 0 1 4.5 0Z"
                />
              </svg>
            )}

            <p>{userID}</p>
          </div>
          <div className="flex-1 ">
            <List
              ref={listRef}
              height={listHeight}
              itemCount={messages.length}
              onScroll={handleScroll}
              itemSize={getRowHeight} // استفاده از تابع اندازه‌گیری پویا
              width={"100%"}
              className="scrollbar-custom"
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

                  const firstMessageTimecode =
                    messages[0]?.timecode || Date.now();
                  if (firstMessageTimecode) {
                    socket.send(
                      JSON.stringify({
                        type: "RequestBulkMessage",
                        date: firstMessageTimecode - 1,
                        channel: userID,
                      })
                    );
                  }
                }
              }}
            >
              {Row}
            </List>
          </div>
          <div className="bg-[#1a1a1e] w-full pb-1 sm:pb-2 px-1 md:px-2">
            <div
              ref={inputRef}
              className={`w-full flex transition-colors duration-200 ${
                isFocused
                  ? "border border-[#303135]"
                  : "border border-transparent"
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
                      toast.error(
                        "The message should not exceed 2000 characters."
                      );
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
                      toast.error(
                        "The message should not exceed 2000 characters."
                      );
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
        </div>
      </div>
    </>
  );
};

export default PrivetChat;
