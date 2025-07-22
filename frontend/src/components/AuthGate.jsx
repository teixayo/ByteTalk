import { useEffect, useState } from "react";
import { useSocket } from "../context/SocketContext";
import { useNavigate, useLocation } from "react-router-dom";

const AuthGate = ({ children }) => {
  const { socket, wsReady, setWsReady, status } = useSocket();
  const navigate = useNavigate();
  const location = useLocation();

  const [checked, setChecked] = useState(false);
  const [loginAgain, setLoginAgain] = useState(false);

  useEffect(() => {
    if (!socket || loginAgain) return;

    const handleOpen = () => setWsReady(true);

    if (socket.readyState === WebSocket.OPEN) {

      function getCookie(name) {
        const value = `; ${document.cookie}`;
        const parts = value.split(`; ${name}=`);
        if (parts.length === 2) return parts.pop().split(";").shift();
      }
      const token = getCookie("token");

      if (token != undefined) {
        if (status.type == "Status") {
          if (status.code === "1002") {
            const returnPath = location.pathname.startsWith("/chat")
              ? location.pathname
              : "/chat";
            navigate(returnPath);
            setChecked(true);
          } else if (status.code === "1003") {
            setLoginAgain(true);
            setChecked(true);
            navigate("/")
          }
        }
      } else {
        setChecked(true);
        navigate("/");
      }

    } else {
      socket.addEventListener("open", handleOpen);
    }

    return () => {
      socket.removeEventListener("open", handleOpen);
    };
  }, [
    socket,
    wsReady,
    location.pathname,
    setWsReady,
    status,
  ]);

  if (!checked) return null;

  return children;
};

export default AuthGate;
