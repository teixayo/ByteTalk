import { useEffect, useState } from "react";
import { Formik, Form, Field, ErrorMessage } from "formik";
import * as Yup from "yup";
import useAuth from "../../hooks/useAuth";
import { useNavigate } from "react-router-dom";
import { useSocket } from "../../context/SocketContext";

let localUserName = "";

const SignUpForm = () => {
  const navigate = useNavigate();
  const [messages, setMessages] = useState([]);
  const { socket } = useSocket();
  const { getToken } = useAuth();

  useEffect(() => {
  if (!socket) return;

  socket.onmessage = (event) => {
    const data = JSON.parse(event.data);
    console.log("📨 Message received:", data);

    if (data.type === "GetToken") {
      localStorage.setItem("token", data.token);
      console.log("✅ Token saved:", data.token);

      const loginPayload = {
        type: "Login",
        name: localUserName,
        token: data.token,
      };

      console.log("📨 Sending login:", loginPayload);
      socket.send(JSON.stringify(loginPayload));
    }

    if (data.type === "SuccessLogin") {
      console.log("✅ Login successful");
    }

    if (data.type === "Error") {
      console.error("❌ Server Error:", data.description);
    }
  };
}, [socket]);


  const handleSubmit = (values) => {
  console.log("🚀 Form submitted", values);
  localUserName = values.fildname;

  if (!socket || socket.readyState !== WebSocket.OPEN) {
    console.warn("❌ WebSocket not ready");
    return;
  }

  const signupPayload = {
    type: "CreateUser",
    name: localUserName,
  };

  console.log("📨 Sending CreateUser", signupPayload);
  socket.send(JSON.stringify(signupPayload));
  setMessages([`You: Sent CreateUser for "${localUserName}"`]);
  setTimeout(() => {
    login()
  }, 1000)
};


  const login = () => {
    const token = getToken();
    const loginPayload = {
      type: "Login",
      name: localUserName,
      token: token,
    };

    console.log("📨 Sending login:", loginPayload);
    socket.send(JSON.stringify(loginPayload));

    setTimeout(() => {
      navigate("/chat");
    }, 2000);
  };

  const validationSchema = Yup.object({
    fildname: Yup.string().required("Username is required"),
  });

  return (
    <div className="flex justify-center items-center w-full">
      <Formik
        initialValues={{ fildname: "" }}
        validationSchema={validationSchema}
        onSubmit={handleSubmit}
      >
        <Form className="bg-white rounded-2xl w-6/12 h-50 flex justify-center items-center mt-10 pb-2">
          <div className="w-full">
            <div className="flex justify-center mt-4">
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
                  className="text-red-500 text-sm"
                />
              </div>
            </div>
            <div className="flex justify-center mt-4">
              <button
                type="submit"
                className="bg-blue-600 w-10/12 h-11 rounded-md text-white"
              >
                Sign in
              </button>
            </div>
            <p className="flex justify-center mt-1">
              Do you have an account?
              <a href="/login" className="ml-1 text-blue-600">
                Register
              </a>
            </p>
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

export default SignUpForm;
