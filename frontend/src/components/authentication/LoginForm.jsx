import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

import { Formik, Form, Field, ErrorMessage } from "formik";
import * as Yup from "yup";

import useAuth from "../../hooks/useAuth.jsx";
import { useSocket } from "../../context/SocketContext.jsx";

let localUserName;

const LoginForm = () => {
  const navigate = useNavigate();
  const [messages, setMessages] = useState([]);
  const { getToken } = useAuth();
  const { socket } = useSocket();

  useEffect(() => {
    if(!socket) {
      console.log('socket isnt ready')
      return
    }

    socket.onopen = () => {
      console.log("✅ WebSocket connected");
    };

    socket.onmessage = (event) => {
      const msg = event.data;
      const parsed = JSON.parse(msg);

      console.log(parsed.description);

      setMessages((prev) => [...prev, `Server: ${msg}`]);
    };

  }, [socket]);

  const handleSubmit = (event) => {
    const token = getToken();
    localUserName = event.fildname;

    if (socket && socket.readyState === WebSocket.OPEN) {
      const loginPayload = {
        type: "Login",
        name: localUserName,
        token: token,
      };

      console.log(loginPayload);
      socket.send(JSON.stringify(loginPayload));

    } else {
      console.log("⚠️ WebSocket هنوز وصل نشده. منتظر اتصال باش.");
    }

    setTimeout(() => {
      navigate("/chat");
    }, 3000);
  };

  const validationSchema = Yup.object({
    fildname: Yup.string().required("Username is required"),
  });
  return (
    <div className="flex justify-center itmes-center w-full">
      <Formik
        initialValues={{ fildname: "" }}
        validationSchema={validationSchema}
        onSubmit={handleSubmit}
      >
        <Form className="bg-white rounded-2xl w-6/12 h-45 flex justify-center items-center mt-10 pb-2">
          <div className="w-full">
            <div className="flex justify-center mt-4 ">
              <div className="w-10/12">
                <Field
                  name="fildname"
                  type="text"
                  placeholder="UserName"
                  className="w-full h-10 border border-gray-400 px-3 rounded-md"
                />
                <ErrorMessage
                  name="fildname"
                  component="div"
                  className="text-red-500 text-sm ml-0.5"
                />
              </div>
            </div>
            <div className="flex justify-center mt-4">
              <button
                type="submit"
                className="bg-blue-600 w-10/12 h-11 rounded-md text-white"
              >
                Login
              </button>
            </div>
          </div>
        </Form>
      </Formik>
      <ul className="mt-6 list-disc pl-5">
        {messages.map((msg, i) => (
          <li key={i} className="text-sm">
            {msg}
          </li>
        ))}
      </ul>
    </div>
  );
};

export default LoginForm;
