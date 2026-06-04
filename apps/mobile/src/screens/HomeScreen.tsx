import { useCallback, useEffect, useState } from 'react'
import { usePortfolio, useAuth, useTrading, useCompany } from '@ssv/ui-core'
import type { Portfolio, AddPositionInput, ModifyPositionInput, Position, Transaction, Company } from '@ssv/ui-core'

type HomeScreenProps = {
    onLogout: () => void
}

type AppScreen = 'portfolio' | 'add-position' | 'edit-position' | 'trading' | 'company-search' | 'company-detail'

type PortfolioStatus =
    | { kind: 'loading' }
    | { kind: 'success'; data: Portfolio }
    | { kind: 'error'; message: string }

export function HomeScreen({ onLogout }: HomeScreenProps) {
    const portfolio = usePortfolio()
    const auth = useAuth()
    const [screen, setScreen] = useState<AppScreen>('portfolio')
    const [status, setStatus] = useState<PortfolioStatus>({ kind: 'loading' })
    const [editingPosition, setEditingPosition] = useState<Position | null>(null)

    // Trading
    const trading = useTrading()
    const [buyCik, setBuyCik] = useState('')
    const [buyQty, setBuyQty] = useState('')
    const [buyError, setBuyError] = useState<string | null>(null)
    const [buySaving, setBuySaving] = useState(false)
    const [sellCik, setSellCik] = useState('')
    const [sellQty, setSellQty] = useState('')
    const [sellError, setSellError] = useState<string | null>(null)
    const [sellSaving, setSellSaving] = useState(false)
    const [txHistory, setTxHistory] = useState<Transaction[]>([])
    const [txLoading, setTxLoading] = useState(false)

    // Company search
    const companyPort = useCompany()
    const [searchQuery, setSearchQuery] = useState('')
    const [searchResults, setSearchResults] = useState<Company[]>([])
    const [searchLoading, setSearchLoading] = useState(false)
    const [searchError, setSearchError] = useState<string | null>(null)
    const [selectedCompany, setSelectedCompany] = useState<Company | null>(null)

    // Add position form
    const [addTicker, setAddTicker] = useState('')
    const [addQty, setAddQty] = useState('')
    const [addDate, setAddDate] = useState(new Date().toISOString().slice(0, 10))
    const [addError, setAddError] = useState<string | null>(null)
    const [addSaving, setAddSaving] = useState(false)

    // Edit position form
    const [editTicker, setEditTicker] = useState('')
    const [editQty, setEditQty] = useState('')
    const [editDate, setEditDate] = useState('')
    const [editError, setEditError] = useState<string | null>(null)
    const [editSaving, setEditSaving] = useState(false)

    const doFetch = useCallback(() => {
        portfolio
            .fetchPortfolio()
            .then((data) => setStatus({ kind: 'success', data }))
            .catch((err: unknown) => {
                const message = err instanceof Error ? err.message : 'Failed to load portfolio.'
                setStatus({ kind: 'error', message })
            })
    }, [portfolio])

    useEffect(() => { doFetch() }, [doFetch])

    async function handleLogout() {
        await auth.logout()
        onLogout()
    }

    async function handleAddPosition() {
        const qty = parseInt(addQty, 10)
        if (!addTicker.trim() || isNaN(qty) || qty <= 0 || !addDate) {
            setAddError('All fields are required.')
            return
        }
        setAddSaving(true)
        setAddError(null)
        const input: AddPositionInput = { ticker: addTicker.trim(), quantity: qty, operationDate: addDate }
        try {
            await portfolio.addPosition(input)
            setAddTicker('')
            setAddQty('')
            setAddDate(new Date().toISOString().slice(0, 10))
            setScreen('portfolio')
            doFetch()
        } catch (err) {
            setAddError(err instanceof Error ? err.message : 'Failed to add position.')
        } finally {
            setAddSaving(false)
        }
    }

    function startEdit(position: Position) {
        setEditingPosition(position)
        setEditTicker(position.ticker)
        setEditQty(String(position.quantity))
        setEditDate(position.operationDate)
        setEditError(null)
        setScreen('edit-position')
    }

    async function handleEditPosition() {
        if (!editingPosition) return
        const qty = parseInt(editQty, 10)
        if (!editTicker.trim() || isNaN(qty) || qty <= 0 || !editDate) {
            setEditError('All fields are required.')
            return
        }
        setEditSaving(true)
        setEditError(null)
        const input: ModifyPositionInput = { ticker: editTicker.trim(), quantity: qty, operationDate: editDate }
        try {
            await portfolio.modifyPosition(editingPosition.id, input)
            setScreen('portfolio')
            doFetch()
        } catch (err) {
            setEditError(err instanceof Error ? err.message : 'Failed to update position.')
        } finally {
            setEditSaving(false)
        }
    }

    async function handleRemovePosition(positionId: string) {
        try {
            await portfolio.removePosition(positionId)
            doFetch()
        } catch {
            // ignore, could show error
        }
    }

    if (screen === 'add-position') {
        return (
            <main className="register-page" data-testid="add-position-screen">
                <section className="register-card">
                    <h1>Add Position</h1>
                    <div className="form-group">
                        <label>Ticker</label>
                        <input
                            data-testid="add-ticker-input"
                            value={addTicker}
                            onChange={(e) => setAddTicker(e.target.value)}
                            placeholder="e.g. AAPL"
                        />
                    </div>
                    <div className="form-group">
                        <label>Quantity</label>
                        <input
                            data-testid="add-quantity-input"
                            type="number"
                            min={1}
                            value={addQty}
                            onChange={(e) => setAddQty(e.target.value)}
                        />
                    </div>
                    <div className="form-group">
                        <label>Date</label>
                        <input
                            data-testid="add-date-input"
                            type="date"
                            value={addDate}
                            onChange={(e) => setAddDate(e.target.value)}
                        />
                    </div>
                    {addError && <p className="register-error" role="alert">{addError}</p>}
                    <button
                        data-testid="confirm-add-position-button"
                        className="register-button"
                        type="button"
                        onClick={handleAddPosition}
                        disabled={addSaving}
                    >
                        {addSaving ? 'Adding…' : 'Add Position'}
                    </button>
                    <button
                        className="register-button-secondary"
                        type="button"
                        onClick={() => setScreen('portfolio')}
                    >
                        Cancel
                    </button>
                </section>
            </main>
        )
    }

    async function handleBuy() {
        const qty = parseInt(buyQty, 10)
        if (!buyCik.trim() || isNaN(qty) || qty <= 0) {
            setBuyError('CIK and positive quantity required.')
            return
        }
        setBuySaving(true)
        setBuyError(null)
        try {
            await trading.buyShares({ cik: buyCik.trim(), quantity: qty })
            setBuyCik('')
            setBuyQty('')
            loadTxHistory()
        } catch (err) {
            setBuyError(err instanceof Error ? err.message : 'Buy failed.')
        } finally {
            setBuySaving(false)
        }
    }

    async function handleSell() {
        const qty = parseInt(sellQty, 10)
        if (!sellCik.trim() || isNaN(qty) || qty <= 0) {
            setSellError('CIK and positive quantity required.')
            return
        }
        setSellSaving(true)
        setSellError(null)
        try {
            await trading.sellShares({ cik: sellCik.trim(), quantity: qty })
            setSellCik('')
            setSellQty('')
            loadTxHistory()
        } catch (err) {
            setSellError(err instanceof Error ? err.message : 'Sell failed.')
        } finally {
            setSellSaving(false)
        }
    }

    function loadTxHistory() {
        setTxLoading(true)
        trading.getTransactionHistory()
            .then(setTxHistory)
            .catch(() => {})
            .finally(() => setTxLoading(false))
    }

    async function handleSearch() {
        if (!searchQuery.trim()) return
        setSearchLoading(true)
        setSearchError(null)
        try {
            const results = await companyPort.searchCompanies(searchQuery.trim())
            setSearchResults(results)
        } catch (err) {
            setSearchError(err instanceof Error ? err.message : 'Search failed.')
        } finally {
            setSearchLoading(false)
        }
    }

    if (screen === 'trading') {
        return (
            <main className="register-page" data-testid="trading-screen">
                <section className="register-card" style={{ maxWidth: '600px' }}>
                    <h1>Trading</h1>

                    <h2>Buy Shares</h2>
                    <div className="form-group">
                        <label>CIK</label>
                        <input data-testid="buy-cik-input" value={buyCik} onChange={(e) => setBuyCik(e.target.value)} placeholder="e.g. 0000320193" />
                    </div>
                    <div className="form-group">
                        <label>Quantity</label>
                        <input data-testid="buy-quantity-input" type="number" min={1} value={buyQty} onChange={(e) => setBuyQty(e.target.value)} />
                    </div>
                    {buyError && <p className="register-error">{buyError}</p>}
                    <button data-testid="buy-submit-button" className="register-button" type="button" onClick={handleBuy} disabled={buySaving}>
                        {buySaving ? 'Buying…' : 'Buy'}
                    </button>

                    <h2 style={{ marginTop: '16px' }}>Sell Shares</h2>
                    <div className="form-group">
                        <label>CIK</label>
                        <input data-testid="sell-cik-input" value={sellCik} onChange={(e) => setSellCik(e.target.value)} placeholder="e.g. 0000320193" />
                    </div>
                    <div className="form-group">
                        <label>Quantity</label>
                        <input data-testid="sell-quantity-input" type="number" min={1} value={sellQty} onChange={(e) => setSellQty(e.target.value)} />
                    </div>
                    {sellError && <p className="register-error">{sellError}</p>}
                    <button data-testid="sell-submit-button" className="register-button" type="button" onClick={handleSell} disabled={sellSaving}>
                        {sellSaving ? 'Selling…' : 'Sell'}
                    </button>

                    <h2 style={{ marginTop: '16px' }}>Transaction History</h2>
                    {txLoading && <p>Loading…</p>}
                    {txHistory.length === 0 && !txLoading && <p data-testid="no-transactions">No transactions yet.</p>}
                    <ul data-testid="transactions-list" style={{ listStyle: 'none', padding: 0 }}>
                        {txHistory.map((tx) => (
                            <li key={tx.id} data-testid={`transaction-${tx.id}`} style={{ padding: '4px 0', borderBottom: '1px solid rgba(255,255,255,0.1)' }}>
                                {tx.type} · {tx.quantity} shares · {tx.cik} · {tx.transactionDate}
                            </li>
                        ))}
                    </ul>

                    <button className="register-button-secondary" type="button" onClick={() => setScreen('portfolio')}>Back</button>
                </section>
            </main>
        )
    }

    if (screen === 'company-search') {
        return (
            <main className="register-page" data-testid="company-search-screen">
                <section className="register-card" style={{ maxWidth: '600px' }}>
                    <h1>Company Research</h1>
                    <div className="form-group">
                        <label>Search</label>
                        <input
                            data-testid="company-search-input"
                            value={searchQuery}
                            onChange={(e) => setSearchQuery(e.target.value)}
                            onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
                            placeholder="e.g. Apple or AAPL"
                        />
                    </div>
                    {searchError && <p className="register-error">{searchError}</p>}
                    <button data-testid="company-search-submit" className="register-button" type="button" onClick={handleSearch} disabled={searchLoading}>
                        {searchLoading ? 'Searching…' : 'Search'}
                    </button>

                    {searchResults.length === 0 && !searchLoading && searchQuery && (
                        <p data-testid="no-results">No results found.</p>
                    )}
                    <ul data-testid="company-search-results" style={{ listStyle: 'none', padding: 0, width: '100%' }}>
                        {searchResults.map((c) => (
                            <li key={c.cik} style={{ padding: '8px 0', borderBottom: '1px solid rgba(255,255,255,0.1)' }}>
                                <button
                                    data-testid={`company-result-${c.cik}`}
                                    type="button"
                                    onClick={() => { setSelectedCompany(c); setScreen('company-detail') }}
                                    style={{ background: 'none', border: 'none', color: 'inherit', cursor: 'pointer', textAlign: 'left' }}
                                >
                                    <strong>{c.name}</strong>
                                    <br />
                                    <small>CIK: {c.cik}{c.tickers?.length > 0 ? ` · ${c.tickers.join(', ')}` : ''}</small>
                                </button>
                            </li>
                        ))}
                    </ul>

                    <button className="register-button-secondary" type="button" onClick={() => setScreen('portfolio')}>Back</button>
                </section>
            </main>
        )
    }

    if (screen === 'company-detail' && selectedCompany) {
        return (
            <main className="register-page" data-testid="company-detail-screen">
                <section className="register-card" style={{ maxWidth: '600px' }}>
                    <h1 data-testid="company-name">{selectedCompany.name}</h1>
                    <p>CIK: {selectedCompany.cik}</p>
                    {selectedCompany.tickers?.length > 0 && <p>Ticker: {selectedCompany.tickers.join(', ')}</p>}
                    <button className="register-button-secondary" type="button" onClick={() => setScreen('company-search')}>Back to search</button>
                </section>
            </main>
        )
    }

    if (screen === 'edit-position' && editingPosition) {
        return (
            <main className="register-page" data-testid="edit-position-screen">
                <section className="register-card">
                    <h1>Edit Position</h1>
                    <div className="form-group">
                        <label>Ticker</label>
                        <input
                            data-testid="edit-ticker-input"
                            value={editTicker}
                            onChange={(e) => setEditTicker(e.target.value)}
                        />
                    </div>
                    <div className="form-group">
                        <label>Quantity</label>
                        <input
                            data-testid="edit-quantity-input"
                            type="number"
                            min={1}
                            value={editQty}
                            onChange={(e) => setEditQty(e.target.value)}
                        />
                    </div>
                    <div className="form-group">
                        <label>Date</label>
                        <input
                            data-testid="edit-date-input"
                            type="date"
                            value={editDate}
                            onChange={(e) => setEditDate(e.target.value)}
                        />
                    </div>
                    {editError && <p className="register-error" role="alert">{editError}</p>}
                    <button
                        data-testid="save-position-button"
                        className="register-button"
                        type="button"
                        onClick={handleEditPosition}
                        disabled={editSaving}
                    >
                        {editSaving ? 'Saving…' : 'Save'}
                    </button>
                    <button
                        className="register-button-secondary"
                        type="button"
                        onClick={() => setScreen('portfolio')}
                    >
                        Cancel
                    </button>
                </section>
            </main>
        )
    }

    return (
        <main className="register-page" data-testid="portfolio-screen">
            <section className="register-card" style={{ maxWidth: '600px' }}>
                <p className="register-eyebrow">Financial App</p>
                <h1 data-testid="portfolio-screen-title">Portfolio</h1>
                <p className="register-description" data-testid="protected-screen-content">
                    Your current positions
                </p>

                {status.kind === 'loading' && (
                    <p data-testid="portfolio-loading">Loading…</p>
                )}

                {status.kind === 'error' && (
                    <p className="register-error" role="alert" data-testid="portfolio-error">
                        {status.message}
                    </p>
                )}

                {status.kind === 'success' && status.data.positions.length === 0 && (
                    <p data-testid="portfolio-empty" className="register-description">
                        No positions yet. Add one to get started.
                    </p>
                )}

                {status.kind === 'success' && status.data.positions.length > 0 && (
                    <ul data-testid="portfolio-positions" style={{ listStyle: 'none', padding: 0, width: '100%' }}>
                        {status.data.positions.map((pos) => (
                            <li
                                key={pos.id}
                                data-testid={`position-row-${pos.id}`}
                                style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '8px 0', borderBottom: '1px solid rgba(255,255,255,0.1)' }}
                            >
                                <span><strong>{pos.ticker}</strong> × {pos.quantity}</span>
                                <span style={{ display: 'flex', gap: '8px' }}>
                                    <button
                                        data-testid={`edit-position-${pos.id}`}
                                        type="button"
                                        onClick={() => startEdit(pos)}
                                        style={{ fontSize: '0.75rem' }}
                                    >
                                        Edit
                                    </button>
                                    <button
                                        data-testid={`remove-position-${pos.id}`}
                                        type="button"
                                        onClick={() => handleRemovePosition(pos.id)}
                                        style={{ fontSize: '0.75rem' }}
                                    >
                                        Remove
                                    </button>
                                </span>
                            </li>
                        ))}
                    </ul>
                )}

                <div style={{ display: 'flex', gap: '8px', marginTop: '16px', flexWrap: 'wrap' }}>
                    <button
                        data-testid="add-position-button"
                        className="register-button"
                        type="button"
                        onClick={() => setScreen('add-position')}
                        style={{ flex: 1 }}
                    >
                        Add Position
                    </button>
                    <button
                        data-testid="trade-button"
                        className="register-button"
                        type="button"
                        onClick={() => setScreen('trading')}
                        style={{ flex: 1 }}
                    >
                        Trade
                    </button>
                    <button
                        data-testid="research-button"
                        className="register-button"
                        type="button"
                        onClick={() => setScreen('company-search')}
                        style={{ flex: 1 }}
                    >
                        Research
                    </button>
                </div>

                <button
                    className="register-button-secondary"
                    type="button"
                    onClick={handleLogout}
                    data-testid="logout-button"
                    style={{ marginTop: '8px' }}
                >
                    Log out
                </button>
            </section>
        </main>
    )
}
