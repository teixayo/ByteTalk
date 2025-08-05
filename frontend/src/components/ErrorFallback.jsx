const ErrorFallback = ({ error, resetErrorBoundary }) => {
  return (
    <div className="flex items-center justify-center h-screen bg-[#1a1a1e] text-white px-4">
      <div className="backdrop-blur-md bg-white/5 border-2 border-white/9 p-6 rounded-xl shadow-lg max-w-md w-full text-center overflow-hidden">
        <h2 className="text-2xl font-bold mb-3">Oops! Something went wrong 😢</h2>

        <p className="text-sm text-gray-300 mb-4 break-words whitespace-pre-wrap max-h-64 overflow-y-auto px-1">
          {error.message}
        </p>

        <button
          onClick={resetErrorBoundary}
          className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded transition cursor-pointer"
        >
          Try Again
        </button>
      </div>
    </div>
  );
};

export default ErrorFallback;
