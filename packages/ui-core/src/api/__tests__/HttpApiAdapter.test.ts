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

  describe('buyShares', () => {
    it('sends POST /portfolio/transactions/buy with input as JSON body', async () => {
      const fetch = okFetch(
        { type: 'BUY', company: '0000320193', quantity: 5, date: '2024-01-01' },
        201,
      )
      vi.stubGlobal('fetch', fetch)
      const input = { companyCik: '0000320193', quantity: 5 }

      await adapter.buyShares(input)

      expect(fetch).toHaveBeenCalledWith(
        `${BASE}/portfolio/transactions/buy`,
        expect.objectContaining({ method: 'POST', body: JSON.stringify(input) }),
      )
    })

    it('returns the created transaction', async () => {
      const transaction = { type: 'BUY' as const, company: '0000320193', quantity: 5, date: '2024-01-01' }
      vi.stubGlobal('fetch', okFetch(transaction, 201))

      expect(await adapter.buyShares({ companyCik: '0000320193', quantity: 5 })).toEqual(transaction)
    })

    it('throws ApiError on 422 for business rule violations', async () => {
      vi.stubGlobal('fetch', errorFetch(422, 'Insufficient funds'))

      const error = await adapter.buyShares({ companyCik: '0000320193', quantity: 999999 }).catch((e) => e)

      expect(error).toBeInstanceOf(ApiError)
      expect(error.status).toBe(422)
      expect(error.message).toBe('Insufficient funds')
    })
  })

  describe('sellShares', () => {
    it('sends POST /portfolio/transactions/sell with input as JSON body', async () => {
      const fetch = okFetch(
        { type: 'SELL', company: '0000320193', quantity: 2, date: '2024-01-01' },
        201,
      )
      vi.stubGlobal('fetch', fetch)
      const input = { companyCik: '0000320193', quantity: 2 }

      await adapter.sellShares(input)

      expect(fetch).toHaveBeenCalledWith(
        `${BASE}/portfolio/transactions/sell`,
        expect.objectContaining({ method: 'POST', body: JSON.stringify(input) }),
      )
    })

    it('throws ApiError for insufficient shares', async () => {
      vi.stubGlobal('fetch', errorFetch(422, 'Insufficient shares'))

      const error = await adapter.sellShares({ companyCik: '0000320193', quantity: 100 }).catch((e) => e)

      expect(error).toBeInstanceOf(ApiError)
      expect(error.status).toBe(422)
      expect(error.message).toBe('Insufficient shares')
    })
  })
})
