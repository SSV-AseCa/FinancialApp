const mockTransaction = {
  id: 'tx-1',
  portfolioId: 'portfolio-1',
  cik: '0000320193',
  quantity: 5,
  type: 'BUY',
  transactionDate: '2025-06-01',
}

const mockSellTransaction = {
  id: 'tx-2',
  portfolioId: 'portfolio-1',
  cik: '0000320193',
  quantity: 2,
  type: 'SELL',
  transactionDate: '2025-06-02',
}

describe('Trading', () => {
  beforeEach(() => {
    cy.loginByAuth0Api()
    cy.intercept('GET', '/portfolio/transactions', { statusCode: 200, body: [] }).as('getHistory')
    cy.visit('/trading')
    cy.wait('@getHistory')
    cy.get('[data-testid="trading-page"]').should('be.visible')
  })

  it('View Transaction History — happy path: investor sees transaction list', () => {
    cy.intercept('GET', '/portfolio/transactions', {
      statusCode: 200,
      body: [mockTransaction, mockSellTransaction],
    }).as('getHistoryFull')

    cy.reload()
    cy.wait('@getHistoryFull')

    cy.get('[data-testid="transactions-list"]').should('be.visible')
    cy.get('[data-testid="transaction-tx-1"]').should('be.visible')
    cy.contains('BUY').should('be.visible')
    cy.get('[data-testid="transaction-tx-2"]').should('be.visible')
    cy.contains('SELL').should('be.visible')
  })

  it('View Transaction History — empty state: shows no-transactions message', () => {
    cy.get('[data-testid="no-transactions"]').should('be.visible')
  })

  it('Buy Shares — happy path: valid CIK and quantity → transaction created and history refreshed', () => {
    cy.intercept('POST', '/portfolio/transactions/buy', { statusCode: 201, body: mockTransaction }).as('buyShares')
    cy.intercept('GET', '/portfolio/transactions', { statusCode: 200, body: [mockTransaction] }).as('refreshHistory')

    cy.get('[data-testid="buy-cik-input"]').type('0000320193')
    cy.get('[data-testid="buy-quantity-input"]').clear().type('5')
    cy.get('[data-testid="buy-submit-button"]').click()

    cy.wait('@buyShares')
    cy.wait('@refreshHistory')
    cy.get('[data-testid="buy-success"]').should('be.visible')
    cy.get('[data-testid="transactions-list"]').should('be.visible')
  })

  it('Buy Shares — validation: missing fields show error', () => {
    cy.get('[data-testid="buy-submit-button"]').click()
    cy.get('[role="alert"]').first().should('be.visible')
  })

  it('Sell Shares — happy path: valid CIK and quantity → transaction created and history refreshed', () => {
    cy.intercept('POST', '/portfolio/transactions/sell', { statusCode: 201, body: mockSellTransaction }).as('sellShares')
    cy.intercept('GET', '/portfolio/transactions', { statusCode: 200, body: [mockSellTransaction] }).as('refreshHistory')

    cy.get('[data-testid="sell-cik-input"]').type('0000320193')
    cy.get('[data-testid="sell-quantity-input"]').clear().type('2')
    cy.get('[data-testid="sell-submit-button"]').click()

    cy.wait('@sellShares')
    cy.wait('@refreshHistory')
    cy.get('[data-testid="sell-success"]').should('be.visible')
  })

  it('Sell Shares — validation: missing fields show error', () => {
    cy.get('[data-testid="sell-submit-button"]').click()
    cy.get('[role="alert"]').last().should('be.visible')
  })
})
