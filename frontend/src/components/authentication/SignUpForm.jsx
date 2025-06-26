import { useEffect, useState } from "react";
import { Formik, Form, Field, ErrorMessage } from "formik";
import * as Yup from "yup";
import { useNavigate, Link } from "react-router-dom";
import { useSocket } from "../../context/SocketContext";

let localUserName = "";
let localUserPassword = "";

const SignUpForm = () => {
  const navigate = useNavigate();
  const { socket } = useSocket();

  useEffect(() => {
    if (!socket) return;

    socket.onmessage = (event) => {
      console.log("im here");
      const data = JSON.parse(event.data);
      console.log("📨 Message received:", data);

      if (data.type == "Status" && data.code == "1000") {
        const loginPayload = {
          type: "Login",
          name: localUserName,
          password: localUserPassword,
        };

        console.log("📨 Sending login:", loginPayload);
        socket.send(JSON.stringify(loginPayload));

        alert("Sing up successful");
        navigate("/chat");
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
    localUserPassword = values.password;

    if (!socket || socket.readyState !== WebSocket.OPEN) {
      console.warn("❌ WebSocket not ready");
      return;
    }

    const signupPayload = {
      type: "CreateUser",
      name: localUserName,
      password: localUserPassword,
    };

    console.log("📨 Sending CreateUser", signupPayload);
    socket.send(JSON.stringify(signupPayload));
  };

  const validationSchema = Yup.object({
      fildname: Yup.string()
        .min(3, "Username must be at least 3 characters")
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
                Sing Up
              </button>
            </div>
            <p className="flex justify-center mt-1">
              Already have an account?
              <Link to="/login" className="text-blue-600 underline ml-1">
                Login
              </Link>
            </p>
          </div>
        </Form>
      </Formik>
    </div>
  );
};

export default SignUpForm;
