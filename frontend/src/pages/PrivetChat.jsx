import { useState, useEffect, useRef } from "react";
import { useSocket } from "../context/SocketContext";
import { useParams, useLocation } from "react-router-dom";
import { VariableSizeList as List } from "react-window";

import linkifyHtml from "linkify-html";
import DOMPurify from "dompurify";

import Sidebar from "../components/Sidebar";
import MessageInput from "../components/MessageInput";

const convertMessage = (text) => {
  // Detect messages containing HTML code or tags
  const isCode =
    /[<>]/.test(text) ||
    text.includes("function") ||
    text.includes("script") ||
    text.includes("svg") ||
    text.includes("href=");

  if (isCode) {
    // Complete cleanup and display as code
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

  // Normal processing for normal messages
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

const PrivetChat = ({ setIsLoading, setFadeOut, setSelectedUser }) => {
  const [text, setText] = useState("");
  const {
    socket,
    bulkMessages,
    newMessage,
    setNewMessage,
    sendStatus,
    setSendStatus,
    bulkLength,
    setPrivetChannels,
    isFirstBulk,
    initialScrollDone,
    setInitialScrollDone,
    localPvMessages,
    setLocalPvMessages,
  } = useSocket();

  const [isAtBottom, setIsAtBottom] = useState(true);
  const debounceTimeout = useRef(null);
  const [messages, setMessages] = useState([]);

  const [inputHeight, setInputHeight] = useState(58);
  const [titleHeight, setTitleHight] = useState(68); // Default height
  const [listHeight, setListHeight] = useState(
    window.innerHeight - titleHeight - titleHeight
  );
  const rowHeights = useRef({});
  const listRef = useRef();

  const { userID } = useParams();

  const location = useLocation();

  const [isMobileSidebar, setIsMobileSidebar] = useState(true);
  const [isSidebarOpen, setIsSidebarOpen] = useState(false);
  const [haveOpacity, setHaveOpacity] = useState(false);

  const [flag2, setFlag2] = useState(false);
  const [numOfMsg, setNumOfMsg] = useState(0);

  const [isBulkMsg, setIsBulkMsg] = useState(true);

  const [validMessage, setValidMessage] = useState(false);

  const [newMessagesLength, setNewMessagesLenngth] = useState(0);

  useEffect(() => {
    setInitialScrollDone(false);
    setLocalPvMessages([]);
  }, [location]);

  useEffect(() => {
    if (numOfMsg > 0) {
      if (isBulkMsg) {
        if (isFirstBulk) {
          setTimeout(() => {
            listRef.current.scrollToItem(bulkMessages.length, "end");
          }, 400);
          // setTimeout(() => {
          //   listRef.current.scrollToItem(bulkMessages.length, "end");
          //   firstRender = false;
          // }, 130);
          setTimeout(() => {
            setInitialScrollDone(true);
          }, 800);
        } else {
          listRef.current.scrollToItem(bulkLength - 1, "start");
        }
      }
    }
  }, [numOfMsg]);

  useEffect(() => {
    if (socket.readyState == WebSocket.OPEN) {
      socket.send(
        JSON.stringify({
          type: "RequestBulkMessage",
          date: -1,
          channel: userID,
        })
      );
    }
  }, [userID]);

  useEffect(() => {
    setLocalPvMessages([]);
  }, [location]);

  useEffect(() => {
    if (flag2) {
      if (bulkMessages?.length > 0 && listRef.current) {
        setIsBulkMsg(true);
        setMessages([...bulkMessages, ...localPvMessages]);
      }
    } else {
      setFlag2(true);
      setMessages([]);
    }
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
          timecode: timestamp,
        };

        setIsBulkMsg(false);

        if (newMessage.username == localStorage.getItem("username")) {
          setNewMessagesLenngth(messages.length + 1);
          setMessages((prev) => {
            const newMessages = [...prev, msg];
            return newMessages;
          });
        } else {
          setNewMessagesLenngth(messages.length + 1);
          setMessages((prev) => {
            const newMessages = [...prev, msg];
            return newMessages;
          });
        }

        if (validMessage) {
          setLocalPvMessages((prev) => {
            return [...prev, msg];
          });
        }

        setNewMessage([]);
      } else {
        const username = localStorage.getItem("username");
        if (newMessage.channel == "global" || newMessage.channel == username)
          return;
        // setPrivetChannels((prev) => {
        //   const prevChannels = prev;
        //   const isRepetitive = prevChannels.find((item) => {
        //     return item.name == newMessage.channel;
        //   });
        //   if (!isRepetitive) {
        //     return [{ name: newMessage.channel }, ...prev];
        //   } else {
        //     return [...prev];
        //   }
        // });

        setPrivetChannels((prev) => {
          const prevChannels = prev.filter(
            (channel) => channel.name !== newMessage.channel
          );

          return [{ name: newMessage.channel }, ...prevChannels];
        });
      }
    }
  }, [newMessage]);

  useEffect(() => {
    setNumOfMsg(messages.length);

    setValidMessage(true);
    if (newMessagesLength > 0) {
      if (newMessage.username == localStorage.getItem("username")) {
        listRef.current.scrollToItem(newMessagesLength, "end");
      } else {
        if (isAtBottom) {
          listRef.current.scrollToItem(newMessagesLength, "end");
        }
      }
    }
  }, [messages]);

  // Height measurement function
  const getRowHeight = (index) => {
    // If there is a saved height, use it.
    if (rowHeights.current[index]) {
      return rowHeights.current[index];
    }

    // Default height based on whether the avatar is displayed or not
    const isSameUserAsPrevious =
      index > 0 && messages[index - 1].username === messages[index].username;
    return isSameUserAsPrevious ? 50 : 70; // Increasing altitude to accommodate time
  };

  // Function to set the actual height
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

    return (
      <div style={style}>
        <div
          ref={rowRef}
          className={`flex p-2 ${isSameUserAsPrevious ? "pt-1" : ""}`}
          style={{ minHeight: showAvatar ? "70px" : "45px" }}
        >
          {/* Avatar (only for the first message) */}
          {showAvatar ? (
            <div className="flex-shrink-0 mr-2">
              <svg
                xmlns="http://www.w3.org/2000/svg"
                fill="none"
                viewBox="0 0 24 24"
                strokeWidth={0.75}
                stroke="currentColor"
                className="size-11"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  d="M17.982 18.725A7.488 7.488 0 0 0 12 15.75a7.488 7.488 0 0 0-5.982 2.975m11.963 0a9 9 0 1 0-11.963 0m11.963 0A8.966 8.966 0 0 1 12 21a8.966 8.966 0 0 1-5.982-2.275M15 9.75a3 3 0 1 1-6 0 3 3 0 0 1 6 0Z"
                />
              </svg>
            </div>
          ) : (
            <div className="flex-shrink-0 w-12 mr-1"></div> // Empty space on par with avatar
          )}

          <div className="flex-1 min-w-0">
            {/* Username (for first message only) */}
            {showAvatar && (
              <div className="flex items-center mb-1">
                <span className="font-medium text-sm mr-2 cursor-pointer">
                  {msg.username}
                </span>
              </div>
            )}

            {/* Message text and time on one line */}
            <div className="flex items-baseline group">
              <p
              dir="auto"
                className="select-text break-words whitespace-pre-wrap inline-block max-w-[85%]"
                dangerouslySetInnerHTML={{
                  __html: convertMessage(msg.content).replace(/\n/g, "<br />"),
                }}
              ></p>
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
    }, 100);
  };

  return (
    <>
      <div className="h-screen grid grid-cols-7 xl:grid-cols-5 text-gray-300">
        <Sidebar
          isMobileSidebar={isMobileSidebar}
          setIsMobileSidebar={setIsMobileSidebar}
          isSidebarOpen={isSidebarOpen}
          setIsSidebarOpen={setIsSidebarOpen}
          setHaveOpacity={setHaveOpacity}
          setSelectedUser={setSelectedUser}
          setIsLoading={setIsLoading}
          setFadeOut={setFadeOut}
        />
        <div
          className={`${
            haveOpacity ? "opacity-50" : null
          } grid col-span-7 sm:col-span-5 xl:col-span-4`}
        >
          <div className="h-14 flex justify-center sm:justify-start items-center bg-[#1a1a1e] border-b border-[#29292d]">
            <button
              className="absolute left-3.5 cursor-pointer sm:hidden"
              onClick={() => {
                setIsMobileSidebar(true);
                setIsSidebarOpen(true);
                setHaveOpacity(true);
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
          <div className="flex-1">
            <List
              ref={listRef}
              height={listHeight}
              itemCount={messages.length}
              onScroll={handleScroll}
              itemSize={getRowHeight} // Using the dynamic measurement function
              width={"100%"}
              className="chat-scrollbar-custom"
              estimatedItemSize={120} // Estimated height for initial calculation
              onItemsRendered={({ visibleStartIndex }) => {
                if (
                  visibleStartIndex === 0 &&
                  sendStatus &&
                  !debounceTimeout.current &&
                  initialScrollDone
                ) {
                  setSendStatus(false);
                  // console.log("🟡The user has reached the top of the list.");

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
          <MessageInput
            setInputHeight={setInputHeight}
            inputHeight={inputHeight}
            setPrivetChannels={setPrivetChannels}
            setText={setText}
            text={text}
            messages={messages}
            setListHeight={setListHeight}
            titleHeight={titleHeight}
            listHeight={listHeight}
          />
        </div>
      </div>
    </>
  );
};

export default PrivetChat;
