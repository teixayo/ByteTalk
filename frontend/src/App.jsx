import "./App.css";
import { Route, Routes } from "react-router-dom";
import SignUpForm from "./components/authentication/SignUpForm";
import Chat from "./components/Chat";
import LoginForm from "./components/authentication/LoginForm";
const App = () => {

  return (
    <Routes>
      <Route path="/" element={<SignUpForm/>}/>
      <Route path="/chat" element={<Chat/>}/>
      <Route path="/login" element={<LoginForm/>}/>
    </Routes>
  );
};

export default App;
