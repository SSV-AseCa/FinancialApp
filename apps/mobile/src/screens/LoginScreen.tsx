import { Page, Block, Button } from 'konsta/react'
import { TrendingUp } from 'lucide-react'
import { useAuth } from '@ssv/ui-core'

type LoginScreenProps = {
    onCreateAccount: () => void
    onAuthenticated?: () => void
    errorMessage?: string | null
}

export function LoginScreen({
      onCreateAccount,
      onAuthenticated,
      errorMessage,
      }: LoginScreenProps) {
    const auth = useAuth()

    async function handleLogin() {
        await auth.login()
        if (auth.isAuthenticated()) {
            onAuthenticated?.()
        }
    }

    return (
        <Page data-testid="login-screen" className="flex flex-col justify-center">
            <Block className="flex flex-col items-center text-center">
                <div className="mb-4 flex h-16 w-16 items-center justify-center rounded-2xl bg-brand-primary/15 text-brand-primary">
                    <TrendingUp size={30} />
                </div>

                <h1
                    data-testid="login-screen-title"
                    className="m-0 text-2xl font-bold text-white"
                >
                    Welcome back
                </h1>

                <p className="mt-1 mb-0 text-sm text-white/60">
                    Log in securely to access your portfolio.
                </p>
            </Block>

            {errorMessage && (
                <Block className="-mt-2">
                    <p role="alert" className="m-0 text-center text-sm text-red-400">
                        {errorMessage}
                    </p>
                </Block>
            )}

            <Block className="flex flex-col gap-2">
                <Button large onClick={handleLogin} data-testid="login-button">
                    Log in
                </Button>

                <Button
                    large
                    clear
                    onClick={onCreateAccount}
                    data-testid="go-to-register-button"
                >
                    Create account
                </Button>
            </Block>
        </Page>
    )
}
