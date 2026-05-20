describe('Application', () => {
  it('loads the home page', () => {
    cy.visit('/')
    cy.get('#root').should('exist')
  })
})
