describe('Log In Flow', () => {
  it('Happy path: programmatic Auth0 login → access token obtained → investor reaches portfolio screen', () => {
    // 1. Log in programmatically
    cy.loginByAuth0Api()

    // 2. Verify token is placed in localStorage by the custom command
    cy.window().then((win) => {
      const token = win.localStorage.getItem('ssv_access_token')
      expect(token).to.be.a('string')
      expect(token?.length).to.be.greaterThan(0)
    })

    // 3. Visit the portfolio screen
    cy.visit('/portfolio')

    // 4. Verify we successfully reach the portfolio screen
    cy.url().should('include', '/portfolio')
  })

  it('Unauthenticated access: direct navigation to a protected route → redirected to login screen', () => {
    // 1. Visit a protected route without being logged in
    cy.visit('/portfolio')

    // 2. Verify we are redirected to the login screen
    cy.url().should('include', '/login')
  })

  it('Post-login access: authenticated investor can reach protected screens', () => {
    // 1. Log in programmatically
    cy.loginByAuth0Api()

    // 2. Visit the protected screen
    cy.visit('/portfolio')

    // 3. Verify we stay on the portfolio screen and it renders
    cy.url().should('include', '/portfolio')
    cy.contains('Portfolio').should('be.visible')
  })
})
