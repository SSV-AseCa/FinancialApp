import { useState } from 'react';
import { useLocation, Link } from 'react-router-dom';
import { useAuth } from '@ssv/ui-core';
import { UserPlus } from 'lucide-react';
import { Button } from '../components/ui/button';

export default function RegisterPage() {
  const auth = useAuth();
  const location = useLocation();
  const [isRegistering, setIsRegistering] = useState(false);
  const [errorMsg, setErrorMsg] = useState<string | null>(location.state?.error || null);

  const handleRegister = async () => {
    setIsRegistering(true);
    setErrorMsg(null);
    try {
      await auth.register();
    } catch (err) {
      console.error("Registration failed", err);
      setErrorMsg(
        err instanceof Error
          ? err.message
          : "Registration failed due to an unknown error",
      );
      setIsRegistering(false);
    }
  };

  return (
    <div className="min-h-screen bg-background text-foreground flex flex-col justify-center items-center p-4 sm:p-8 relative overflow-hidden">
      <div className="absolute top-[-10%] left-[-10%] w-[40%] h-[40%] bg-primary/20 rounded-full blur-[120px] pointer-events-none" />
      <div className="absolute bottom-[-10%] right-[-10%] w-[40%] h-[40%] bg-ring/20 rounded-full blur-[120px] pointer-events-none" />

      <div className="w-full max-w-md relative z-10">
        <div className="text-center mb-10">
          <h1 className="text-4xl font-extrabold tracking-tight mb-2 bg-gradient-to-r from-primary to-ring bg-clip-text text-transparent">
            Join the Future
          </h1>
          <p className="text-muted-foreground text-lg">
            Create your account to start tracking your portfolio.
          </p>
        </div>

        <div className="bg-card/50 backdrop-blur-xl border border-white/10 shadow-2xl rounded-2xl p-8 space-y-4 transition-all hover:border-primary/30 hover:shadow-primary/5">
          {errorMsg && (
            <div
              data-cy="error-banner"
              className="bg-destructive/10 text-destructive border border-destructive/20 p-3 rounded-xl text-sm text-center"
            >
              {errorMsg}
            </div>
          )}

          <Button
            id="register-button"
            onClick={handleRegister}
            disabled={isRegistering}
            className="w-full"
          >
            <UserPlus className="h-5 w-5" />
            <span>{isRegistering ? "Redirecting…" : "Create Account"}</span>
          </Button>

          <div className="mt-4 text-center text-sm text-muted-foreground">
            Already have an account?{' '}
            <Link to="/login" className="text-primary hover:underline hover:text-primary/80 transition-colors">
              Sign In
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
