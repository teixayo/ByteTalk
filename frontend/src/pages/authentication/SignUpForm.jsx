import { useEffect } from "react";
import { Formik, Form, Field, ErrorMessage } from "formik";
import { useNavigate, Link } from "react-router-dom";
import * as Yup from "yup";

import { useSocket } from "../../context/SocketContext";

let localUserName = "";
let localUserPassword = "";

const SignUpForm = () => {
  const navigate = useNavigate();
  const { socket, status } = useSocket();


  useEffect(() => {
    if (status.type == "Status" && status.code == "1004") {
      setTimeout(() => {
        localStorage.setItem("username", localUserName);
        const loginPayload = {
          type: "Login",
          username: localUserName,
          password: localUserPassword,
        };
        // console.log("📨 Sending login:", loginPayload);
        // console.log("WS readyState:", socket.readyState);
        socket.send(JSON.stringify(loginPayload));
      }, 100);
      setTimeout(() => {

        navigate("/chat");
      }, 1800);
    }
  }, [status]);

  const handleSubmit = (values) => {
    localUserName = values.fildname;
    localUserPassword = values.password;

    if (!socket || socket.readyState !== WebSocket.OPEN) {
      console.warn("❌ WebSocket not ready");
      return;
    }

    const signupPayload = {
      type: "CreateUser",
      username: localUserName,
      password: localUserPassword,
    };

    socket.send(JSON.stringify(signupPayload));
  };

  const validationSchema = Yup.object({
    fildname: Yup.string()
      .min(4, "Username must be at least 4 characters")
      .matches(
        /^[a-zA-Z][a-zA-Z0-9]*$/,
        "Username must start with a letter and contain only English letters and numbers."
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
    <div className="flex justify-center text-white itmes-center w-full">
      <Formik
        initialValues={{ fildname: "", password: "" }}
        validationSchema={validationSchema}
        onSubmit={handleSubmit}
      >
        <Form className="bg-neutral-900 rounded-2xl w-11/12 sm:10/12 md:w-7/12 xl:w-4/12 md:h-70 flex justify-center items-center mt-10 pb-2">
          <div className="w-full">
            <div className="flex justify-center mt-4 pt-2">
              <div className="w-10/12 my-2">
                <Field
                  name="fildname"
                  type="text"
                  placeholder="UserName"
                  className="w-full h-10 bg-neutral-700 px-3 rounded-md"
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
                  className="w-full h-10 mt-4 bg-neutral-700 px-3 rounded-md"
                />
                <ErrorMessage
                  name="password"
                  component="div"
                  className="text-red-500 t`ext-sm ml-0.5"
                />
              </div>
            </div>

            <div className="flex justify-center mt-2 pb-2">
              <button
                type="submit"
                className="bg-blue-600 hover:bg-blue-700 cursor-pointer w-10/12 h-11 rounded-md text-white transition-colors duration-200"
              >
                Sign Up
              </button>
            </div>
            <p className="flex justify-center mt-1 pb-6">
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