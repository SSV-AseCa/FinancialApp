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
                <h1>Welcome home</h1>
                <p className="register-description">
                    Your account is ready. This is the authenticated home screen.
                </p>

                <button className="register-button" type="button" onClick={handleLogout}>
                    Log out
                </button>
            </section>
        </main>
    )
}