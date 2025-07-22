import { useSocket } from "../context/SocketContext";
import { useEffect } from "react";
import { useNavigate, Link } from "react-router-dom";
const Sidebar = () => {
  const { privetChannels, setActiveChat, activeChat } = useSocket();
  const navigate = useNavigate();

  useEffect(() => {
    if (!privetChannels[0]) return;

    console.log(privetChannels[0]);
  }, [privetChannels]);

  useEffect(() => {
    const path = location.pathname.split("/")[2] || "chat";
    setActiveChat(path);
  }, []);

  return (
    <div className="grid col-span-2 xl:col-span-1">
      <div className="bg-[#121214] h-full p-4">
        <h2 className="text-lg font-semibold text-white mb-4">Private chats</h2>
        <div className="space-y-2">
          {/* لیست کاربران - بعداً تکمیل می‌شود */}
          <div
            className={`pl-2 h-[46px] opacity-50 flex items-center ${
              activeChat == "chat"
                ? "bg-[#2c2c30] opacity-100"
                : "hover:bg-[#1d1d1e] hover:opacity-100"
            } rounded cursor-pointer`}
            onClick={() => {
              setActiveChat("chat");
              navigate(`/chat`);
              // window.location.reload();
              navigate(0);
            }}
          >
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
            <p className="text-white pl-2">Global</p>
          </div>
          {privetChannels.map((channel, index) => {
            return (
              <div
                key={index}
                className={`pl-1.5 h-[46px] opacity-50 flex items-center ${
                  `/chat/${channel.name}` == location.pathname
                    ? "bg-[#2c2c30] opacity-100"
                    : "hover:bg-[#1d1d1e] hover:opacity-100"
                } rounded cursor-pointer`}
                onClick={() => {
                  setActiveChat(channel.name);
                  navigate(`/chat/${channel.name}`);
                  // window.location.reload();
                  navigate(0);
                }}
              >
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
                <p className={`text-white pl-1.5 mb-0.5`}>{channel.name}</p>
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
};

export default Sidebar;
