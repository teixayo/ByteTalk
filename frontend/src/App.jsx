import "./App.css";
import { Route, Routes } from "react-router-dom";

import AuthGate from "./components/AuthGate.jsx";
import SignUpForm from "./components/authentication/SignUpForm";
import Chat from "./components/Chat";
import LoginForm from "./components/authentication/LoginForm";
import PrivetChat from "./components/PrivetChat.jsx";
import { Toaster } from "react-hot-toast";

const App = () => {
  return (
    <AuthGate>
      <Toaster
        position="top-right"
        toastOptions={{
          style: {
            background: "#1a1a1a", // خیلی نزدیک به رنگ بک‌گراند چت
            color: "#f0f0f0", // متن روشن
            border: "1px solid #333", // بوردر ظریف
            padding: "10px 15px",
            fontSize: "0.9rem",
          },
          success: {
            iconTheme: {
              primary: "#4ade80", // سبز ملایم
              secondary: "#1a1a1a",
            },
          },
          error: {
            iconTheme: {
              primary: "#f87171", // قرمز ملایم
              secondary: "#1f1f1f",
            },
          },
        }}
      />
      <Routes>
        <Route path="/" element={<SignUpForm />} />
        <Route path="/chat" element={<Chat />} />
        <Route path="/chat/:userID" element={<PrivetChat />} />
        <Route path="/login" element={<LoginForm />} />
      </Routes>
    </AuthGate>
  );
};

export default App;
