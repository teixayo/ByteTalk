import { useSocket } from "../context/SocketContext";
import { useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import { FixedSizeList as List } from "react-window";

const Sidebar = ({
  isMobileSidebar,
  setIsMobileSidebar,
  isSidebarOpen,
  setIsSidebarOpen,
  setHaveOpacity,
  setSelectedUser,
  setIsLoading,
  setFadeOut,
}) => {
  const {
    privetChannels,
    setActiveChat,
    activeChat,
    setLocalGlobalMessages,
    setLocalPvMessages,
  } = useSocket();
  const navigate = useNavigate();
  const sidebarRef = useRef(null);
  const [listHeight, setListHeight] = useState(0);
  const [unreadChannels, setUnreadChannels] = useState([]);
  const [isGlobalUnread, setIsGlobalUnread] = useState(false);
  // const [isThisChannelUnread, setIsThisChannelUnread] = useState(false);

  useEffect(() => {
    const path = location.pathname.split("/")[2] || "chat";
    setActiveChat(path);

    const checkScreenSize = () => {
      if (window.innerWidth >= 640) {
        setIsMobileSidebar(false);
        setHaveOpacity(false);
      }
    };

    const updateListHeight = () => {
      if (sidebarRef.current) {
        // Calculating usable height for a list
        const headerHeight = 116; // High Waste (Title + Global chat item)
        // const paddingBottom = 16; // Bottom padding
        const availableHeight = sidebarRef.current.clientHeight - headerHeight;
        setListHeight(Math.max(availableHeight, 100));
      }
    };

    checkScreenSize();
    updateListHeight();

    setUnreadChannels(JSON.parse(sessionStorage.getItem("unreadChannels")));
    console.log(JSON.parse(sessionStorage.getItem("unreadChannels")));

    window.addEventListener("resize", () => {
      checkScreenSize();
      updateListHeight();
    });

    const handleUnreadUpdate = () => {
      const updated =
        JSON.parse(sessionStorage.getItem("unreadChannels")) || [];
      setUnreadChannels(updated);
    };

    window.addEventListener("unreadUpdated", handleUnreadUpdate);

    return () => {
      window.removeEventListener("resize", checkScreenSize);
      window.removeEventListener("resize", updateListHeight);
      window.removeEventListener("unreadUpdated", handleUnreadUpdate);
    };
  }, []);

  useEffect(() => {
    console.log(unreadChannels);
    if (!unreadChannels) {
      setIsGlobalUnread(false);
      return;
    }
    console.log(unreadChannels);
    setIsGlobalUnread(() => {
      const is = unreadChannels.find((channel) => {
        return channel == "global";
      });

      return is;
    });
  }, [unreadChannels]);

  // Common item renderer for both mobile and desktop
  const renderItem = ({ index, style }) => {
    if (!privetChannels[0]) return;
    const pv = privetChannels[index];

    const thisChannelUnread = unreadChannels
      ? unreadChannels.includes(pv.name)
      : null;

    return (
      <div key={index} style={style}>
        <div
          className={`pl-1.5 w-97/100 h-[44px] opacity-50 flex items-center justify-between ${
            `/chat/${pv.name}` == location.pathname
              ? "bg-[#2c2c30] opacity-100"
              : "hover:bg-[#1d1d1e] hover:opacity-100"
          } rounded cursor-pointer group`}
          onClick={() => {
            setFadeOut(false);
            setIsLoading(false);
            setLocalPvMessages([]);
            setActiveChat(pv.name);
            const stored = JSON.parse(sessionStorage.getItem("unreadChannels"));
            if (stored) {
              const updateUnreadChannels = stored.filter(
                (channel) => channel !== pv.name
              );
              sessionStorage.setItem(
                "unreadChannels",
                JSON.stringify(updateUnreadChannels)
              );
              setUnreadChannels(updateUnreadChannels);
            }
            navigate(`/chat/${pv.name}`);
            if (isMobileSidebar) {
              setIsSidebarOpen(false);
              setHaveOpacity(false);
            }
          }}
        >
          {/* آیکن */}
          <div className="flex-shrink-0">
            <svg
              xmlns="http://www.w3.org/2000/svg"
              fill="none"
              viewBox="0 0 24 24"
              strokeWidth={1}
              stroke="currentColor"
              className="size-8 text-white"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                d="M17.982 18.725A7.488 7.488 0 0 0 12 15.75a7.488 7.488 0 0 0-5.982 2.975m11.963 0a9 9 0 1 0-11.963 0m11.963 0A8.966 8.966 0 0 1 12 21a8.966 8.966 0 0 1-5.982-2.275M15 9.75a3 3 0 1 1-6 0 3 3 0 0 1 6 0Z"
              />
            </svg>
          </div>

          {/* اسم کاربر */}
          <div className="flex-1 pl-1.5 overflow-hidden">
            <p className="text-white truncate">{pv.name}</p>
          </div>

          {/* نقطه‌ی آبی */}
          <div className="w-8 flex justify-end pr-3">
            {thisChannelUnread && (
              <div className="w-4 h-4 bg-blue-500 rounded-full" />
            )}
          </div>
        </div>
      </div>
    );
  };

  const GlobalChatItem = () => (
    <div
      className={`pl-2 h-[44px] opacity-50 flex items-center  ${
        activeChat == "chat"
          ? "bg-[#2c2c30] opacity-100"
          : "hover:bg-[#1d1d1e] hover:opacity-100"
      } rounded cursor-pointer`}
      onClick={() => {
        setSelectedUser(null);
        setFadeOut(false);
        setIsLoading(false);
        setLocalGlobalMessages([]);
        setActiveChat("chat");
        navigate(`/chat`);
        if (JSON.parse(sessionStorage.getItem("unreadChannels"))) {
          const updateUnreadChannels = JSON.parse(
            sessionStorage.getItem("unreadChannels")
          ).filter((channel) => {
            return channel != "global";
          });
          sessionStorage.setItem(
            "unreadChannels",
            JSON.stringify(updateUnreadChannels)
          );
        }
        if (isMobileSidebar) {
          setIsSidebarOpen(false);
          setHaveOpacity(false);
        }
      }}
    >
      <div className="flex-shrink-0">
        <svg
          xmlns="http://www.w3.org/2000/svg"
          fill="none"
          viewBox="0 0 24 24"
          strokeWidth={1}
          stroke="currentColor"
          className="size-7 b-white"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            d="M18 18.72a9.094 9.094 0 0 0 3.741-.479 3 3 0 0 0-4.682-2.72m.94 3.198.001.031c0 .225-.012.447-.037.666A11.944 11.944 0 0 1 12 21c-2.17 0-4.207-.576-5.963-1.584A6.062 6.062 0 0 1 6 18.719m12 0a5.971 5.971 0 0 0-.941-3.197m0 0A5.995 5.995 0 0 0 12 12.75a5.995 5.995 0 0 0-5.058 2.772m0 0a3 3 0 0 0-4.681 2.72 8.986 8.986 0 0 0 3.74.477m.94-3.197a5.971 5.971 0 0 0-.94 3.197M15 6.75a3 3 0 1 1-6 0 3 3 0 0 1 6 0Zm6 3a2.25 2.25 0 1 1-4.5 0 2.25 2.25 0 0 1 4.5 0Zm-13.5 0a2.25 2.25 0 1 1-4.5 0 2.25 2.25 0 0 1 4.5 0Z"
          />
        </svg>
      </div>
      <p className="text-white pl-2">Global</p>
      {isGlobalUnread ? (
        <div className="w-full flex justify-end">
          <div className="w-4 h-4 mr-3 bg-blue-500 rounded-full"></div>
        </div>
      ) : null}
    </div>
  );

  const mobileSidebar = () => (
    <>
      {isSidebarOpen && isMobileSidebar && (
        <div
          className="fixed inset-0 z-40 sm:hidden"
          onClick={() => {
            setIsSidebarOpen(false);
            setHaveOpacity(false);
          }}
        ></div>
      )}

      <div
        className={`
        fixed top-0 bottom-0 left-0 z-50 transition-transform duration-300 ease-in-out  
        w-2/4 sm:static sm:w-auto sm:translate-x-0
        ${isSidebarOpen ? "translate-x-0" : "-translate-x-full"} 
      `}
        ref={sidebarRef}
      >
        <div className="grid col-span-2 xl:col-span-1 h-full">
          <div className="bg-[#121214] h-full px-4 pt-4 flex flex-col">
            <h2 className="text-lg font-semibold text-white mb-4">
              Private chats
            </h2>
            <div className="flex-1 overflow-hidden">
              <GlobalChatItem />

              <div className="bottom-0 left-0 w-96/100 h-[2px] bg-[#666771] mt-2"></div>

              <div className="flex-1 mt-2">
                <List
                  height={listHeight}
                  itemCount={privetChannels.length}
                  itemSize={46}
                  width="100%"
                  className="sidebar-scrollbar-custom"
                >
                  {renderItem}
                </List>
              </div>
            </div>
          </div>
        </div>
      </div>
    </>
  );

  const desktopSidebar = () => (
    <div
      className={`hidden sm:grid col-span-2 xl:col-span-1 h-full`}
      ref={sidebarRef}
    >
      <div className="bg-[#121214] h-full px-4 pt-4 flex flex-col">
        <h2 className="text-lg font-semibold text-white mb-4">Private chats</h2>
        <div className="flex-1 overflow-hidden">
          <GlobalChatItem />

          <div className="bottom-0 left-0 w-96/100 h-[2px] bg-[#666771] mt-2"></div>

          <div className="flex-1">
            <List
              height={listHeight}
              itemCount={privetChannels.length}
              itemSize={48}
              width="100%"
              className="sidebar-scrollbar-custom"
            >
              {renderItem}
            </List>
          </div>
        </div>
      </div>
    </div>
  );

  return isMobileSidebar ? mobileSidebar() : desktopSidebar();
};

export default Sidebar;
