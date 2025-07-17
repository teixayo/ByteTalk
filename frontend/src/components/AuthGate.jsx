// src/AuthGate.jsx
import { useEffect, useState } from "react";
import { useSocket } from "../context/SocketContext";
import { useNavigate } from "react-router-dom";

const AuthGate = ({ children }) => {
  const { socket, wsReady, setWsReady } = useSocket();
  const navigate = useNavigate();

  const [checked, setChecked] = useState(false); // کنترل فقط یکبار بررسی
  const [loginAgain, setLoginAgain] = useState(false);

  useEffect(() => {
    if (!socket) return;
    if (loginAgain) return;
    console.log("ساکت اوکیه", socket);

    const handleOpen = () => setWsReady(true);

    console.log(socket.readyState);
    console.log(WebSocket.OPEN);

    if (socket.readyState === WebSocket.OPEN) {
      console.log("WsReady", wsReady);

      const username = localStorage.getItem("username");
      const token = localStorage.getItem("token");

      if (!username || !token) {
        navigate("/");
        setChecked(true);
        return;
      }

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
          setChecked(true);
        } 
        if (data.type == "Status" && data.code == "1003") {
          navigate("/"); // توکن معتبر بود
          setLoginAgain(true);
          setChecked(true);
        }

        socket.removeEventListener("message", handler);
      };

      socket.addEventListener("message", handler);
    } else {
      socket.addEventListener("open", handleOpen);
    }

    return () => socket.removeEventListener("open", handleOpen);
  }, [socket, wsReady]);

  // useEffect(() => {
  //   console.log(!wsReady, checked);
  //   if (!wsReady || checked) {
  //     return;
  //   }
  //   console.log("وارد شدیم");

  //   const username = localStorage.getItem("username");
  //   const token = localStorage.getItem("token");

  //   if (!username || !token) {
  //     navigate("/");
  //     setChecked(true);
  //     return;
  //   }

  //   // ارسال پکت لاگین با توکن برای اعتبارسنجی
  //   const loginTokenPayload = {
  //     type: "Login",
  //     username: username,
  //     token,
  //   };
  //   console.log("🔐 Sent loginTokenPayload:", loginTokenPayload);

  //   socket.send(JSON.stringify(loginTokenPayload));

  //   // گوش دادن به نتیجه (فقط یک بار)
  //   const handler = (event) => {
  //     const data = JSON.parse(event.data);
  //     console.log(data);
  //     if (data.type == "Status" && data.code == "1002") {
  //       navigate("/chat"); // توکن معتبر بود
  //       setChecked(true);
  //     } else {
  //       navigate("/"); // نامعتبر یا منقضی شده
  //     }
  //     if (data.type == "Status" && data.code == "1003") {
  //       navigate("/"); // توکن معتبر بود
  //       setChecked(true);
  //     }

  //     socket.removeEventListener("message", handler);
  //   };

  //   socket.addEventListener("message", handler);
  //   setWsReady(false);
  // }, [wsReady]);

  // تا وقتی وضعیت مشخص نشده چیزی نشون نده
  // if (!checked)
  //   return <p className="text-white text-center mt-10">Checking credit...</p>;

  return children;
};

export default AuthGate;