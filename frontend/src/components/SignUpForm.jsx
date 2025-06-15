import { useEffect, useRef, useState } from "react";
import { Formik, Form, Field, ErrorMessage } from "formik";
import * as Yup from "yup";
import useAuth from "../hooks/useAuth";

const SignUpForm = () => {
  const [messages, setMessages] = useState([]);
  const [username, setUsername] = useState("");
  const socketRef = useRef(null);
  const { getToken } = useAuth();

  useEffect(() => {
    socketRef.current = new WebSocket("ws://localhost:25565");

    socketRef.current.onopen = () => {
      console.log("✅ WebSocket connected");
    };

    socketRef.current.onmessage = (event) => {
      const msg = event.data;
      console.log(event);

      try {
        const parsed = JSON.parse(msg);
        setMessages((prev) => [...prev, `Server: ${msg}`]);

        localStorage.setItem("token", parsed.token);
        console.log("✅ Token saved:", parsed.token);
      } catch (error) {
        console.error("❌ Failed to parse WebSocket message:", error);
      }
    };

    socketRef.current.onerror = (error) => {
      console.error("WebSocket error:", error);
    };

    socketRef.current.onclose = () => {
      console.log("❌ WebSocket disconnected");
    };

    return () => {
      socketRef.current.close();
    };
  }, []);

  const handleSubmit = (event) => {
    if (socketRef.current && socketRef.current.readyState === WebSocket.OPEN) {
      const name = event.username;

      const signupPayload = {
        type: "CreateUser",
        name: name,
      };
      socketRef.current.send(JSON.stringify(signupPayload));

      login();
      setUsername(name);
      setMessages(() => [`You: Sent CreateUser for "${event.username}"`]);
    }
  };

  const login = () => {
    const token = getToken();
    const loginPayload = {
      type: "Login",
      name: "AdelNouri",
      token: token,
    };

    socketRef.current.send(JSON.stringify(loginPayload));
  };
  const validationSchema = Yup.object({
    username: Yup.string().required("Username is required"),
  });

  return (
    <div className="flex justify-center itmes-center w-full">
      <Formik
        initialValues={{ username: "" }}
        validationSchema={validationSchema}
        onSubmit={handleSubmit}
      >
        <Form className="bg-white rounded-2xl w-6/12 h-45 flex justify-center items-center mt-10 pb-2">
          <div className="w-full">
            <div className="flex justify-center mt-4 ">
              <div className="w-10/12">
                <Field
                  name="username"
                  type="text"
                  placeholder="User Name"
                  className="w-full h-10 border border-gray-400 px-3 rounded-md"
                />
                <ErrorMessage
                  name="username"
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
                Sign in
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

export default SignUpForm;
