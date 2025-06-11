import "./App.css";
import { Route, Routes } from "react-router-dom";
import SignUpForm from "./components/SignUpForm";
import Chat from "./components/Chat";

const App = () => {

  return (
    <Routes>
      <Route path="/" element={<SignUpForm/>}/>
      <Route path="/chat" element={<Chat/>}/>
    </Routes>
  );
};

export default App;
