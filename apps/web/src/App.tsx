import {
  BrowserRouter as Router,
  Routes,
  Route,
  Navigate,
} from "react-router-dom";
import {
  AuthProvider,
  PortfolioProvider,
  CompanyProvider,
  TradingProvider,
  WatchlistProvider,
  createAuth0Adapter,
  LocalStorageTokenStore,
  HttpApiAdapter,
} from "@ssv/ui-core";
import type { AuthPort, PortfolioPort, CompanyPort, TradingPort, WatchlistPort } from "@ssv/ui-core";
import RegisterPage from "./pages/RegisterPage";
import LoginPage from "./pages/LoginPage";
import PortfolioPage from "./pages/PortfolioPage";
import CallbackPage from "./pages/CallbackPage";
import CompanySearchPage from "./pages/CompanySearchPage";
import CompanyDetailPage from "./pages/CompanyDetailPage";
import WatchlistPage from "./pages/WatchlistPage";
import TradingPage from "./pages/TradingPage";
import AuthGuard from "./components/AuthGuard";

const domain = import.meta.env.VITE_AUTH0_DOMAIN;
const clientId = import.meta.env.VITE_AUTH0_CLIENT_ID;
const audience = import.meta.env.VITE_AUTH0_AUDIENCE;
const apiBaseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";

const callbackUrl = `${window.location.origin}/auth/callback`;

let authAdapter: AuthPort;

if (domain && clientId) {
  authAdapter = createAuth0Adapter(
    {
      domain,
      clientId,
      audience,
      redirectUri: callbackUrl,
      logoutReturnTo: window.location.origin,
    },
    new LocalStorageTokenStore(),
  );
} else {
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

const apiAdapter = new HttpApiAdapter(authAdapter, apiBaseUrl);
const portfolioAdapter: PortfolioPort = apiAdapter;
const companyAdapter: CompanyPort = apiAdapter;
const tradingAdapter: TradingPort = apiAdapter;
const watchlistAdapter: WatchlistPort = apiAdapter;

function App() {
  return (
    <AuthProvider auth={authAdapter}>
      <PortfolioProvider port={portfolioAdapter}>
        <CompanyProvider port={companyAdapter}>
          <TradingProvider port={tradingAdapter}>
            <WatchlistProvider port={watchlistAdapter}>
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
                  <Route
                    path="/companies"
                    element={
                      <AuthGuard>
                        <CompanySearchPage />
                      </AuthGuard>
                    }
                  />
                  <Route
                    path="/companies/:cik"
                    element={
                      <AuthGuard>
                        <CompanyDetailPage />
                      </AuthGuard>
                    }
                  />
                  <Route
                    path="/watchlist"
                    element={
                      <AuthGuard>
                        <WatchlistPage />
                      </AuthGuard>
                    }
                  />
                  <Route
                    path="/trading"
                    element={
                      <AuthGuard>
                        <TradingPage />
                      </AuthGuard>
                    }
                  />
                  <Route path="/auth/callback" element={<CallbackPage />} />
                </Routes>
              </Router>
            </WatchlistProvider>
          </TradingProvider>
        </CompanyProvider>
      </PortfolioProvider>
    </AuthProvider>
  );
}

export default App;
