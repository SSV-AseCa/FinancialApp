const mockCompanies = [
  { name: 'Apple Inc.', cik: '0000320193', tickers: ['AAPL'] },
  { name: 'Apple Hospitality REIT', cik: '0001418121', tickers: [] },
]

describe('Company Research', () => {
  beforeEach(() => {
    cy.loginByAuth0Api()
    cy.visit('/companies')
    cy.get('[data-testid="company-search-page"]').should('be.visible')
  })

  it('Search Companies — happy path: query returns matching companies', () => {
    cy.intercept('GET', '/companies/search?q=apple', { statusCode: 200, body: mockCompanies }).as('search')

    cy.get('[data-testid="company-search-input"]').type('apple')
    cy.get('[data-testid="company-search-submit"]').click()
    cy.wait('@search')

    cy.get('[data-testid="company-search-results"]').should('be.visible')
    cy.get('[data-testid="company-result-0000320193"]').should('be.visible')
    cy.contains('Apple Inc.').should('be.visible')
  })

  it('Search Companies — empty result: shows no-results message', () => {
    cy.intercept('GET', '/companies/search?q=unknownxyz', { statusCode: 200, body: [] }).as('emptySearch')

    cy.get('[data-testid="company-search-input"]').type('unknownxyz')
    cy.get('[data-testid="company-search-submit"]').click()
    cy.wait('@emptySearch')

    cy.get('[data-testid="no-results"]').should('be.visible')
  })

  it('Search Companies — pressing Enter triggers search', () => {
    cy.intercept('GET', '/companies/search?q=apple', { statusCode: 200, body: mockCompanies }).as('search')

    cy.get('[data-testid="company-search-input"]').type('apple{enter}')
    cy.wait('@search')
    cy.get('[data-testid="company-search-results"]').should('be.visible')
  })

  it('Search Companies — results show company name, CIK and tickers', () => {
    cy.intercept('GET', '/companies/search?q=apple', { statusCode: 200, body: mockCompanies }).as('search')

    cy.get('[data-testid="company-search-input"]').type('apple')
    cy.get('[data-testid="company-search-submit"]').click()
    cy.wait('@search')

    cy.get('[data-testid="company-result-0000320193"]').should('be.visible')
    cy.contains('AAPL').should('be.visible')
  })
})
