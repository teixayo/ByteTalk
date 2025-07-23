import { useCallback } from "react";

const useAuth = () => {
  // Token
  const getToken = useCallback(() => {
    return localStorage.getItem("token");
  }, []);

  return { getToken };
};

export default useAuth;
