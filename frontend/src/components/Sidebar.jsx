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
            className={`p-2 opacity-50 ${
              activeChat == "chat" ? "bg-[#2c2c30] opacity-100" : "hover:bg-[#1d1d1e] hover:opacity-100"
            } rounded cursor-pointer`}
            onClick={() => {
              setActiveChat("chat");
              navigate(`/chat`);
              // window.location.reload();
              navigate(0);
            }}
          >
            <p className="text-white">Global</p>
          </div>
          {privetChannels.map((channel, index) => {
            return (
              <div
                key={index}
                className={`p-2 opacity-50 ${
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
                <p
                  className={`text-white `}
                >
                  {channel.name}
                </p>
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
};

export default Sidebar;
