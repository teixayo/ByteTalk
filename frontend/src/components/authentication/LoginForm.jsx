import { useEffect } from "react";
import { useNavigate, Link } from "react-router-dom";

import { Formik, Form, Field, ErrorMessage } from "formik";
import * as Yup from "yup";

import { useSocket } from "../../context/SocketContext.jsx";

let localUserName;
let localUserPassword;
const statusMessages = {
  1000: "✅ Success",
  1001: "❌ Incorrect username or password",
  1006: "❌ Invalid username format",
  1007: "❌ Invalid password format",
};

const LoginForm = () => {
  const navigate = useNavigate();
  const { socket, status } = useSocket();

  useEffect(() => {
    if (status.code == "1000") {
      alert(statusMessages[status.code]);
    } else if (status.code == "1001") {
      alert(statusMessages[status.code]);
    }
    
    console.log("✅ useEffect in login form is running");

    if (status.type == "Status" && status.code == "1000") {
      navigate("/chat");
    }
  }, [status]);

  const handleSubmit = (event) => {
    localUserName = event.fildname;
    localUserPassword = event.password;

    if (socket && socket.readyState === WebSocket.OPEN) {
      const loginPayload = {
        type: "Login",
        username: localUserName,
        password: localUserPassword,
      };

      console.log("📨 Sending login:", loginPayload);
      socket.send(JSON.stringify(loginPayload));
    } else {
      console.log("⚠️ WebSocket هنوز وصل نشده. منتظر اتصال باش.");
    }
  };

  const validationSchema = Yup.object({
    fildname: Yup.string()
      .min(4, "Username must be at least 4 characters")
      .matches(
        /^[a-zA-Z]+$/,
        "Username must only contain lowercase letters (a-z)"
      )
      .max(19, "Username must be a maximum of 19 characters")
      .required("Username is required"),
    password: Yup.string()
      .min(8, "Password must be at least 8 characters")
      .max(19, "Password must be at most 19 characters")
      .matches(
        /^[\x20-\x7E]+$/,
        "Password can only include English letters, numbers, and symbols"
      )
      .required("Password is required"),
  });

  return (
    <div className="flex justify-center itmes-center w-full">
      <Formik
        initialValues={{ fildname: "", password: "" }}
        validationSchema={validationSchema}
        onSubmit={handleSubmit}
      >
        <Form className="bg-white rounded-2xl w-6/12 h-70 flex justify-center items-center mt-10 pb-2">
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
                <Field
                  name="password"
                  type="text"
                  placeholder="Password"
                  className="w-full h-10 border mt-4 border-gray-400 px-3 rounded-md"
                />
                <ErrorMessage
                  name="password"
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
            <p className="flex justify-center mt-1">
              Already have an account?
              <Link to="/" className="text-blue-600 underline ml-1">
                Sing up
              </Link>
            </p>
          </div>
        </Form>
      </Formik>
    </div>
  );
};

export default LoginForm;
