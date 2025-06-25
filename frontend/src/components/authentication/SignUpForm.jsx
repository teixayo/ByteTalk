
import { useEffect, useState } from "react";
import { Formik, Form, Field, ErrorMessage } from "formik";
import * as Yup from "yup";
import { useNavigate } from "react-router-dom";
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
    fildname: Yup.string().required("Username is required"),
    password: Yup.string().required("Username is required"),
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
              Do you have an account?
              <a href="/login" className="ml-1 text-blue-600">
                Register
              </a>
            </p>
          </div>
        </Form>
      </Formik>
    </div>
  );
};

export default SignUpForm;
