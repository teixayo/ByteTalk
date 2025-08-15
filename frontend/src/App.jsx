import { Route, Routes, useNavigate } from "react-router-dom";
import { useState, useEffect, useRef } from "react";
import { Toaster } from "react-hot-toast";
import { Portal } from "react-portal";
import { Suspense, lazy } from "react";

import AuthGate from "./components/AuthGate.jsx";

const SignUpForm = lazy(() => import("./pages/authentication/SignUpForm.jsx"));
const LoginForm = lazy(() => import("./pages/authentication/LoginForm.jsx"));
const Chat = lazy(() => import("./pages/Chat"));
const PrivetChat = lazy(() => import("./pages/PrivetChat.jsx"));

const App = () => {
  const [isLoading, setIsLoading] = useState(false);
  const [selectedUser, setSelectedUser] = useState(null);
  const [fadeOut, setFadeOut] = useState(false);

  const popupRef = useRef(null);
  const navigate = useNavigate();

  useEffect(() => {
    if (!isLoading) {
      setTimeout(() => {
        setFadeOut(true);
      }, 1900);
      const timeout = setTimeout(() => {
        setIsLoading(true);
      }, 2000);

      return () => clearTimeout(timeout);
    }
  }, [isLoading]);

  // Close popup when clicked outside of it
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (popupRef.current && !popupRef.current.contains(event.target)) {
        setSelectedUser(null);
      }
    };

    document.addEventListener("mousedown", handleClickOutside);
    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, []);

  const LoadingScreen = () => {
    if (!isLoading && fadeOut) return null;
    return (
      <Portal>
          <div
            className={`fixed inset-0 z-50 flex flex-col items-center backdrop-blur-xl justify-center transition-all duration-500 `}
          >
            <div
              className={`bg-white/10 rounded-2xl p-8 shadow-xl border border-white/30 backdrop-blur-lg text-white flex flex-col items-center `}
            >
              {/* ${
                fadeOut ? "fade-out-down" : "fade-in-up"
              } */}
              <div className="spinner rounded-full h-16 w-16 border-t-4 border-white border-solid mb-6" />
              <p className="text-lg font-semibold">Preparing...</p>
            </div>
          </div>
      </Portal>
    );
  };

  const Popup = () => {
    return (
      <Portal>
        <div
          className="fixed inset-0 backdrop-blur-xs bg-opacity-50 flex items-center justify-center z-50 transition-opacity duration-300 ease-out"
          style={{
            opacity: selectedUser ? 1 : 0,
            pointerEvents: selectedUser ? "auto" : "none",
          }}
          onClick={(e) => {
            if (e.target === e.currentTarget) {
              setSelectedUser(null);
            }
          }}
        >
          <div
            ref={popupRef}
            className={` popup-animation
      bg-neutral-900 rounded-lg p-5 sm:p-6 max-w-sm sm:max-w-md w-full mx-4 border border-neutral-800
      transition-all duration-300 ease-out transform
      ${selectedUser ? "scale-100 opacity-100" : "scale-95 opacity-0"}
    `}
            onClick={(e) => e.stopPropagation()}
          >
            <div className="flex items-center">
              <div className="mr-1 sm:mr-4">
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  fill="none"
                  viewBox="0 0 24 24"
                  strokeWidth={1}
                  stroke="currentColor"
                  className="size-9 sm:size-11 text-white"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    d="M17.982 18.725A7.488 7.488 0 0 0 12 15.75a7.488 7.488 0 0 0-5.982 2.975m11.963 0a9 9 0 1 0-11.963 0m11.963 0A8.966 8.966 0 0 1 12 21a8.966 8.966 0 0 1-5.982-2.275M15 9.75a3 3 0 1 1-6 0 3 3 0 0 1 6 0Z"
                  />
                </svg>
              </div>
              <div>
                <h3 className="text-xs sm:text-lg text-white">
                  {selectedUser?.username}
                </h3>
              </div>
            </div>

            <div className="mt-3 sm:mt-4">
              <button
                onClick={() => {
                  setFadeOut(false);
                  setIsLoading(false);
                  navigate(`/chat/${selectedUser?.username}`);
                  setSelectedUser(null);
                }}
                className="bg-blue-600 hover:bg-blue-700 cursor-pointer text-white py-1 sm:py-2 px-4 rounded-sm sm:rounded-lg w-full transition-colors duration-200"
              >
                Send private message
              </button>
            </div>
          </div>
        </div>
      </Portal>
    );
  };

  return (
    <AuthGate>
      <Toaster
        position="top-right"
        toastOptions={{
          style: {
            background: "#1a1a1a",
            color: "#f0f0f0",
            border: "1px solid #333",
            padding: "10px 15px",
            fontSize: "0.9rem",
          },
          success: {
            iconTheme: {
              primary: "#4ade80",
              secondary: "#1a1a1a",
            },
          },
          error: {
            iconTheme: {
              primary: "#f87171",
              secondary: "#1f1f1f",
            },
          },
        }}
      />

      {selectedUser && <Popup />}

      {!isLoading && <LoadingScreen />}

      <Suspense
        fallback={<div className="text-white p-4">Loading page...</div>}
      >
        <Routes>
          <Route path="/" element={<SignUpForm />} />
          <Route
            path="/chat"
            element={
              <Chat
                setIsLoading={setIsLoading}
                setFadeOut={setFadeOut}
                selectedUser={selectedUser}
                setSelectedUser={setSelectedUser}
              />
            }
          />
          <Route
            path="/chat/:userID"
            element={
              <PrivetChat
                setIsLoading={setIsLoading}
                setFadeOut={setFadeOut}
                selectedUser={selectedUser}
                setSelectedUser={setSelectedUser}
              />
            }
          />
          <Route path="/login" element={<LoginForm />} />
        </Routes>
      </Suspense>
    </AuthGate>
  );
};

export default App;
