import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, createAuth0Adapter, LocalStorageTokenStore } from '@ssv/ui-core';
import type { AuthPort } from '@ssv/ui-core';
import RegisterPage from './pages/RegisterPage';
import PortfolioPage from './pages/PortfolioPage';

const domain = import.meta.env.VITE_AUTH0_DOMAIN;
const clientId = import.meta.env.VITE_AUTH0_CLIENT_ID;

let authAdapter: AuthPort;

if (domain && clientId) {
  authAdapter = createAuth0Adapter({
    domain,
    clientId,
    redirectUri: window.location.origin,
    logoutReturnTo: window.location.origin,
  }, new LocalStorageTokenStore());
} else {
  // Temporary mock to prove UI-Core integration without real credentials
  authAdapter = {
    register: async () => { 
      alert("¡Éxito! La integración con ui-core funciona. (Modo Mock: Esperando credenciales Auth0)"); 
    },
    login: async () => {},
    logout: async () => {},
    handleCallback: async () => {},
    getAccessToken: () => null,
    isAuthenticated: () => false,
  };
}

function App() {
  return (
    <AuthProvider auth={authAdapter}>
      <Router>
        <Routes>
          <Route path="/" element={<Navigate to="/register" replace />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/portfolio" element={<PortfolioPage />} />
        </Routes>
      </Router>
    </AuthProvider>
  );
}

export default App;
