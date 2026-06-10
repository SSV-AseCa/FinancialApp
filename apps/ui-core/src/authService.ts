export const authService = {
  register: async (data: { email: string; name: string }): Promise<{ success: boolean; token: string }> => {
    console.log("Mock Auth0 Register called with", data);
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve({ success: true, token: "mock_token_123" });
      }, 1000);
    });
  }
};
