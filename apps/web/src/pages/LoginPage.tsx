import { useState } from 'react';
import { useLocation, Link } from 'react-router-dom';
import { useAuth } from '@ssv/ui-core';
import { LogIn } from 'lucide-react';
import { Button } from '../components/ui/button';

export default function LoginPage() {
  const auth = useAuth();
  const location = useLocation();
  const [isSigningIn, setIsSigningIn] = useState(false);
  const [errorMsg, setErrorMsg] = useState<string | null>(location.state?.error || null);

  const handleLogin = async () => {
    setIsSigningIn(true);
    setErrorMsg(null);
    try {
      await auth.login();
    } catch (err) {
      console.error('Login failed', err);
      setErrorMsg(err instanceof Error ? err.message : 'Login failed due to an unknown error');
      setIsSigningIn(false);
    }
  };

  return (
    <div className="min-h-screen bg-background text-foreground flex flex-col justify-center items-center p-4 sm:p-8 relative overflow-hidden">
      <div className="absolute top-[-10%] left-[-10%] w-[40%] h-[40%] bg-primary/20 rounded-full blur-[120px] pointer-events-none" />
      <div className="absolute bottom-[-10%] right-[-10%] w-[40%] h-[40%] bg-ring/20 rounded-full blur-[120px] pointer-events-none" />

      <div className="w-full max-w-md relative z-10">
        <div className="text-center mb-10">
          <h1 className="text-4xl font-extrabold tracking-tight mb-2 bg-gradient-to-r from-primary to-ring bg-clip-text text-transparent">
            Welcome Back
          </h1>
          <p className="text-muted-foreground text-lg">
            Sign in to continue to your portfolio.
          </p>
        </div>

        <div className="bg-card/50 backdrop-blur-xl border border-white/10 shadow-2xl rounded-2xl p-8 space-y-4 transition-all hover:border-primary/30 hover:shadow-primary/5">
          {errorMsg && (
            <div className="bg-destructive/10 text-destructive border border-destructive/20 p-3 rounded-xl text-sm text-center">
              {errorMsg}
            </div>
          )}

          <Button
            id="login-button"
            onClick={handleLogin}
            disabled={isSigningIn}
            className="w-full bg-card/80 hover:bg-card text-foreground border border-white/10 hover:border-primary/30"
          >
            <LogIn className="h-5 w-5 mr-2" />
            <span>{isSigningIn ? 'Redirecting…' : 'Sign In'}</span>
          </Button>
          
          <div className="mt-4 text-center text-sm text-muted-foreground">
            Don't have an account?{' '}
            <Link to="/register" className="text-primary hover:underline hover:text-primary/80 transition-colors">
              Create one
            </Link>
          </div>
        </div>

        <p className="text-center text-xs text-muted-foreground mt-6">
          By continuing, you agree to our Terms of Service and Privacy Policy.
        </p>
      </div>
    </div>
  );
}
