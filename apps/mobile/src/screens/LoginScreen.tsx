import { useAuth } from '@ssv/ui-core'

type LoginScreenProps = {
    onCreateAccount: () => void
    errorMessage?: string | null
}

export function LoginScreen({
      onCreateAccount,
      errorMessage,
      }: LoginScreenProps) {
    const auth = useAuth()

    async function handleLogin() {
        await auth.login()
    }

    return (
        <main className="auth-page" data-testid="login-screen">
            <section className="auth-card">
                <p className="register-eyebrow">Financial App</p>

                <h1 data-testid="login-screen-title">Welcome back</h1>

                <p className="register-description">
                    Log in securely to access your portfolio.
                </p>

                {errorMessage && (
                    <p className="register-error" role="alert">
                        {errorMessage}
                    </p>
                )}

                <button
                    className="register-button"
                    type="button"
                    data-testid="login-button"
                    onClick={handleLogin}
                >
                    Log in
                </button>

                <button
                    className="register-button-secondary"
                    type="button"
                    data-testid="go-to-register-button"
                    onClick={onCreateAccount}
                >
                    Create account
                </button>
            </section>
        </main>
    )
}