import { useCallback } from "react";

const useAuth = () => {
  // Token
  const getToket = useCallback(() => {
    return localStorage.getItem("token");
  }, []);


  return { getToket, };
};

export default useAuth;
