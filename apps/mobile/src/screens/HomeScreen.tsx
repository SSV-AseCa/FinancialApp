import { useCallback, useEffect, useState } from 'react'
import { usePortfolio, useAuth, useTrading, useCompany, useWatchlist } from '@ssv/ui-core'
import type {
    Portfolio,
    PortfolioValue,
    PortfolioPerformance,
    AddPositionInput,
    ModifyPositionInput,
    Position,
    Transaction,
    Company,
    SecFiling,
    HistoricalDataPoint,
    WatchlistCompany,
} from '@ssv/ui-core'
type HomeScreenProps = {
    onLogout: () => void
}

const formatCurrency = (value: number) =>
    new Intl.NumberFormat('en-US', {
        style: 'currency',
        currency: 'USD',
    }).format(value)

const formatSignedCurrency = (value: number) =>
    new Intl.NumberFormat('en-US', {
        style: 'currency',
        currency: 'USD',
        signDisplay: 'exceptZero',
    }).format(value)

const formatCompactCurrency = (value: number) =>
    new Intl.NumberFormat('en-US', {
        style: 'currency',
        currency: 'USD',
        notation: 'compact',
        maximumFractionDigits: 2,
    }).format(value)

const pnlDirection = (pnl: number) => (pnl > 0 ? 'gain' : pnl < 0 ? 'loss' : 'flat')

type AppScreen =
    | 'portfolio'
    | 'add-position'
    | 'edit-position'
    | 'trading'
    | 'company-search'
    | 'performance'
    | 'company-filings'
    | 'company-history'
    | 'watchlist'

type PortfolioStatus =
    | { kind: 'loading' }
    | { kind: 'success'; data: Portfolio }
    | { kind: 'error'; message: string }


type PortfolioValueStatus =
    | { kind: 'loading' }
    | { kind: 'success'; data: PortfolioValue }
    | { kind: 'error'; message: string }

type PerformanceStatus =
    | { kind: 'loading' }
    | { kind: 'success'; data: PortfolioPerformance }
    | { kind: 'error'; message: string }

type FilingsStatus =
    | { kind: 'loading' }
    | { kind: 'success'; data: SecFiling[] }
    | { kind: 'error'; message: string }

type HistoryStatus =
    | { kind: 'loading' }
    | { kind: 'success'; data: HistoricalDataPoint[] }
    | { kind: 'error'; message: string }

type WatchlistStatus =
    | { kind: 'loading' }
    | { kind: 'success'; data: WatchlistCompany[] }
    | { kind: 'error'; message: string }

type ComparisonStatus =
    | { kind: 'idle' }
    | { kind: 'loading' }
    | { kind: 'success'; data: WatchlistCompany[] }
    | { kind: 'error'; message: string }

export function HomeScreen({ onLogout }: HomeScreenProps) {
    const portfolio = usePortfolio()
    const auth = useAuth()
    const watchlist = useWatchlist()
    const [screen, setScreen] = useState<AppScreen>('portfolio')
    const [status, setStatus] = useState<PortfolioStatus>({ kind: 'loading' })
    const [portfolioValueStatus, setPortfolioValueStatus] = useState<PortfolioValueStatus>({ kind: 'loading' })
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

    // Portfolio performance metrics
    const [performanceStatus, setPerformanceStatus] = useState<PerformanceStatus>({ kind: 'loading' })

    // Company SEC filings + historical data
    const [filingsCompany, setFilingsCompany] = useState<Company | null>(null)
    const [filingsStatus, setFilingsStatus] = useState<FilingsStatus>({ kind: 'loading' })
    const [historyCompany, setHistoryCompany] = useState<Company | null>(null)
    const [historyStatus, setHistoryStatus] = useState<HistoryStatus>({ kind: 'loading' })

    // Watchlist
    const [watchlistStatus, setWatchlistStatus] = useState<WatchlistStatus>({ kind: 'loading' })
    const [watchlistError, setWatchlistError] = useState<string | null>(null)
    const [watchedCiks, setWatchedCiks] = useState<Record<string, boolean>>({})
    const [savingWatchlist, setSavingWatchlist] = useState<Record<string, boolean>>({})
    const [selectedCompareCiks, setSelectedCompareCiks] = useState<string[]>([])
    const [comparisonStatus, setComparisonStatus] = useState<ComparisonStatus>({ kind: 'idle' })

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
        setStatus({ kind: 'loading' })
        setPortfolioValueStatus({ kind: 'loading' })

        portfolio
            .fetchPortfolio()
            .then((data) => setStatus({ kind: 'success', data }))
            .catch((err: unknown) => {
                const message = err instanceof Error ? err.message : 'Failed to load portfolio.'
                setStatus({ kind: 'error', message })
            })

        portfolio
            .getPortfolioTotalValue()
            .then((data) => setPortfolioValueStatus({ kind: 'success', data }))
            .catch((err: unknown) => {
                const message = err instanceof Error ? err.message : 'Failed to load portfolio value.'
                setPortfolioValueStatus({ kind: 'error', message })
            })
    }, [portfolio])

    useEffect(() => {
        let cancelled = false

        portfolio
            .fetchPortfolio()
            .then((data) => {
                if (!cancelled) {
                    setStatus({ kind: 'success', data })
                }
            })
            .catch((err: unknown) => {
                if (!cancelled) {
                    const message = err instanceof Error ? err.message : 'Failed to load portfolio.'
                    setStatus({ kind: 'error', message })
                }
            })

        portfolio
            .getPortfolioTotalValue()
            .then((data) => {
                if (!cancelled) {
                    setPortfolioValueStatus({ kind: 'success', data })
                }
            })
            .catch((err: unknown) => {
                if (!cancelled) {
                    const message = err instanceof Error ? err.message : 'Failed to load portfolio value.'
                    setPortfolioValueStatus({ kind: 'error', message })
                }
            })

        return () => {
            cancelled = true
        }
    }, [portfolio])

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

    function openPerformance() {
        setScreen('performance')
        setPerformanceStatus({ kind: 'loading' })
        portfolio
            .getPortfolioPerformance()
            .then((data) => setPerformanceStatus({ kind: 'success', data }))
            .catch((err: unknown) => {
                const message = err instanceof Error ? err.message : 'Failed to load performance.'
                setPerformanceStatus({ kind: 'error', message })
            })
    }

    function openFilings(company: Company) {
        setFilingsCompany(company)
        setScreen('company-filings')
        setFilingsStatus({ kind: 'loading' })
        companyPort
            .getCompanySecFilings(company.cik)
            .then((data) => setFilingsStatus({ kind: 'success', data }))
            .catch((err: unknown) => {
                const message = err instanceof Error ? err.message : 'Failed to load filings.'
                setFilingsStatus({ kind: 'error', message })
            })
    }

    function openHistory(company: Company) {
        setHistoryCompany(company)
        setScreen('company-history')
        setHistoryStatus({ kind: 'loading' })
        companyPort
            .getCompanyHistoricalData(company.cik)
            .then((data) => setHistoryStatus({ kind: 'success', data }))
            .catch((err: unknown) => {
                const message = err instanceof Error ? err.message : 'Failed to load historical data.'
                setHistoryStatus({ kind: 'error', message })
            })
    }

    async function handleAddToWatchlist(cik: string) {
        setSavingWatchlist((prev) => ({ ...prev, [cik]: true }))
        setWatchlistError(null)
        try {
            await watchlist.addToWatchlist(cik)
            setWatchedCiks((prev) => ({ ...prev, [cik]: true }))
        } catch (err) {
            setWatchlistError(err instanceof Error ? err.message : 'Failed to add to watchlist.')
        } finally {
            setSavingWatchlist((prev) => ({ ...prev, [cik]: false }))
        }
    }

    function openWatchlist() {
        setScreen('watchlist')
        setWatchlistStatus({ kind: 'loading' })
        setWatchlistError(null)
        setSelectedCompareCiks([])
        setComparisonStatus({ kind: 'idle' })
        watchlist
            .getWatchlist()
            .then((data) => setWatchlistStatus({ kind: 'success', data }))
            .catch((err: unknown) => {
                const message = err instanceof Error ? err.message : 'Failed to load watchlist.'
                setWatchlistStatus({ kind: 'error', message })
            })
    }

    async function handleRemoveFromWatchlist(cik: string) {
        setWatchlistError(null)
        try {
            await watchlist.removeFromWatchlist(cik)
            setWatchlistStatus((prev) =>
                prev.kind === 'success'
                    ? { kind: 'success', data: prev.data.filter((c) => c.cik !== cik) }
                    : prev,
            )
            setSelectedCompareCiks((prev) => prev.filter((c) => c !== cik))
        } catch (err) {
            setWatchlistError(err instanceof Error ? err.message : 'Failed to remove from watchlist.')
        }
    }

    function toggleCompareSelect(cik: string) {
        setSelectedCompareCiks((prev) =>
            prev.includes(cik) ? prev.filter((c) => c !== cik) : [...prev, cik],
        )
    }

    async function handleCompare() {
        if (selectedCompareCiks.length < 2) return
        setComparisonStatus({ kind: 'loading' })
        try {
            const result = await watchlist.compareWatchlistCompanies(selectedCompareCiks)
            setComparisonStatus({ kind: 'success', data: result.companies })
        } catch (err) {
            const message = err instanceof Error ? err.message : 'Failed to compare companies.'
            setComparisonStatus({ kind: 'error', message })
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
                            <li
                                key={c.cik}
                                data-testid={`company-result-${c.cik}`}
                                style={{ padding: '8px 0', borderBottom: '1px solid rgba(255,255,255,0.1)' }}
                            >
                                <strong>{c.name}</strong>
                                <br />
                                <small>CIK: {c.cik}{c.tickers?.length > 0 ? ` · ${c.tickers.join(', ')}` : ''}</small>
                                <div style={{ display: 'flex', gap: '8px', marginTop: '8px', flexWrap: 'wrap' }}>
                                    <button
                                        data-testid={`view-filings-${c.cik}`}
                                        type="button"
                                        onClick={() => openFilings(c)}
                                        style={{ fontSize: '0.75rem' }}
                                    >
                                        Filings
                                    </button>
                                    <button
                                        data-testid={`view-history-${c.cik}`}
                                        type="button"
                                        onClick={() => openHistory(c)}
                                        style={{ fontSize: '0.75rem' }}
                                    >
                                        History
                                    </button>
                                    {watchedCiks[c.cik] ? (
                                        <span data-testid={`watching-badge-${c.cik}`} style={{ fontSize: '0.75rem' }}>
                                            Watching
                                        </span>
                                    ) : (
                                        <button
                                            data-testid={`add-watchlist-${c.cik}`}
                                            type="button"
                                            onClick={() => handleAddToWatchlist(c.cik)}
                                            disabled={savingWatchlist[c.cik]}
                                            style={{ fontSize: '0.75rem' }}
                                        >
                                            {savingWatchlist[c.cik] ? 'Adding…' : 'Watch'}
                                        </button>
                                    )}
                                </div>
                            </li>
                        ))}
                    </ul>

                    {watchlistError && (
                        <p className="register-error" role="alert" data-testid="watchlist-error">
                            {watchlistError}
                        </p>
                    )}

                    <button className="register-button-secondary" type="button" onClick={() => setScreen('portfolio')}>Back</button>
                </section>
            </main>
        )
    }

    if (screen === 'performance') {
        return (
            <main className="register-page" data-testid="performance-screen">
                <section className="register-card" style={{ maxWidth: '600px' }}>
                    <h1>Portfolio Performance</h1>
                    <div
                        data-testid="performance-metrics-panel"
                        style={{
                            width: '100%',
                            padding: '12px',
                            marginBottom: '16px',
                            border: '1px solid rgba(255,255,255,0.1)',
                            borderRadius: '8px',
                            textAlign: 'left',
                        }}
                    >
                        {performanceStatus.kind === 'loading' && (
                            <p data-testid="performance-loading">Loading…</p>
                        )}

                        {performanceStatus.kind === 'error' && (
                            <p className="register-error" role="alert" data-testid="performance-error">
                                {performanceStatus.message}
                            </p>
                        )}

                        {performanceStatus.kind === 'success' && (
                            <>
                                <p className="register-description" style={{ margin: 0 }}>
                                    Total Value
                                </p>
                                <strong data-testid="performance-total-value">
                                    {formatCurrency(performanceStatus.data.totalValue)}
                                </strong>
                                <p className="register-description" style={{ margin: '8px 0 0' }}>
                                    Total P&L
                                </p>
                                <strong data-testid="performance-total-pnl">
                                    {formatSignedCurrency(performanceStatus.data.totalPnL)}
                                </strong>
                            </>
                        )}
                    </div>
                    <button className="register-button-secondary" type="button" onClick={() => setScreen('portfolio')}>Back</button>
                </section>
            </main>
        )
    }

    if (screen === 'company-filings') {
        return (
            <main className="register-page" data-testid="company-filings-screen">
                <section className="register-card" style={{ maxWidth: '600px' }}>
                    <h1>SEC Filings</h1>
                    <p className="register-description" data-testid="company-name">
                        {filingsCompany?.name ?? ''}
                    </p>

                    {filingsStatus.kind === 'loading' && (
                        <p data-testid="filings-loading">Loading…</p>
                    )}

                    {filingsStatus.kind === 'error' && (
                        <p className="register-error" role="alert" data-testid="filings-error">
                            {filingsStatus.message}
                        </p>
                    )}

                    {filingsStatus.kind === 'success' && filingsStatus.data.length === 0 && (
                        <p data-testid="filings-empty" className="register-description">
                            No filings found.
                        </p>
                    )}

                    {filingsStatus.kind === 'success' && filingsStatus.data.length > 0 && (
                        <ul data-testid="filings-list" style={{ listStyle: 'none', padding: 0, width: '100%' }}>
                            {filingsStatus.data.map((filing, index) => (
                                <li
                                    key={index}
                                    data-testid={`filing-row-${index}`}
                                    style={{ padding: '8px 0', borderBottom: '1px solid rgba(255,255,255,0.1)', textAlign: 'left' }}
                                >
                                    <strong data-testid="filing-form-type">{filing.formType}</strong>
                                    <br />
                                    <small data-testid="filing-date">{filing.filingDate}</small>
                                    {filing.description && (
                                        <>
                                            <br />
                                            <small>{filing.description}</small>
                                        </>
                                    )}
                                </li>
                            ))}
                        </ul>
                    )}

                    <button className="register-button-secondary" type="button" onClick={() => setScreen('company-search')}>Back</button>
                </section>
            </main>
        )
    }

    if (screen === 'company-history') {
        return (
            <main className="register-page" data-testid="company-history-screen">
                <section className="register-card" style={{ maxWidth: '600px' }}>
                    <h1>Historical Financials</h1>
                    <p className="register-description" data-testid="company-name">
                        {historyCompany?.name ?? ''}
                    </p>

                    {historyStatus.kind === 'loading' && (
                        <p data-testid="history-loading">Loading…</p>
                    )}

                    {historyStatus.kind === 'error' && (
                        <p className="register-error" role="alert" data-testid="history-error">
                            {historyStatus.message}
                        </p>
                    )}

                    {historyStatus.kind === 'success' && historyStatus.data.length === 0 && (
                        <p data-testid="history-empty" className="register-description">
                            No historical data found.
                        </p>
                    )}

                    {historyStatus.kind === 'success' && historyStatus.data.length > 0 && (
                        <table data-testid="trend-table" style={{ width: '100%', borderCollapse: 'collapse', fontSize: '0.85rem' }}>
                            <thead>
                                <tr>
                                    <th style={{ textAlign: 'left' }}>Period</th>
                                    <th style={{ textAlign: 'right' }}>Revenue</th>
                                    <th style={{ textAlign: 'right' }}>Net Income</th>
                                    <th style={{ textAlign: 'right' }}>Assets</th>
                                    <th style={{ textAlign: 'right' }}>Equity</th>
                                </tr>
                            </thead>
                            <tbody>
                                {historyStatus.data.map((point, index) => (
                                    <tr key={index} data-testid={`history-row-${index}`}>
                                        <td data-testid="history-cell-period" style={{ textAlign: 'left' }}>{point.period}</td>
                                        <td data-testid="history-cell-revenue" style={{ textAlign: 'right' }}>{formatCompactCurrency(point.revenue)}</td>
                                        <td data-testid="history-cell-net-income" style={{ textAlign: 'right' }}>{formatCompactCurrency(point.netIncome)}</td>
                                        <td data-testid="history-cell-assets" style={{ textAlign: 'right' }}>{formatCompactCurrency(point.assets)}</td>
                                        <td data-testid="history-cell-equity" style={{ textAlign: 'right' }}>{formatCompactCurrency(point.equity)}</td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    )}

                    <button className="register-button-secondary" type="button" onClick={() => setScreen('company-search')}>Back</button>
                </section>
            </main>
        )
    }

    if (screen === 'watchlist') {
        return (
            <main className="register-page" data-testid="watchlist-screen">
                <section className="register-card" style={{ maxWidth: '600px' }}>
                    <h1>Watchlist</h1>

                    {watchlistError && (
                        <p className="register-error" role="alert" data-testid="watchlist-error">
                            {watchlistError}
                        </p>
                    )}

                    {watchlistStatus.kind === 'loading' && (
                        <p data-testid="watchlist-loading">Loading…</p>
                    )}

                    {watchlistStatus.kind === 'error' && (
                        <p className="register-error" role="alert" data-testid="watchlist-error">
                            {watchlistStatus.message}
                        </p>
                    )}

                    {watchlistStatus.kind === 'success' && watchlistStatus.data.length === 0 && (
                        <p data-testid="watchlist-empty" className="register-description">
                            Your watchlist is empty.
                        </p>
                    )}

                    {comparisonStatus.kind === 'loading' && (
                        <p data-testid="comparison-loading">Loading…</p>
                    )}

                    {comparisonStatus.kind === 'error' && (
                        <p className="register-error" role="alert" data-testid="watchlist-error">
                            {comparisonStatus.message}
                        </p>
                    )}

                    {comparisonStatus.kind === 'success' && (
                        <div data-testid="comparison-view" style={{ width: '100%', marginBottom: '16px' }}>
                            <h2>Comparison</h2>
                            {comparisonStatus.data.map((c) => (
                                <div
                                    key={c.cik}
                                    style={{ padding: '8px 0', borderBottom: '1px solid rgba(255,255,255,0.1)', textAlign: 'left' }}
                                >
                                    <strong>{c.name}</strong>
                                    <br />
                                    <small data-testid={`compare-revenue-${c.cik}`}>Revenue: {formatCompactCurrency(c.metrics.revenue)}</small>
                                    <br />
                                    <small data-testid={`compare-net-income-${c.cik}`}>Net Income: {formatCompactCurrency(c.metrics.netIncome)}</small>
                                    <br />
                                    <small data-testid={`compare-assets-${c.cik}`}>Assets: {formatCompactCurrency(c.metrics.assets)}</small>
                                    <br />
                                    <small data-testid={`compare-equity-${c.cik}`}>Equity: {formatCompactCurrency(c.metrics.equity)}</small>
                                </div>
                            ))}
                            <button
                                data-testid="close-comparison"
                                className="register-button-secondary"
                                type="button"
                                onClick={() => setComparisonStatus({ kind: 'idle' })}
                            >
                                Close
                            </button>
                        </div>
                    )}

                    {watchlistStatus.kind === 'success' && watchlistStatus.data.length > 0 && comparisonStatus.kind !== 'success' && (
                        <>
                            <ul style={{ listStyle: 'none', padding: 0, width: '100%' }}>
                                {watchlistStatus.data.map((c) => (
                                    <li
                                        key={c.cik}
                                        data-testid={`watchlist-item-${c.cik}`}
                                        style={{ padding: '8px 0', borderBottom: '1px solid rgba(255,255,255,0.1)', textAlign: 'left' }}
                                    >
                                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                            <span>
                                                <input
                                                    data-testid={`compare-select-${c.cik}`}
                                                    type="checkbox"
                                                    checked={selectedCompareCiks.includes(c.cik)}
                                                    onChange={() => toggleCompareSelect(c.cik)}
                                                />{' '}
                                                <strong>{c.name}</strong>
                                            </span>
                                            <button
                                                data-testid={`remove-watchlist-${c.cik}`}
                                                type="button"
                                                onClick={() => handleRemoveFromWatchlist(c.cik)}
                                                style={{ fontSize: '0.75rem' }}
                                            >
                                                Remove
                                            </button>
                                        </div>
                                        <small data-testid={`watchlist-metric-revenue-${c.cik}`}>Revenue: {formatCompactCurrency(c.metrics.revenue)}</small>
                                        <br />
                                        <small
                                            data-testid={`watchlist-metric-net-income-${c.cik}`}
                                            style={{ color: c.metrics.netIncome >= 0 ? 'green' : 'red' }}
                                        >
                                            Net Income: {formatCompactCurrency(c.metrics.netIncome)}
                                        </small>
                                        <br />
                                        <small data-testid={`watchlist-metric-assets-${c.cik}`}>Assets: {formatCompactCurrency(c.metrics.assets)}</small>
                                        <br />
                                        <small data-testid={`watchlist-metric-equity-${c.cik}`}>Equity: {formatCompactCurrency(c.metrics.equity)}</small>
                                    </li>
                                ))}
                            </ul>
                            <button
                                data-testid="compare-button"
                                className="register-button"
                                type="button"
                                onClick={handleCompare}
                                disabled={selectedCompareCiks.length < 2}
                            >
                                Compare
                            </button>
                        </>
                    )}

                    <button className="register-button-secondary" type="button" onClick={() => setScreen('portfolio')}>Back</button>
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

                <div
                    data-testid="portfolio-total-value-card"
                    style={{
                        width: '100%',
                        padding: '12px',
                        marginBottom: '16px',
                        border: '1px solid rgba(255,255,255,0.1)',
                        borderRadius: '8px',
                        textAlign: 'left',
                    }}
                >
                    <p className="register-description" style={{ margin: 0 }}>
                        Total Portfolio Value
                    </p>

                    {portfolioValueStatus.kind === 'loading' && (
                        <strong data-testid="portfolio-total-value-loading">Loading…</strong>
                    )}

                    {portfolioValueStatus.kind === 'error' && (
                        <p className="register-error" role="alert" data-testid="portfolio-total-value-error">
                            {portfolioValueStatus.message}
                        </p>
                    )}

                    {portfolioValueStatus.kind === 'success' && (
                        <strong data-testid="portfolio-total-value">
                            {formatCurrency(portfolioValueStatus.data.totalValue)}
                        </strong>
                    )}
                </div>

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
                                <span>
                                    <strong>{pos.ticker}</strong> × {pos.quantity}
                                    <br />
                                    <small
                                        data-testid={`position-pnl-${pos.id}`}
                                        data-pnl-direction={pnlDirection(pos.pnl)}
                                    >
                                        {formatSignedCurrency(pos.pnl)} ({pos.pnlPercent}%)
                                    </small>
                                </span>
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
                    <button
                        data-testid="nav-performance"
                        className="register-button"
                        type="button"
                        onClick={openPerformance}
                        style={{ flex: 1 }}
                    >
                        Performance
                    </button>
                    <button
                        data-testid="nav-watchlist"
                        className="register-button"
                        type="button"
                        onClick={openWatchlist}
                        style={{ flex: 1 }}
                    >
                        Watchlist
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
