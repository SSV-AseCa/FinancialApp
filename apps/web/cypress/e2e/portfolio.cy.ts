const mockPortfolio = {
  id: 'portfolio-1',
  positions: [
    { id: 'pos-1', ticker: 'AAPL', quantity: 10, operationDate: '2025-01-15' },
  ],
}

const emptyPortfolio = { id: 'portfolio-1', positions: [] }

describe('Portfolio Management', () => {
  beforeEach(() => {
    cy.loginByAuth0Api()
    cy.intercept('GET', '/portfolio', { statusCode: 200, body: mockPortfolio }).as('getPortfolio')
    cy.visit('/portfolio')
    cy.wait('@getPortfolio')
    cy.get('[data-testid="portfolio-page"]').should('be.visible')
  })

  it('View Portfolio — happy path: authenticated investor sees their positions', () => {
    cy.get('[data-testid="portfolio-positions"]').should('be.visible')
    cy.get('[data-testid="position-row-pos-1"]').should('be.visible')
    cy.contains('AAPL').should('be.visible')
  })

  it('View Portfolio — empty state: shows empty message when no positions', () => {
    cy.intercept('GET', '/portfolio', { statusCode: 200, body: emptyPortfolio }).as('emptyPortfolio')
    cy.reload()
    cy.wait('@emptyPortfolio')
    cy.get('[data-testid="portfolio-empty"]').should('be.visible')
  })

  it('Add Position — happy path: form submitted → position appears in portfolio', () => {
    const newPosition = { id: 'pos-2', ticker: 'MSFT', quantity: 5, operationDate: '2025-06-01' }
    cy.intercept('POST', '/portfolio/positions', { statusCode: 201, body: newPosition }).as('addPosition')
    cy.intercept('GET', '/portfolio', {
      statusCode: 200,
      body: { ...mockPortfolio, positions: [...mockPortfolio.positions, newPosition] },
    }).as('refreshPortfolio')

    cy.get('[data-testid="add-position-button"]').click()
    cy.get('[data-testid="add-position-form"]').should('be.visible')

    cy.get('[data-testid="add-ticker-input"]').type('MSFT')
    cy.get('[data-testid="add-quantity-input"]').clear().type('5')
    cy.get('[data-testid="add-date-input"]').invoke('val', '2025-06-01').trigger('change')

    cy.get('[data-testid="confirm-add-position-button"]').click()
    cy.wait('@addPosition')
    cy.wait('@refreshPortfolio')

    cy.contains('MSFT').should('be.visible')
  })

  it('Modify Position — happy path: edit form submitted → updated position shown', () => {
    const updated = { id: 'pos-1', ticker: 'AAPL', quantity: 20, operationDate: '2025-01-15' }
    cy.intercept('PUT', '/portfolio/positions/pos-1', { statusCode: 200, body: updated }).as('updatePosition')
    cy.intercept('GET', '/portfolio', {
      statusCode: 200,
      body: { ...mockPortfolio, positions: [updated] },
    }).as('refreshPortfolio')

    cy.get('[data-testid="position-row-pos-1"]').trigger('mouseover')
    cy.get('[data-testid="edit-position-pos-1"]').click({ force: true })

    cy.get('[data-testid="edit-quantity-input"]').clear().type('20')
    cy.get('[data-testid="save-position-button"]').click()

    cy.wait('@updatePosition')
    cy.wait('@refreshPortfolio')
    cy.contains('20').should('be.visible')
  })

  it('Remove Position — happy path: position deleted and disappears from list', () => {
    cy.intercept('DELETE', '/portfolio/positions/pos-1', { statusCode: 204 }).as('removePosition')
    cy.intercept('GET', '/portfolio', {
      statusCode: 200,
      body: emptyPortfolio,
    }).as('refreshPortfolio')

    cy.get('[data-testid="position-row-pos-1"]').trigger('mouseover')
    cy.get('[data-testid="remove-position-pos-1"]').click({ force: true })

    cy.wait('@removePosition')
    cy.wait('@refreshPortfolio')
    cy.get('[data-testid="portfolio-empty"]').should('be.visible')
  })
})
