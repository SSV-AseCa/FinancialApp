import { useState } from 'react'
import { TrendingUp, Briefcase, Eye, BarChart2, LineChart, ArrowUpRight } from 'lucide-react'
import { Page, Block, Button, Sheet } from 'konsta/react'
import { useAuth } from '@ssv/ui-core'

type RegisterAccountScreenProps = {
    onAuthenticated: () => void
    onLogin: () => void
}

const FEATURES = [
    { icon: <Briefcase size={20} />, title: 'Positions & P&L', desc: 'Track every holding and its live gain or loss.' },
    { icon: <Eye size={20} />, title: 'Watchlist', desc: 'Follow companies and compare their fundamentals.' },
    { icon: <BarChart2 size={20} />, title: 'SEC Filings', desc: 'Read EDGAR filings straight from the source.' },
    { icon: <LineChart size={20} />, title: 'Market Prices', desc: 'Up-to-date quotes from Yahoo Finance.' },
]

export function RegisterAccountScreen({
    onAuthenticated,
    onLogin,
    }: RegisterAccountScreenProps) {
    const [loading, setLoading] = useState(false)
    const [error, setError] = useState<string | null>(null)
    const [sheetOpen, setSheetOpen] = useState(false)
    const auth = useAuth()

    async function handleRegister() {
        try {
            setLoading(true)
            setError(null)

            await auth.register()

            setSheetOpen(false)
            onAuthenticated()
        } catch (error) {
            console.error('Register failed:', error)
            setError('Could not start registration. Please try again.')
        } finally {
            setLoading(false)
        }
    }

    return (
        <Page className="flex flex-col">
            <Block className="flex flex-col items-center text-center">
                <div className="mb-4 flex h-16 w-16 items-center justify-center rounded-2xl bg-brand-primary/15 text-brand-primary">
                    <TrendingUp size={30} />
                </div>

                <h1 className="m-0 text-3xl font-bold text-white">SSV</h1>
                <p className="mt-1 mb-0 text-sm text-white/60">
                    Track your US stock portfolio.
                </p>

                {/* Portfolio value preview (illustrative) */}
                <div className="mt-6 w-full rounded-2xl border border-white/10 bg-white/5 p-5 text-left">
                    <p className="m-0 text-xs text-white/60">Portfolio value</p>
                    <p className="mt-1 mb-1 text-3xl font-bold tracking-tight text-white">
                        $20,432.81
                    </p>
                    <span className="inline-flex items-center gap-1 text-sm font-medium text-emerald-400">
                        +$528.32 (12.3%) <ArrowUpRight size={14} />
                    </span>

                    <svg className="mt-3 h-12 w-full" viewBox="0 0 300 48" fill="none">
                        <polyline
                            points="0,38 40,30 80,34 120,20 160,24 200,14 240,18 300,8"
                            stroke="#34d399"
                            strokeWidth="2"
                            strokeLinecap="round"
                            strokeLinejoin="round"
                        />
                    </svg>
                </div>

                {/* Feature grid */}
                <div className="mt-4 grid w-full grid-cols-2 gap-2.5">
                    {FEATURES.map((f) => (
                        <div
                            key={f.title}
                            className="rounded-2xl border border-white/10 bg-white/5 p-4 text-left"
                        >
                            <div className="mb-2 text-brand-primary" aria-hidden="true">
                                {f.icon}
                            </div>
                            <p className="m-0 text-sm font-semibold text-white">{f.title}</p>
                            <p className="m-0 mt-1 text-xs leading-snug text-white/55">
                                {f.desc}
                            </p>
                        </div>
                    ))}
                </div>
            </Block>

            <Block className="mt-auto flex flex-col gap-2">
                <Button
                    large
                    data-testid="create-account-button"
                    onClick={() => setSheetOpen(true)}
                >
                    Create account
                </Button>
                <Button
                    large
                    clear
                    data-testid="go-to-login-button"
                    onClick={onLogin}
                >
                    Log in
                </Button>
                <p className="m-0 text-center text-xs text-white/45">
                    Secure · No card required · Instant access
                </p>
            </Block>

            <Sheet
                className="pb-safe w-full"
                opened={sheetOpen}
                onBackdropClick={() => setSheetOpen(false)}
            >
                <Block>
                    <h2 className="m-0 text-lg font-semibold text-white">
                        Create your secure account
                    </h2>
                    <p className="mt-2 mb-0 text-sm text-white/60">
                        We'll redirect you to our secure authentication provider to
                        complete your signup.
                    </p>

                    <div className="mt-4 flex flex-col gap-1 text-sm text-white/70">
                        <span>🔒 Secure authentication</span>
                        <span>⚡ Fast account setup</span>
                        <span>📱 Mobile-ready access</span>
                    </div>

                    <Button
                        large
                        className="mt-5"
                        data-testid="continue-secure-signup-button"
                        onClick={handleRegister}
                        disabled={loading}
                    >
                        {loading ? 'Opening secure signup…' : 'Continue to secure sign up'}
                    </Button>

                    <p className="mt-4 mb-0 text-center text-sm text-white/60">
                        Already have an account?{' '}
                        <span
                            role="button"
                            tabIndex={0}
                            data-testid="go-to-login-text"
                            className="font-semibold text-brand-primary"
                            onClick={onLogin}
                            onKeyDown={(event) => {
                                if (event.key === 'Enter' || event.key === ' ') {
                                    onLogin()
                                }
                            }}
                        >
                            Log in
                        </span>
                    </p>

                    {error && (
                        <p role="alert" className="mt-3 mb-0 text-center text-sm text-red-400">
                            {error}
                        </p>
                    )}
                </Block>
            </Sheet>
        </Page>
    )
}
