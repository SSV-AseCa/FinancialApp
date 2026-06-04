import { useState } from 'react';
import { useAuth } from '@ssv/ui-core';
import { LogOut } from 'lucide-react';
import { Button } from '../components/ui/button';

export default function PortfolioPage() {
  const auth = useAuth();
  const [isLoggingOut, setIsLoggingOut] = useState(false);

  const handleLogout = async () => {
    setIsLoggingOut(true);
    try {
      await auth.logout();
    } catch (err) {
      console.error('Logout failed', err);
    } finally {
      setIsLoggingOut(false);
    }
  };

  return (
    <div className="min-h-screen bg-background text-foreground flex flex-col">
      {/* Navigation Header */}
      <header className="w-full border-b border-white/10 bg-card/50 backdrop-blur-md px-6 py-4 flex justify-between items-center sticky top-0 z-50">
        <div className="text-xl font-bold bg-gradient-to-r from-primary to-ring bg-clip-text text-transparent tracking-wide">
          SSV Financial
        </div>
        <Button 
          onClick={handleLogout} 
          disabled={isLoggingOut}
          className="bg-card/80 text-foreground hover:bg-destructive/90 hover:text-destructive-foreground border border-white/10 transition-colors py-2 px-4"
        >
          <LogOut className="w-4 h-4 mr-2" />
          <span>{isLoggingOut ? 'Logging Out...' : 'Log Out'}</span>
        </Button>
      </header>

      {/* Main Content */}
      <main className="flex-1 flex flex-col items-center justify-center p-8 relative overflow-hidden">
        <div className="absolute top-[20%] left-[10%] w-[30%] h-[30%] bg-primary/10 rounded-full blur-[100px] pointer-events-none" />
        <div className="absolute bottom-[20%] right-[10%] w-[30%] h-[30%] bg-ring/10 rounded-full blur-[100px] pointer-events-none" />

        <div className="z-10 text-center">
          <h1 className="text-5xl font-extrabold tracking-tight mb-4 bg-gradient-to-br from-white to-white/50 bg-clip-text text-transparent">
            Your Portfolio
          </h1>
          <p className="text-muted-foreground text-xl max-w-lg mx-auto">
            Welcome to your financial hub. This screen is currently a placeholder for upcoming features.
          </p>
        </div>
      </main>
    </div>
  );
}
