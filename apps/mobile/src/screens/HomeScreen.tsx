import { useCallback, useEffect, useState } from 'react'
import type { InputHTMLAttributes, ReactNode } from 'react'
import { usePortfolio, useAuth, useTrading, useCompany, useWatchlist } from '@ssv/ui-core'
import {
    Page,
    Navbar,
    NavbarBackLink,
    Block,
    BlockTitle,
    List,
    Card,
    Button,
} from 'konsta/react'
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
    CompanyFinancialMetrics,
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

const formatMetricValue = (value: number, unit: string) => {
    if (unit === 'USD') {
        return formatCompactCurrency(value)
    }
    const compact = new Intl.NumberFormat('en-US', {
        notation: 'compact',
        maximumFractionDigits: 2,
    }).format(value)
    return unit ? `${compact} ${unit}` : compact
}

// ── Small presentational helpers (Konsta-styled) ──────────────────────────

type FieldProps = {
    label: string
    testid: string
} & InputHTMLAttributes<HTMLInputElement>

function Field({ label, testid, ...inputProps }: FieldProps) {
    return (
        <li className="list-none px-4 py-2.5 [&:not(:last-child)]:border-b [&:not(:last-child)]:border-white/10">
            <label className="flex flex-col gap-1">
                <span className="text-xs font-medium uppercase tracking-wide text-white/45">
                    {label}
                </span>
                <input
                    data-testid={testid}
                    className="w-full bg-transparent text-base text-white outline-none placeholder:text-white/30"
                    {...inputProps}
                />
            </label>
        </li>
    )
}

function ErrorText({ children, testid }: { children: ReactNode; testid?: string }) {
    return (
        <p role="alert" data-testid={testid} className="m-0 text-sm text-red-400">
            {children}
        </p>
    )
}

function SubScreen({
    testid,
    title,
    titleTestId,
    onBack,
    backLabel = 'Back',
    right,
    children,
}: {
    testid: string
    title: string
    titleTestId?: string
    onBack?: () => void
    backLabel?: string
    right?: ReactNode
    children: ReactNode
}) {
    return (
        <Page data-testid={testid}>
            <Navbar
                title={
                    titleTestId ? <span data-testid={titleTestId}>{title}</span> : title
                }
                left={onBack ? <NavbarBackLink text={backLabel} onClick={onBack} /> : undefined}
                right={right}
            />
            {children}
        </Page>
    )
}

type AppScreen =
    | 'portfolio'
    | 'add-position'
    | 'edit-position'
    | 'trading'
    | 'company-search'
    | 'performance'
    | 'company-filings'
    | 'company-metrics'
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

type MetricsStatus =
    | { kind: 'loading' }
    | { kind: 'success'; data: CompanyFinancialMetrics[] }
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

    // Company SEC filings + financial metrics + historical data
    const [filingsCompany, setFilingsCompany] = useState<Company | null>(null)
    const [filingsStatus, setFilingsStatus] = useState<FilingsStatus>({ kind: 'loading' })
    const [metricsCompany, setMetricsCompany] = useState<Company | null>(null)
    const [metricsStatus, setMetricsStatus] = useState<MetricsStatus>({ kind: 'loading' })
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
            <SubScreen
                testid="add-position-screen"
                title="Add Position"
                onBack={() => setScreen('portfolio')}
                backLabel="Cancel"
            >
                <List strong inset>
                    <Field
                        label="Ticker"
                        testid="add-ticker-input"
                        value={addTicker}
                        onChange={(e) => setAddTicker(e.target.value)}
                        placeholder="e.g. AAPL"
                    />
                    <Field
                        label="Quantity"
                        testid="add-quantity-input"
                        type="number"
                        min={1}
                        value={addQty}
                        onChange={(e) => setAddQty(e.target.value)}
                    />
                    <Field
                        label="Date"
                        testid="add-date-input"
                        type="date"
                        value={addDate}
                        onChange={(e) => setAddDate(e.target.value)}
                    />
                </List>
                {addError && (
                    <Block className="-mt-2">
                        <ErrorText>{addError}</ErrorText>
                    </Block>
                )}
                <Block className="flex flex-col gap-2">
                    <Button
                        data-testid="confirm-add-position-button"
                        onClick={handleAddPosition}
                        disabled={addSaving}
                    >
                        {addSaving ? 'Adding…' : 'Add Position'}
                    </Button>
                    <Button clear onClick={() => setScreen('portfolio')}>
                        Cancel
                    </Button>
                </Block>
            </SubScreen>
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

    function openMetrics(company: Company) {
        setMetricsCompany(company)
        setScreen('company-metrics')
        setMetricsStatus({ kind: 'loading' })
        companyPort
            .getCompanyFinancialMetrics(company.cik)
            .then((data) => setMetricsStatus({ kind: 'success', data }))
            .catch((err: unknown) => {
                const message = err instanceof Error ? err.message : 'Failed to load financial metrics.'
                setMetricsStatus({ kind: 'error', message })
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
            <SubScreen
                testid="trading-screen"
                title="Trading"
                onBack={() => setScreen('portfolio')}
            >
                <BlockTitle>Buy Shares</BlockTitle>
                <List strong inset>
                    <Field
                        label="CIK"
                        testid="buy-cik-input"
                        value={buyCik}
                        onChange={(e) => setBuyCik(e.target.value)}
                        placeholder="e.g. 0000320193"
                    />
                    <Field
                        label="Quantity"
                        testid="buy-quantity-input"
                        type="number"
                        min={1}
                        value={buyQty}
                        onChange={(e) => setBuyQty(e.target.value)}
                    />
                </List>
                {buyError && (
                    <Block className="-mt-2">
                        <ErrorText>{buyError}</ErrorText>
                    </Block>
                )}
                <Block className="-mt-2">
                    <Button data-testid="buy-submit-button" onClick={handleBuy} disabled={buySaving}>
                        {buySaving ? 'Buying…' : 'Buy'}
                    </Button>
                </Block>

                <BlockTitle>Sell Shares</BlockTitle>
                <List strong inset>
                    <Field
                        label="CIK"
                        testid="sell-cik-input"
                        value={sellCik}
                        onChange={(e) => setSellCik(e.target.value)}
                        placeholder="e.g. 0000320193"
                    />
                    <Field
                        label="Quantity"
                        testid="sell-quantity-input"
                        type="number"
                        min={1}
                        value={sellQty}
                        onChange={(e) => setSellQty(e.target.value)}
                    />
                </List>
                {sellError && (
                    <Block className="-mt-2">
                        <ErrorText>{sellError}</ErrorText>
                    </Block>
                )}
                <Block className="-mt-2">
                    <Button data-testid="sell-submit-button" onClick={handleSell} disabled={sellSaving}>
                        {sellSaving ? 'Selling…' : 'Sell'}
                    </Button>
                </Block>

                <BlockTitle>Transaction History</BlockTitle>
                {txLoading && <Block className="-mt-2 text-white/60">Loading…</Block>}
                {txHistory.length === 0 && !txLoading && (
                    <Block className="-mt-2 text-white/60" data-testid="no-transactions">
                        No transactions yet.
                    </Block>
                )}
                <List strong inset data-testid="transactions-list">
                    {txHistory.map((tx) => (
                        <li
                            key={tx.id}
                            data-testid={`transaction-${tx.id}`}
                            className="list-none px-4 py-2.5 text-sm text-white [&:not(:last-child)]:border-b [&:not(:last-child)]:border-white/10"
                        >
                            {tx.type} · {tx.quantity} shares · {tx.cik} · {tx.transactionDate}
                        </li>
                    ))}
                </List>
            </SubScreen>
        )
    }

    if (screen === 'company-search') {
        return (
            <SubScreen
                testid="company-search-screen"
                title="Company Research"
                onBack={() => setScreen('portfolio')}
            >
                <List strong inset>
                    <Field
                        label="Search"
                        testid="company-search-input"
                        value={searchQuery}
                        onChange={(e) => setSearchQuery(e.target.value)}
                        onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
                        placeholder="e.g. Apple or AAPL"
                    />
                </List>
                {searchError && (
                    <Block className="-mt-2">
                        <ErrorText>{searchError}</ErrorText>
                    </Block>
                )}
                <Block className="-mt-2">
                    <Button
                        data-testid="company-search-submit"
                        onClick={handleSearch}
                        disabled={searchLoading}
                    >
                        {searchLoading ? 'Searching…' : 'Search'}
                    </Button>
                </Block>

                {searchResults.length === 0 && !searchLoading && searchQuery && (
                    <Block className="-mt-2 text-white/60" data-testid="no-results">
                        No results found.
                    </Block>
                )}

                <List strong inset data-testid="company-search-results">
                    {searchResults.map((c) => (
                        <li
                            key={c.cik}
                            data-testid={`company-result-${c.cik}`}
                            className="list-none px-4 py-3 text-left [&:not(:last-child)]:border-b [&:not(:last-child)]:border-white/10"
                        >
                            <strong className="text-white">{c.name}</strong>
                            <div className="text-xs text-white/50">
                                CIK: {c.cik}
                                {c.tickers?.length > 0 ? ` · ${c.tickers.join(', ')}` : ''}
                            </div>
                            <div className="mt-2 flex flex-wrap gap-2">
                                <Button small inline tonal data-testid={`view-filings-${c.cik}`} onClick={() => openFilings(c)}>
                                    Filings
                                </Button>
                                <Button small inline tonal data-testid={`view-metrics-${c.cik}`} onClick={() => openMetrics(c)}>
                                    Metrics
                                </Button>
                                <Button small inline tonal data-testid={`view-history-${c.cik}`} onClick={() => openHistory(c)}>
                                    History
                                </Button>
                                {watchedCiks[c.cik] ? (
                                    <span
                                        data-testid={`watching-badge-${c.cik}`}
                                        className="inline-flex items-center text-xs font-medium text-emerald-400"
                                    >
                                        Watching
                                    </span>
                                ) : (
                                    <Button
                                        small
                                        inline
                                        data-testid={`add-watchlist-${c.cik}`}
                                        onClick={() => handleAddToWatchlist(c.cik)}
                                        disabled={savingWatchlist[c.cik]}
                                    >
                                        {savingWatchlist[c.cik] ? 'Adding…' : 'Watch'}
                                    </Button>
                                )}
                            </div>
                        </li>
                    ))}
                </List>

                {watchlistError && (
                    <Block className="-mt-2">
                        <ErrorText testid="watchlist-error">{watchlistError}</ErrorText>
                    </Block>
                )}
            </SubScreen>
        )
    }

    if (screen === 'performance') {
        return (
            <SubScreen
                testid="performance-screen"
                title="Portfolio Performance"
                onBack={() => setScreen('portfolio')}
            >
                <Block>
                    <Card data-testid="performance-metrics-panel" className="text-left">
                        {performanceStatus.kind === 'loading' && (
                            <p className="m-0 text-white/60" data-testid="performance-loading">Loading…</p>
                        )}

                        {performanceStatus.kind === 'error' && (
                            <ErrorText testid="performance-error">{performanceStatus.message}</ErrorText>
                        )}

                        {performanceStatus.kind === 'success' && (
                            <>
                                <p className="m-0 text-sm text-white/60">Total Value</p>
                                <strong data-testid="performance-total-value" className="text-xl text-white">
                                    {formatCurrency(performanceStatus.data.totalValue)}
                                </strong>
                                <p className="mt-2 mb-0 text-sm text-white/60">Total P&amp;L</p>
                                <strong data-testid="performance-total-pnl" className="text-xl text-white">
                                    {formatSignedCurrency(performanceStatus.data.totalPnL)}
                                </strong>
                            </>
                        )}
                    </Card>
                </Block>
            </SubScreen>
        )
    }

    if (screen === 'company-filings') {
        return (
            <SubScreen
                testid="company-filings-screen"
                title="SEC Filings"
                onBack={() => setScreen('company-search')}
            >
                <Block className="text-white/60" data-testid="company-name">
                    {filingsCompany?.name ?? ''}
                </Block>

                {filingsStatus.kind === 'loading' && (
                    <Block className="-mt-2 text-white/60" data-testid="filings-loading">Loading…</Block>
                )}

                {filingsStatus.kind === 'error' && (
                    <Block className="-mt-2">
                        <ErrorText testid="filings-error">{filingsStatus.message}</ErrorText>
                    </Block>
                )}

                {filingsStatus.kind === 'success' && filingsStatus.data.length === 0 && (
                    <Block className="-mt-2 text-white/60" data-testid="filings-empty">No filings found.</Block>
                )}

                {filingsStatus.kind === 'success' && filingsStatus.data.length > 0 && (
                    <List strong inset data-testid="filings-list">
                        {filingsStatus.data.map((filing, index) => (
                            <li
                                key={index}
                                data-testid={`filing-row-${index}`}
                                className="list-none px-4 py-3 text-left [&:not(:last-child)]:border-b [&:not(:last-child)]:border-white/10"
                            >
                                <strong data-testid="filing-form-type" className="text-white">{filing.formType}</strong>
                                <div data-testid="filing-date" className="text-xs text-white/50">{filing.filingDate}</div>
                                {filing.description && (
                                    <div className="text-xs text-white/50">{filing.description}</div>
                                )}
                            </li>
                        ))}
                    </List>
                )}
            </SubScreen>
        )
    }

    if (screen === 'company-metrics') {
        return (
            <SubScreen
                testid="company-metrics-screen"
                title="Financial Metrics"
                onBack={() => setScreen('company-search')}
            >
                <Block className="text-white/60" data-testid="company-name">
                    {metricsCompany?.name ?? ''}
                </Block>

                {metricsStatus.kind === 'loading' && (
                    <Block className="-mt-2 text-white/60" data-testid="metrics-loading">Loading…</Block>
                )}

                {metricsStatus.kind === 'error' && (
                    <Block className="-mt-2">
                        <ErrorText testid="metrics-error">{metricsStatus.message}</ErrorText>
                    </Block>
                )}

                {metricsStatus.kind === 'success' && metricsStatus.data.length === 0 && (
                    <Block className="-mt-2 text-white/60" data-testid="metrics-empty">No metrics found.</Block>
                )}

                {metricsStatus.kind === 'success' && metricsStatus.data.length > 0 && (
                    <List strong inset data-testid="metrics-list">
                        {metricsStatus.data.map((metric, index) => (
                            <li
                                key={index}
                                data-testid={`metric-card-${index}`}
                                className="list-none px-4 py-3 text-left [&:not(:last-child)]:border-b [&:not(:last-child)]:border-white/10"
                            >
                                <strong data-testid="metric-name" className="text-white">{metric.metric}</strong>
                                <div data-testid="metric-value" className="text-sm text-white/70">
                                    {formatMetricValue(metric.value, metric.unit)}
                                </div>
                                {metric.periodEnd && (
                                    <div className="text-xs text-white/45">As of {metric.periodEnd}</div>
                                )}
                            </li>
                        ))}
                    </List>
                )}
            </SubScreen>
        )
    }

    if (screen === 'company-history') {
        return (
            <SubScreen
                testid="company-history-screen"
                title="Historical Financials"
                onBack={() => setScreen('company-search')}
            >
                <Block className="text-white/60" data-testid="company-name">
                    {historyCompany?.name ?? ''}
                </Block>

                {historyStatus.kind === 'loading' && (
                    <Block className="-mt-2 text-white/60" data-testid="history-loading">Loading…</Block>
                )}

                {historyStatus.kind === 'error' && (
                    <Block className="-mt-2">
                        <ErrorText testid="history-error">{historyStatus.message}</ErrorText>
                    </Block>
                )}

                {historyStatus.kind === 'success' && historyStatus.data.length === 0 && (
                    <Block className="-mt-2 text-white/60" data-testid="history-empty">No historical data found.</Block>
                )}

                {historyStatus.kind === 'success' && historyStatus.data.length > 0 && (
                    <Block>
                        <Card className="overflow-x-auto">
                            <table data-testid="trend-table" className="w-full border-collapse text-sm text-white">
                                <thead>
                                    <tr className="text-white/60">
                                        <th className="text-left font-medium">Period</th>
                                        <th className="text-right font-medium">Revenue</th>
                                        <th className="text-right font-medium">Net Income</th>
                                        <th className="text-right font-medium">Assets</th>
                                        <th className="text-right font-medium">Equity</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {historyStatus.data.map((point, index) => (
                                        <tr key={index} data-testid={`history-row-${index}`}>
                                            <td data-testid="history-cell-period" className="text-left">{point.period}</td>
                                            <td data-testid="history-cell-revenue" className="text-right">{formatCompactCurrency(point.revenue)}</td>
                                            <td data-testid="history-cell-net-income" className="text-right">{formatCompactCurrency(point.netIncome)}</td>
                                            <td data-testid="history-cell-assets" className="text-right">{formatCompactCurrency(point.assets)}</td>
                                            <td data-testid="history-cell-equity" className="text-right">{formatCompactCurrency(point.equity)}</td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </Card>
                    </Block>
                )}
            </SubScreen>
        )
    }

    if (screen === 'watchlist') {
        return (
            <SubScreen
                testid="watchlist-screen"
                title="Watchlist"
                onBack={() => setScreen('portfolio')}
            >
                {watchlistError && (
                    <Block><ErrorText testid="watchlist-error">{watchlistError}</ErrorText></Block>
                )}

                {watchlistStatus.kind === 'loading' && (
                    <Block className="text-white/60" data-testid="watchlist-loading">Loading…</Block>
                )}

                {watchlistStatus.kind === 'error' && (
                    <Block><ErrorText testid="watchlist-error">{watchlistStatus.message}</ErrorText></Block>
                )}

                {watchlistStatus.kind === 'success' && watchlistStatus.data.length === 0 && (
                    <Block className="text-white/60" data-testid="watchlist-empty">Your watchlist is empty.</Block>
                )}

                {comparisonStatus.kind === 'loading' && (
                    <Block className="text-white/60" data-testid="comparison-loading">Loading…</Block>
                )}

                {comparisonStatus.kind === 'error' && (
                    <Block><ErrorText testid="watchlist-error">{comparisonStatus.message}</ErrorText></Block>
                )}

                {comparisonStatus.kind === 'success' && (
                    <div data-testid="comparison-view">
                        <BlockTitle>Comparison</BlockTitle>
                        <List strong inset>
                            {comparisonStatus.data.map((c) => (
                                <li
                                    key={c.cik}
                                    className="list-none px-4 py-3 text-left [&:not(:last-child)]:border-b [&:not(:last-child)]:border-white/10"
                                >
                                    <strong className="text-white">{c.name}</strong>
                                    <div data-testid={`compare-revenue-${c.cik}`} className="text-xs text-white/55">Revenue: {formatCompactCurrency(c.metrics.revenue)}</div>
                                    <div data-testid={`compare-net-income-${c.cik}`} className="text-xs text-white/55">Net Income: {formatCompactCurrency(c.metrics.netIncome)}</div>
                                    <div data-testid={`compare-assets-${c.cik}`} className="text-xs text-white/55">Assets: {formatCompactCurrency(c.metrics.assets)}</div>
                                    <div data-testid={`compare-equity-${c.cik}`} className="text-xs text-white/55">Equity: {formatCompactCurrency(c.metrics.equity)}</div>
                                </li>
                            ))}
                        </List>
                        <Block className="-mt-2">
                            <Button
                                clear
                                data-testid="close-comparison"
                                onClick={() => setComparisonStatus({ kind: 'idle' })}
                            >
                                Close
                            </Button>
                        </Block>
                    </div>
                )}

                {watchlistStatus.kind === 'success' && watchlistStatus.data.length > 0 && comparisonStatus.kind !== 'success' && (
                    <>
                        <List strong inset>
                            {watchlistStatus.data.map((c) => (
                                <li
                                    key={c.cik}
                                    data-testid={`watchlist-item-${c.cik}`}
                                    className="list-none px-4 py-3 text-left [&:not(:last-child)]:border-b [&:not(:last-child)]:border-white/10"
                                >
                                    <div className="flex items-center justify-between">
                                        <span className="flex items-center gap-2 text-white">
                                            <input
                                                data-testid={`compare-select-${c.cik}`}
                                                type="checkbox"
                                                checked={selectedCompareCiks.includes(c.cik)}
                                                onChange={() => toggleCompareSelect(c.cik)}
                                            />
                                            <strong>{c.name}</strong>
                                        </span>
                                        <Button
                                            small
                                            inline
                                            clear
                                            data-testid={`remove-watchlist-${c.cik}`}
                                            onClick={() => handleRemoveFromWatchlist(c.cik)}
                                        >
                                            Remove
                                        </Button>
                                    </div>
                                    <div data-testid={`watchlist-metric-revenue-${c.cik}`} className="text-xs text-white/55">Revenue: {formatCompactCurrency(c.metrics.revenue)}</div>
                                    <div
                                        data-testid={`watchlist-metric-net-income-${c.cik}`}
                                        className="text-xs"
                                        style={{ color: c.metrics.netIncome >= 0 ? '#34d399' : '#f87171' }}
                                    >
                                        Net Income: {formatCompactCurrency(c.metrics.netIncome)}
                                    </div>
                                    <div data-testid={`watchlist-metric-assets-${c.cik}`} className="text-xs text-white/55">Assets: {formatCompactCurrency(c.metrics.assets)}</div>
                                    <div data-testid={`watchlist-metric-equity-${c.cik}`} className="text-xs text-white/55">Equity: {formatCompactCurrency(c.metrics.equity)}</div>
                                </li>
                            ))}
                        </List>
                        <Block className="-mt-2">
                            <Button
                                data-testid="compare-button"
                                onClick={handleCompare}
                                disabled={selectedCompareCiks.length < 2}
                            >
                                Compare
                            </Button>
                        </Block>
                    </>
                )}
            </SubScreen>
        )
    }

    if (screen === 'edit-position' && editingPosition) {
        return (
            <SubScreen
                testid="edit-position-screen"
                title="Edit Position"
                onBack={() => setScreen('portfolio')}
                backLabel="Cancel"
            >
                <List strong inset>
                    <Field
                        label="Ticker"
                        testid="edit-ticker-input"
                        value={editTicker}
                        onChange={(e) => setEditTicker(e.target.value)}
                    />
                    <Field
                        label="Quantity"
                        testid="edit-quantity-input"
                        type="number"
                        min={1}
                        value={editQty}
                        onChange={(e) => setEditQty(e.target.value)}
                    />
                    <Field
                        label="Date"
                        testid="edit-date-input"
                        type="date"
                        value={editDate}
                        onChange={(e) => setEditDate(e.target.value)}
                    />
                </List>
                {editError && (
                    <Block className="-mt-2"><ErrorText>{editError}</ErrorText></Block>
                )}
                <Block className="flex flex-col gap-2">
                    <Button
                        data-testid="save-position-button"
                        onClick={handleEditPosition}
                        disabled={editSaving}
                    >
                        {editSaving ? 'Saving…' : 'Save'}
                    </Button>
                    <Button clear onClick={() => setScreen('portfolio')}>Cancel</Button>
                </Block>
            </SubScreen>
        )
    }

    return (
        <Page data-testid="portfolio-screen">
            <Navbar
                title={<span data-testid="portfolio-screen-title">Portfolio</span>}
                subtitle="Financial App"
            />

            <Block className="-mb-2">
                <p className="m-0 text-sm text-white/60" data-testid="protected-screen-content">
                    Your current positions
                </p>
            </Block>

            <Block>
                <Card data-testid="portfolio-total-value-card" className="text-left">
                    <p className="m-0 text-sm text-white/60">Total Portfolio Value</p>

                    {portfolioValueStatus.kind === 'loading' && (
                        <strong data-testid="portfolio-total-value-loading" className="text-2xl text-white">Loading…</strong>
                    )}

                    {portfolioValueStatus.kind === 'error' && (
                        <ErrorText testid="portfolio-total-value-error">{portfolioValueStatus.message}</ErrorText>
                    )}

                    {portfolioValueStatus.kind === 'success' && (
                        <strong data-testid="portfolio-total-value" className="text-2xl font-bold text-white">
                            {formatCurrency(portfolioValueStatus.data.totalValue)}
                        </strong>
                    )}
                </Card>
            </Block>

            {status.kind === 'loading' && (
                <Block className="-mt-2 text-white/60" data-testid="portfolio-loading">Loading…</Block>
            )}

            {status.kind === 'error' && (
                <Block className="-mt-2"><ErrorText testid="portfolio-error">{status.message}</ErrorText></Block>
            )}

            {status.kind === 'success' && status.data.positions.length === 0 && (
                <Block className="-mt-2 text-white/60" data-testid="portfolio-empty">
                    No positions yet. Add one to get started.
                </Block>
            )}

            {status.kind === 'success' && status.data.positions.length > 0 && (
                <List strong inset data-testid="portfolio-positions">
                    {status.data.positions.map((pos) => (
                        <li
                            key={pos.id}
                            data-testid={`position-row-${pos.id}`}
                            className="flex list-none items-center justify-between px-4 py-3 [&:not(:last-child)]:border-b [&:not(:last-child)]:border-white/10"
                        >
                            <span className="text-white">
                                <strong>{pos.ticker}</strong> × {pos.quantity}
                                <br />
                                <small
                                    data-testid={`position-pnl-${pos.id}`}
                                    data-pnl-direction={pnlDirection(pos.pnl)}
                                    style={{ color: pos.pnl > 0 ? '#34d399' : pos.pnl < 0 ? '#f87171' : undefined }}
                                >
                                    {formatSignedCurrency(pos.pnl)} ({pos.pnlPercent}%)
                                </small>
                            </span>
                            <span className="flex gap-2">
                                <Button small inline tonal data-testid={`edit-position-${pos.id}`} onClick={() => startEdit(pos)}>
                                    Edit
                                </Button>
                                <Button small inline clear data-testid={`remove-position-${pos.id}`} onClick={() => handleRemovePosition(pos.id)}>
                                    Remove
                                </Button>
                            </span>
                        </li>
                    ))}
                </List>
            )}

            <Block className="grid grid-cols-2 gap-2">
                <Button data-testid="add-position-button" onClick={() => setScreen('add-position')}>
                    Add Position
                </Button>
                <Button data-testid="trade-button" onClick={() => setScreen('trading')}>
                    Trade
                </Button>
                <Button data-testid="research-button" onClick={() => setScreen('company-search')}>
                    Research
                </Button>
                <Button data-testid="nav-performance" onClick={openPerformance}>
                    Performance
                </Button>
                <Button data-testid="nav-watchlist" className="col-span-2" onClick={openWatchlist}>
                    Watchlist
                </Button>
            </Block>

            <Block>
                <Button clear data-testid="logout-button" onClick={handleLogout}>
                    Log out
                </Button>
            </Block>
        </Page>
    )
}
