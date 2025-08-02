import { createRoot } from "react-dom/client";
import { BrowserRouter } from "react-router-dom";
import { ErrorBoundary } from "react-error-boundary";

import { SocketProvider } from "./context/SocketContext.jsx";
import App from "./App.jsx";
import ErrorFallback from "./components/ErrorFallback.jsx";

import "./index.css";

import { useState } from "react";

const Root = () => {
  const [retryKey, setRetryKey] = useState(0);

  return (
    <BrowserRouter>
      <ErrorBoundary
        FallbackComponent={ErrorFallback}
        onReset={() => setRetryKey(prev => prev + 1)}
      >
        <SocketProvider>
          <App key={retryKey} />
        </SocketProvider>
      </ErrorBoundary>
    </BrowserRouter>
  );
};

createRoot(document.getElementById("root")).render(<Root />);
