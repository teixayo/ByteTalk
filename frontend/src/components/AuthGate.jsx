import { useEffect, useState } from "react";
import { useSocket } from "../context/SocketContext";
import { useNavigate, useLocation } from "react-router-dom";

const AuthGate = ({ children }) => {
  const { socket, wsReady, setWsReady } = useSocket();
  const navigate = useNavigate();
  const location = useLocation();

  const [checked, setChecked] = useState(false);
  const [loginAgain, setLoginAgain] = useState(false);

  useEffect(() => {
    if (!socket) return;
    if (loginAgain) return;

    const handleOpen = () => setWsReady(true);

    if (socket.readyState === WebSocket.OPEN) {
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

      socket.send(JSON.stringify(loginTokenPayload));

      const handler = (event) => {
        const data = JSON.parse(event.data);
        if (data.type == "Status" && data.code == "1002") {
          // حفظ مسیر قبلی یا رفتن به چت عمومی
          const returnPath = location.pathname.startsWith("/chat") 
            ? location.pathname 
            : "/chat";
          navigate(returnPath);
          setChecked(true);
        }
        if (data.type == "Status" && data.code == "1003") {
          navigate("/");
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
  }, [socket, wsReady, location.pathname]);

  if (!checked)
    return <p className="text-white text-center mt-10">Checking credit...</p>;

  return children;
};

export default AuthGate;