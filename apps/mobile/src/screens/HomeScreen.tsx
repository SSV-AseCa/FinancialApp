import { useAuth } from '@ssv/ui-core'

type HomeScreenProps = {
    onLogout: () => void
}

export function HomeScreen({ onLogout }: HomeScreenProps) {
    const auth = useAuth()

    async function handleLogout() {
        await auth.logout()
        onLogout()
    }

    return (
        <main className="register-page">
            <section className="register-card">
                <p className="register-eyebrow">Financial App</p>
                <h1 data-testid="portfolio-screen-title">
                    Welcome home
                </h1>
                <p className="register-description"
                    data-testid="protected-screen-content">
                    Your account is ready. This is the authenticated home screen.
                </p>

                <button className="register-button"
                        type="button"
                        onClick={handleLogout}
                        data-testid="logout-button">
                    Log out
                </button>
            </section>
        </main>
    )
}