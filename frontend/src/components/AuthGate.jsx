// src/AuthGate.jsx
import { useEffect, useState } from "react";
import { useSocket } from "../context/SocketContext";
import { useNavigate } from "react-router-dom";

const AuthGate = ({ children }) => {
  const { socket } = useSocket();
  const navigate = useNavigate();

  const [checked, setChecked] = useState(false); // کنترل فقط یکبار بررسی
  const [wsReady, setWsReady] = useState(false);

  useEffect(() => {
    if (!socket) return;

    const handleOpen = () => setWsReady(true);

    if (socket.readyState === WebSocket.OPEN) {
      setWsReady(true);
    } else {
      socket.addEventListener("open", handleOpen);
    }

    return () => socket.removeEventListener("open", handleOpen);
  }, [socket]);

  useEffect(() => {
    if (!wsReady || checked) {
    //   setChecked(true);
      return;
    }

    const username = localStorage.getItem("username");
    const token = localStorage.getItem("token");

    if (!username || !token) {
      navigate("/");
      setChecked(true)
      return;
    }

    // ارسال پکت لاگین با توکن برای اعتبارسنجی
    const loginTokenPayload = {
      type: "Login",
      username: username,
      token,
    };
    console.log("🔐 Sent loginTokenPayload:", loginTokenPayload);

    socket.send(JSON.stringify(loginTokenPayload));

    // گوش دادن به نتیجه (فقط یک بار)
    const handler = (event) => {
      const data = JSON.parse(event.data);
      console.log(data);
      if (data.type == "Status" && data.code == "1002") {
        navigate("/chat"); // توکن معتبر بود
      } else {
        navigate("/"); // نامعتبر یا منقضی شده
        setChecked(true)
      }
      setChecked(true);
      socket.removeEventListener("message", handler);
    };

    socket.addEventListener("message", handler);
  }, [wsReady]);

  // تا وقتی وضعیت مشخص نشده چیزی نشون نده
  if (!checked)
    return (
      <p className="text-white text-center mt-10">در حال بررسی اعتبار...</p>
    );

  return children;
};

export default AuthGate;
