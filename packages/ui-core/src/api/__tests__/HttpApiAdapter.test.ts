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
})
