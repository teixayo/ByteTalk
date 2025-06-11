import { Formik, Form, Field, ErrorMessage } from "formik";
import * as Yup from "yup";
import { useNavigate } from "react-router-dom";

const SignUpForm = () => {
  const navigate = useNavigate()

  const handleSubmit = () => {
    navigate("/chat")
  };

  const validationSchema = Yup.object({
    email: Yup.string()
      .email("فرمت ایمیل اشتباهه")
      .required("ایمیل الزامی است"),
    password: Yup.string()
      .min(8, "رمز عبور باید حداقل 8 کاراکتر باشد")
      .required("رمز عبور الزامی است"),
    name: Yup.string().required("اسم الزامی است"),
  });

  return (
    <div className="flex justify-center itmes-center w-full">
      <Formik
        initialValues={{ email: "", password: "", name: "" }}
        validationSchema={validationSchema}
        onSubmit={handleSubmit}
      >
        <Form className="bg-white rounded-2xl w-6/12 h-90 flex justify-center items-center mt-10 pb-2">
          <div className="w-full">
            <div className="flex justify-center">
              <p className="text-2xl font-bold mb-5">Sing Up</p>
            </div>
            <div className="flex justify-center">
              <div className="w-10/12">
                <Field
                  name="email"
                  type="email"
                  placeholder="Email"
                  className="w-full h-10 border border-gray-400 px-3 rounded-md"
                />

                <ErrorMessage
                  name="email"
                  component="div"
                  className="text-red-500 text-sm"
                />
              </div>
            </div>
            <div className="flex justify-center mt-4 ">
              <div className="w-10/12">
                <Field
                  name="password"
                  type="password"
                  placeholder="Password"
                  className="w-full h-10 border border-gray-400 px-3 rounded-md"
                />
                <ErrorMessage
                  name="password"
                  component="div"
                  className="text-red-500 text-sm"
                />
              </div>
            </div>
            <div className="flex justify-center mt-4 ">
              <div className="w-10/12">
                <Field
                  name="name"
                  type="text"
                  placeholder="Name"
                  className="w-full h-10 border border-gray-400 px-3 rounded-md"
                />
                <ErrorMessage
                  name="name"
                  component="div"
                  className="text-red-500 text-sm"
                />
              </div>
            </div>
            <div className="flex justify-center mt-4">
              <button className="bg-blue-600 w-10/12 h-11 rounded-md text-white">
                Sign up
              </button>
            </div>
          </div>
        </Form>
      </Formik>
    </div>
  );
};

export default SignUpForm;