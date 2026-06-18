import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import type { AuthPort } from '../../auth/AuthPort'
import { ApiError } from '../ApiError'
import { HttpApiAdapter } from '../HttpApiAdapter'

const fakeAuth: AuthPort = {
  register: async () => {},
  login: async () => {},
  logout: async () => {},
  handleCallback: async () => {},
  getAccessToken: () => 'test-token',
  isAuthenticated: () => true,
}

function okFetch(data: unknown, status = 200) {
  return vi.fn().mockResolvedValue({
    ok: true,
    status,
    json: async () => data,
  })
}

function errorFetch(status: number, message: string) {
  return vi.fn().mockResolvedValue({
    ok: false,
    status,
    statusText: 'Error',
    json: async () => ({ message }),
  })
}

const BASE = 'http://localhost:8080'

describe('HttpApiAdapter', () => {
  let adapter: HttpApiAdapter

  beforeEach(() => {
    adapter = new HttpApiAdapter(fakeAuth, BASE)
  })

  afterEach(() => {
    vi.unstubAllGlobals()
  })

  describe('fetchPortfolio', () => {
    it('sends GET /portfolio with Authorization header', async () => {
      const fetch = okFetch({ id: 'p1', positions: [] })
      vi.stubGlobal('fetch', fetch)

      await adapter.fetchPortfolio()

      expect(fetch).toHaveBeenCalledWith(
        `${BASE}/portfolio`,
        expect.objectContaining({
          headers: expect.objectContaining({ Authorization: 'Bearer test-token' }),
        }),
      )
    })

    it('returns the portfolio from the response', async () => {
      const portfolio = {
        id: 'p1',
        positions: [{ id: 'pos1', ticker: 'AAPL', quantity: 10, operationDate: '2024-01-01' }],
      }
      vi.stubGlobal('fetch', okFetch(portfolio))

      expect(await adapter.fetchPortfolio()).toEqual(portfolio)
    })

    it('throws ApiError on 401', async () => {
      vi.stubGlobal('fetch', errorFetch(401, 'Unauthorized'))

      const error = await adapter.fetchPortfolio().catch((e) => e)

      expect(error).toBeInstanceOf(ApiError)
      expect(error.status).toBe(401)
      expect(error.message).toBe('Unauthorized')
    })

    it('exposes per-position pnl and pnlPercent', async () => {
      const portfolio = {
        id: 'p1',
        positions: [
          { id: 'pos1', ticker: 'AAPL', quantity: 10, operationDate: '2024-01-01', pnl: 150.5, pnlPercent: 12.5 },
        ],
      }
      vi.stubGlobal('fetch', okFetch(portfolio))

      const result = await adapter.fetchPortfolio()

      expect(result.positions[0].pnl).toBe(150.5)
      expect(result.positions[0].pnlPercent).toBe(12.5)
    })
  })

  describe('addPosition', () => {
    it('sends POST /portfolio/positions with the input as JSON body', async () => {
      const fetch = okFetch({ id: 'pos1', ticker: 'AAPL', quantity: 5, operationDate: '2024-01-01' }, 201)
      vi.stubGlobal('fetch', fetch)
      const input = { ticker: 'AAPL', quantity: 5, operationDate: '2024-01-01' }

      await adapter.addPosition(input)

      expect(fetch).toHaveBeenCalledWith(
        `${BASE}/portfolio/positions`,
        expect.objectContaining({ method: 'POST', body: JSON.stringify(input) }),
      )
    })

    it('returns the created position', async () => {
      const position = { id: 'pos1', ticker: 'AAPL', quantity: 5, operationDate: '2024-01-01' }
      vi.stubGlobal('fetch', okFetch(position, 201))

      expect(await adapter.addPosition({ ticker: 'AAPL', quantity: 5, operationDate: '2024-01-01' })).toEqual(position)
    })

    it('throws ApiError with the message from the response body on 400', async () => {
      vi.stubGlobal('fetch', errorFetch(400, 'Invalid ticker'))

      const error = await adapter.addPosition({ ticker: '', quantity: 0, operationDate: '' }).catch((e) => e)

      expect(error).toBeInstanceOf(ApiError)
      expect(error.status).toBe(400)
      expect(error.message).toBe('Invalid ticker')
    })
  })

  describe('modifyPosition', () => {
    it('sends PUT /portfolio/positions/{id} with the input as JSON body', async () => {
      const fetch = okFetch({ id: 'pos1', ticker: 'GOOG', quantity: 3, operationDate: '2024-02-01' })
      vi.stubGlobal('fetch', fetch)
      const input = { ticker: 'GOOG', quantity: 3, operationDate: '2024-02-01' }

      await adapter.modifyPosition('pos1', input)

      expect(fetch).toHaveBeenCalledWith(
        `${BASE}/portfolio/positions/pos1`,
        expect.objectContaining({ method: 'PUT', body: JSON.stringify(input) }),
      )
    })

    it('throws ApiError with status 404 when position not found', async () => {
      vi.stubGlobal('fetch', errorFetch(404, 'Position not found'))

      const error = await adapter
        .modifyPosition('nonexistent', { ticker: 'X', quantity: 1, operationDate: '2024-01-01' })
        .catch((e) => e)

      expect(error).toBeInstanceOf(ApiError)
      expect(error.status).toBe(404)
      expect(error.message).toBe('Position not found')
    })
  })

  describe('removePosition', () => {
    it('sends DELETE /portfolio/positions/{id}', async () => {
      const fetch = vi.fn().mockResolvedValue({ ok: true, status: 204 })
      vi.stubGlobal('fetch', fetch)

      await adapter.removePosition('pos1')

      expect(fetch).toHaveBeenCalledWith(
        `${BASE}/portfolio/positions/pos1`,
        expect.objectContaining({ method: 'DELETE' }),
      )
    })

    it('returns undefined on 204', async () => {
      vi.stubGlobal('fetch', vi.fn().mockResolvedValue({ ok: true, status: 204 }))

      expect(await adapter.removePosition('pos1')).toBeUndefined()
    })

    it('throws ApiError with status 404 when position not found', async () => {
      vi.stubGlobal('fetch', errorFetch(404, 'Position not found'))

      const error = await adapter.removePosition('nonexistent').catch((e) => e)

      expect(error).toBeInstanceOf(ApiError)
      expect(error.status).toBe(404)
    })
  })

  describe('searchCompanies', () => {
    it('sends GET /companies/search with the encoded query', async () => {
      const fetch = okFetch([{ name: 'Apple Inc.', cik: '0000320193' }])
      vi.stubGlobal('fetch', fetch)

      await adapter.searchCompanies('Apple')

      expect(fetch).toHaveBeenCalledWith(
        `${BASE}/companies/search?q=Apple`,
        expect.any(Object),
      )
    })

    it('URL-encodes the query parameter', async () => {
      const fetch = okFetch([])
      vi.stubGlobal('fetch', fetch)

      await adapter.searchCompanies('Apple Inc')

      expect(fetch).toHaveBeenCalledWith(
        `${BASE}/companies/search?q=Apple%20Inc`,
        expect.any(Object),
      )
    })

    it('returns the list of matching companies', async () => {
      const companies = [{ name: 'Apple Inc.', cik: '0000320193' }]
      vi.stubGlobal('fetch', okFetch(companies))

      expect(await adapter.searchCompanies('Apple')).toEqual(companies)
    })

    it('returns an empty list when there are no matches', async () => {
      vi.stubGlobal('fetch', okFetch([]))

      expect(await adapter.searchCompanies('zzznomatch')).toEqual([])
    })
  })

  describe('getPortfolioTotalValue', () => {
    it('sends GET /portfolio/value with Authorization header', async () => {
      const fetch = okFetch({ totalValue: 1234.56 })
      vi.stubGlobal('fetch', fetch)

      await adapter.getPortfolioTotalValue()

      expect(fetch).toHaveBeenCalledWith(
        `${BASE}/portfolio/value`,
        expect.objectContaining({
          headers: expect.objectContaining({ Authorization: 'Bearer test-token' }),
        }),
      )
    })

    it('returns the portfolio value from the response', async () => {
      const value = { totalValue: 1234.56 }
      vi.stubGlobal('fetch', okFetch(value))

      expect(await adapter.getPortfolioTotalValue()).toEqual(value)
    })

    it('throws ApiError on 401', async () => {
      vi.stubGlobal('fetch', errorFetch(401, 'Unauthorized'))

      const error = await adapter.getPortfolioTotalValue().catch((e) => e)

      expect(error).toBeInstanceOf(ApiError)
      expect(error.status).toBe(401)
      expect(error.message).toBe('Unauthorized')
    })
  })

  describe('getPortfolioPerformance', () => {
    it('sends GET /portfolio/performance with Authorization header', async () => {
      const fetch = okFetch({ totalValue: 1000, totalPnL: 250 })
      vi.stubGlobal('fetch', fetch)

      await adapter.getPortfolioPerformance()

      expect(fetch).toHaveBeenCalledWith(
        `${BASE}/portfolio/performance`,
        expect.objectContaining({
          headers: expect.objectContaining({ Authorization: 'Bearer test-token' }),
        }),
      )
    })

    it('returns the performance metrics from the response', async () => {
      const performance = { totalValue: 1000, totalPnL: 250 }
      vi.stubGlobal('fetch', okFetch(performance))

      expect(await adapter.getPortfolioPerformance()).toEqual(performance)
    })

    it('throws ApiError on 401', async () => {
      vi.stubGlobal('fetch', errorFetch(401, 'Unauthorized'))

      const error = await adapter.getPortfolioPerformance().catch((e) => e)

      expect(error).toBeInstanceOf(ApiError)
      expect(error.status).toBe(401)
      expect(error.message).toBe('Unauthorized')
    })
  })

  describe('addToWatchlist', () => {
    it('sends POST /watchlist with the cik as JSON body and Authorization header', async () => {
      const fetch = okFetch({ id: 'w1', companyId: 'c1', cik: '0000320193' }, 201)
      vi.stubGlobal('fetch', fetch)

      await adapter.addToWatchlist('0000320193')

      expect(fetch).toHaveBeenCalledWith(
        `${BASE}/watchlist`,
        expect.objectContaining({
          method: 'POST',
          body: JSON.stringify({ cik: '0000320193' }),
          headers: expect.objectContaining({ Authorization: 'Bearer test-token' }),
        }),
      )
    })

    it('returns the created watchlist entry', async () => {
      const entry = { id: 'w1', companyId: 'c1', cik: '0000320193' }
      vi.stubGlobal('fetch', okFetch(entry, 201))

      expect(await adapter.addToWatchlist('0000320193')).toEqual(entry)
    })

    it('throws ApiError with status 409 when the company is already on the watchlist', async () => {
      vi.stubGlobal('fetch', errorFetch(409, 'Already on watchlist'))

      const error = await adapter.addToWatchlist('0000320193').catch((e) => e)

      expect(error).toBeInstanceOf(ApiError)
      expect(error.status).toBe(409)
      expect(error.message).toBe('Already on watchlist')
    })
  })

  describe('getWatchlist', () => {
    it('sends GET /watchlist with Authorization header', async () => {
      const fetch = okFetch([])
      vi.stubGlobal('fetch', fetch)

      await adapter.getWatchlist()

      expect(fetch).toHaveBeenCalledWith(
        `${BASE}/watchlist`,
        expect.objectContaining({
          headers: expect.objectContaining({ Authorization: 'Bearer test-token' }),
        }),
      )
    })

    it('returns the list of watched companies with their financial metrics', async () => {
      const companies = [
        {
          companyId: 'c1',
          cik: '0000320193',
          symbol: 'AAPL',
          name: 'Apple Inc.',
          metrics: { revenue: 383285, netIncome: 96995, assets: 352583, equity: 62146 },
        },
      ]
      vi.stubGlobal('fetch', okFetch(companies))

      expect(await adapter.getWatchlist()).toEqual(companies)
    })
  })

  describe('removeFromWatchlist', () => {
    it('sends DELETE /watchlist/{cik} with Authorization header', async () => {
      const fetch = okFetch(undefined, 204)
      vi.stubGlobal('fetch', fetch)

      await adapter.removeFromWatchlist('0000320193')

      expect(fetch).toHaveBeenCalledWith(
        `${BASE}/watchlist/0000320193`,
        expect.objectContaining({
          method: 'DELETE',
          headers: expect.objectContaining({ Authorization: 'Bearer test-token' }),
        }),
      )
    })

    it('throws ApiError with status 404 when the entry is not found', async () => {
      vi.stubGlobal('fetch', errorFetch(404, 'Watchlist entry not found'))

      const error = await adapter.removeFromWatchlist('0000320193').catch((e) => e)

      expect(error).toBeInstanceOf(ApiError)
      expect(error.status).toBe(404)
      expect(error.message).toBe('Watchlist entry not found')
    })
  })

  describe('compareWatchlistCompanies', () => {
    it('sends GET /watchlist/compare with the ciks comma-joined and encoded and Authorization header', async () => {
      const fetch = okFetch({ companies: [] })
      vi.stubGlobal('fetch', fetch)

      await adapter.compareWatchlistCompanies(['0000320193', '0000789019'])

      expect(fetch).toHaveBeenCalledWith(
        `${BASE}/watchlist/compare?ciks=0000320193%2C0000789019`,
        expect.objectContaining({
          headers: expect.objectContaining({ Authorization: 'Bearer test-token' }),
        }),
      )
    })

    it('returns the comparison with companies and their financial metrics', async () => {
      const comparison = {
        companies: [
          {
            companyId: 'c1',
            cik: '0000320193',
            symbol: 'AAPL',
            name: 'Apple Inc.',
            metrics: { revenue: 383285, netIncome: 96995, assets: 352583, equity: 62146 },
          },
          {
            companyId: 'c2',
            cik: '0000789019',
            symbol: 'MSFT',
            name: 'Microsoft Corporation',
            metrics: { revenue: 211915, netIncome: 72361, assets: 411976, equity: 206223 },
          },
        ],
      }
      vi.stubGlobal('fetch', okFetch(comparison))

      expect(await adapter.compareWatchlistCompanies(['0000320193', '0000789019'])).toEqual(comparison)
    })

    it('throws ApiError with status 400 when fewer than two companies are provided', async () => {
      vi.stubGlobal('fetch', errorFetch(400, 'At least two companies are required'))

      const error = await adapter.compareWatchlistCompanies(['0000320193']).catch((e) => e)

      expect(error).toBeInstanceOf(ApiError)
      expect(error.status).toBe(400)
      expect(error.message).toBe('At least two companies are required')
    })
  })
})
