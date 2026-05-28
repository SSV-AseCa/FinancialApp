describe('Auth0 Registration & Authentication Flow', () => {
  it('Security: invalid callback state -> investor redirected to register screen with error', () => {
    // 1. Visit the callback URL with fake code/state parameters
    // Without a preceding valid Auth0 login transaction stored in the browser,
    // the Auth0 SDK will throw an "Invalid state" error during handleRedirectCallback.
    cy.visit('/auth/callback?code=mock_code&state=mock_state')

    // 2. Verify that the application gracefully catches the error and redirects to /register
    cy.url().should('include', '/register')
  })

  it('Post-registration access: authenticated investor can reach a protected screen', () => {
    // 1. Programmatically login via Auth0 Management API 
    // (Bypasses UI cross-origin limitations)
    cy.loginByAuth0Api()

    // 2. Visit the protected screen directly
    cy.visit('/portfolio')

    // 3. Verify we stay on the portfolio screen and are NOT kicked out to /register
    cy.url().should('include', '/portfolio')

    // 4. Add a specific assertion for a DOM element that only appears when logged in
    cy.contains('Your Portfolio').should('be.visible')
  })
})
