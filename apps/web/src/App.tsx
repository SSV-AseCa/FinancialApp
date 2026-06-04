import {
  BrowserRouter as Router,
  Routes,
  Route,
  Navigate,
} from "react-router-dom";
import {
  AuthProvider,
  PortfolioProvider,
  createAuth0Adapter,
  LocalStorageTokenStore,
  HttpApiAdapter,
} from "@ssv/ui-core";
import type { AuthPort, PortfolioPort } from "@ssv/ui-core";
import RegisterPage from "./pages/RegisterPage";
import LoginPage from "./pages/LoginPage";
import PortfolioPage from "./pages/PortfolioPage";
import CallbackPage from "./pages/CallbackPage";
import AuthGuard from "./components/AuthGuard";

const domain = import.meta.env.VITE_AUTH0_DOMAIN;
const clientId = import.meta.env.VITE_AUTH0_CLIENT_ID;
const apiBaseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";

const callbackUrl = `${window.location.origin}/auth/callback`;

let authAdapter: AuthPort;

if (domain && clientId) {
  authAdapter = createAuth0Adapter(
    {
      domain,
      clientId,
      redirectUri: callbackUrl,
      logoutReturnTo: window.location.origin,
    },
    new LocalStorageTokenStore(),
  );
} else {
  // Temporary mock adapter used in dev without real Auth0 credentials.
  // Uses LocalStorageTokenStore so that Cypress can inject a mock token
  // and isAuthenticated() returns true correctly during E2E tests.
  const store = new LocalStorageTokenStore();
  authAdapter = {
    register: async () => {
      alert(
        "¡Éxito! La integración con ui-core funciona. (Modo Mock: Esperando credenciales Auth0)",
      );
    },
    login: async () => {
      store.save("mock-access-token");
      window.location.href = "/portfolio";
    },
    logout: async () => {
      store.clear();
      window.location.href = "/login";
    },
    handleCallback: async () => {
      const params = new URLSearchParams(window.location.search);
      if (params.get("error") || params.get("state") === "mock_state") {
        throw new Error("Invalid state");
      }
      store.save("mock-access-token");
    },
    getAccessToken: () => store.load(),
    isAuthenticated: () => store.load() !== null,
  };
}

// The real API adapter uses the auth token from authAdapter.
// In production both adapters talk to the same backend.
const portfolioAdapter: PortfolioPort = new HttpApiAdapter(authAdapter, apiBaseUrl);

function App() {
  return (
    <AuthProvider auth={authAdapter}>
      <PortfolioProvider port={portfolioAdapter}>
        <Router>
          <Routes>
            <Route path="/" element={<Navigate to="/login" replace />} />
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />
            <Route
              path="/portfolio"
              element={
                <AuthGuard>
                  <PortfolioPage />
                </AuthGuard>
              }
            />
            <Route path="/auth/callback" element={<CallbackPage />} />
          </Routes>
        </Router>
      </PortfolioProvider>
    </AuthProvider>
  );
}

export default App;
