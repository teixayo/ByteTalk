import "./App.css";
import { Route, Routes } from "react-router-dom";

import AuthGate from "./components/AuthGate.jsx";
import SignUpForm from "./components/authentication/SignUpForm";
import Chat from "./components/Chat";
import LoginForm from "./components/authentication/LoginForm";
import PrivetChat from "./components/PrivetChat.jsx";

const App = () => {
  return (
    <AuthGate>
      <Routes>
        <Route path="/" element={<SignUpForm />} />
        <Route path="/chat" element={<Chat />} />
        <Route path="/chat/:userID" element={<PrivetChat/>} />
        <Route path="/login" element={<LoginForm />} />

      </Routes>
    </AuthGate>
  );
};

export default App;
