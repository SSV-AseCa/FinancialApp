type Position = {
    id: string
    ticker: string
    quantity: number
    operationDate: string
}

type Portfolio = {
    id: string
    positions: Position[]
}

type PortfolioValue = {
    totalValue: number
}

function apiBaseUrl(): string {
    return process.env.E2E_API_BASE_URL ?? 'http://10.0.2.2:8080'
}

async function apiRequest<T>(
    token: string,
    path: string,
    init?: RequestInit,
): Promise<T> {
    const response = await fetch(`${apiBaseUrl()}${path}`, {
        ...init,
        headers: {
            'Content-Type': 'application/json',
            Authorization: `Bearer ${token}`,
            ...(init?.headers as Record<string, string> | undefined),
        },
    })

    if (!response.ok) {
        throw new Error(
            `API request failed ${init?.method ?? 'GET'} ${path}: ${response.status} ${await response.text()}`,
        )
    }

    if (response.status === 204) {
        return undefined as T
    }

    const text = await response.text()

    if (!text) {
        return undefined as T
    }

    return JSON.parse(text) as T
}

export async function fetchPortfolio(token: string): Promise<Portfolio> {
    return apiRequest<Portfolio>(token, '/portfolio')
}

export async function fetchPortfolioValue(token: string): Promise<PortfolioValue> {
    return apiRequest<PortfolioValue>(token, '/portfolio/value')
}

export async function addPosition(
    token: string,
    input: { ticker: string; quantity: number; operationDate: string },
): Promise<Position> {
    return apiRequest<Position>(token, '/portfolio/positions', {
        method: 'POST',
        body: JSON.stringify(input),
    })
}

export async function removePosition(token: string, positionId: string): Promise<void> {
    await apiRequest<void>(token, `/portfolio/positions/${positionId}`, {
        method: 'DELETE',
    })
}

export async function clearPortfolio(token: string): Promise<void> {
    const portfolio = await fetchPortfolio(token)

    await Promise.all(
        portfolio.positions.map((position) =>
            removePosition(token, position.id),
        ),
    )
}

export async function seedPortfolioWithPosition(token: string): Promise<void> {
    await clearPortfolio(token)

    await addPosition(token, {
        ticker: 'AAPL',
        quantity: 10,
        operationDate: '2024-01-01',
    })
}