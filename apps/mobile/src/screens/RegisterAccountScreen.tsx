import { useState } from 'react'
import { Wallet, Send, BarChart2, Lock } from 'lucide-react'

export function RegisterAccountScreen() {
    const [loading, setLoading] = useState(false)
    const [error, setError] = useState<string | null>(null)
    const [form, setForm] = useState({firstName: '', lastName: '', email: '', password: ''})
    const [sheetOpen, setSheetOpen] = useState (false)

    async function handleRegister() {
        try {
            setLoading(true)
            setError(null)

            console.log('Register account clicked')

            await new Promise((resolve) => {
                setTimeout(resolve, 1000)
            })
        } catch {
            setError('Could not start registration. Please try again.')
        } finally {
            setLoading(false)
        }
    }

    function handleChange(e: React.ChangeEvent<HTMLInputElement>) {
        setForm(prev => ({...prev, [e.target.name]: e.target.value}))
    }


    return (
        <main className="register-page">
            <section className="register-hero">

                <div className="register-app-icon" aria-hidden="true">💳</div>

                <h1 className="register-title">Finance</h1>
                <p className="register-description">Your smart wallet.</p>

                {/* Balance card */}
                <div className="register-preview">
                    <p className="register-preview-label">Your Balance</p>
                    <p className="register-preview-amount">$20,432.81</p>
                    <span className="register-preview-badge">+$528.32 (12.3%) ↗</span>

                    {/* SVG line chart decorativo */}
                    <svg className="register-preview-chart" viewBox="0 0 300 48" fill="none">
                        <polyline
                            points="0,38 40,30 80,34 120,20 160,24 200,14 240,18 300,8"
                            stroke="#86d3a6"
                            strokeWidth="2"
                            strokeLinecap="round"
                            strokeLinejoin="round"
                        />
                    </svg>

                    <div className="register-preview-actions">
                        <button className="register-preview-btn-primary" type="button">Withdraw</button>
                        <button className="register-preview-btn-secondary" type="button">Deposit</button>
                    </div>
                </div>

                {/* Feature grid */}
                <div className="register-features">
                    {[
                        { icon: <Wallet size={20} />, title: 'Total Balance', desc: 'Visualize all your funds in one place' },
                        { icon: <Send size={20}/>, title: 'Transfers', desc: 'Send money fast and safe.' },
                        { icon: <BarChart2 size={20}/>, title: 'Statistics', desc: 'Monthly reports from your expenses.' },
                        { icon: <Lock size={20}/>, title: 'Security', desc: 'Bank-level encryption.' },
                    ].map(f => (
                        <div className="register-feature-card" key={f.title}>
                            <div className="register-feature-icon" aria-hidden="true">{f.icon}</div>
                            <p className="register-feature-title">{f.title}</p>
                            <p className="register-feature-desc">{f.desc}</p>
                        </div>
                    ))}
                </div>
            </section>

            {/* CTA */}
            <div className="register-cta-area">
                <button
                    className="register-button-hero"
                    type="button"
                    onClick={() => setSheetOpen(true)}
                    style={{ opacity: sheetOpen ? 0 : 1, pointerEvents: sheetOpen ? 'none' : 'auto', transition: 'opacity 0.3s' }}
                >
                    Create Account
                </button>
                <button className="register-button-secondary" type="button">
                    Log in
                </button>
            </div>

            <div className="register-trust">
                <span>Safe</span>
                <span>•</span>
                <span>No card</span>
                <span>•</span>
                <span>Instant Access</span>
            </div>

            <div
                className={`register-overlay ${sheetOpen ? 'register-overlay--visible' : ''}`}
                onClick={() => setSheetOpen(false)}
                aria-hidden="true"
            />

            {/* Bottom sheet — formulario */}
            <section className={`register-sheet ${sheetOpen ? 'register-sheet--open' : ''}`} aria-labelledby="register-title">                <div className="register-handle" aria-hidden="true" onClick={() => setSheetOpen(false)} style={{ cursor: 'pointer' }} />

                <div className="register-name-row">
                    <div className="register-field">
                        <label htmlFor="firstName">Name</label>
                        <input id="firstName" name="firstName" type="text"
                               placeholder="Juan" value={form.firstName} onChange={handleChange}/>
                    </div>
                    <div className="register-field">
                        <label htmlFor="lastName">Lastname</label>
                        <input id="lastName" name="lastName" type="text"
                               placeholder="García" value={form.lastName} onChange={handleChange}/>
                    </div>
                </div>

                <div className="register-field">
                    <label htmlFor="email">Email</label>
                    <input id="email" name="email" type="email"
                           placeholder="juan@email.com" value={form.email} onChange={handleChange}/>
                </div>

                <div className="register-field">
                    <label htmlFor="password">Password</label>
                    <input id="password" name="password" type="password"
                           placeholder="••••••••" value={form.password} onChange={handleChange}/>
                </div>

                <button
                    className="register-button"
                    type="button"
                    onClick={handleRegister}
                    disabled={loading}
                >
                    {loading ? 'Creating Account...' : 'Create Account'}
                </button>

                <div className="register-trust" aria-label="Garantías de seguridad">
                    <span>🔒 SSL seguro</span>
                    <span aria-hidden="true">·</span>
                    <span>Sin tarjeta requerida</span>
                    <span aria-hidden="true">·</span>
                    <span>Gratis</span>
                </div>

                <p className="register-secondary-text">
                    ¿Already have an account? <span role="button" tabIndex={0}>Log in</span>
                </p>

                {error && <p className="register-error" role="alert">{error}</p>}
            </section>
        </main>
    )
}