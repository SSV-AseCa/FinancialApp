describe('Auth0 Registration & Authentication Flow', () => {
  it('Happy path: callback received -> investor redirected to portfolio screen', () => {
    // 1. Intercept the Auth0 SDK's token exchange network request 
    // to mock a successful callback without actually hitting Auth0 UI.
    cy.intercept('POST', '**/oauth/token', {
      statusCode: 200,
      body: {
        access_token: 'mock-access-token-from-cypress',
        id_token: 'mock-id-token',
        scope: 'openid profile email',
        expires_in: 86400,
        token_type: 'Bearer'
      }
    }).as('auth0TokenExchange')

    // 2. Visit the callback URL with fake code/state parameters
    // This simulates the redirect back from Auth0 after a successful registration/login.
    cy.visit('/auth/callback?code=mock_code&state=mock_state')

    // 3. Verify that the SDK made the exchange call
    cy.wait('@auth0TokenExchange')

    // 4. Verify we are redirected to the protected portfolio screen
    cy.url().should('include', '/portfolio')
  })

  it('Post-registration access: authenticated investor can reach a protected screen', () => {
    // 1. Programmatically login via Auth0 Management API 
    // (Bypasses UI cross-origin limitations)
    cy.loginByAuth0Api()

    // 2. Visit the protected screen directly
    cy.visit('/portfolio')

    // 3. Verify we stay on the portfolio screen and are NOT kicked out to /login
    cy.url().should('include', '/portfolio')

    // (Optional) Add a specific assertion for a DOM element that only appears when logged in
    // cy.contains('Portfolio').should('be.visible')
  })
})
