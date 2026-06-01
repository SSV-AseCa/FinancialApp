describe('Log Out Flow', () => {
  it('Happy path: authenticated investor logs out → redirected to login screen', () => {
    // 1. Log in programmatically
    cy.loginByAuth0Api()

    // 2. Visit the protected portfolio screen
    cy.visit('/portfolio')

    // 3. Ensure we are on the portfolio screen
    cy.url().should('include', '/portfolio')

    // 4. Click the Log Out button in the header
    cy.contains('button', 'Log Out').click()

    // 5. Verify we are redirected to the Auth0 logout endpoint and then back to the login screen
    cy.url().should('include', '/login')
  })

  it('Post-logout access: attempt to navigate to a protected route after logout → redirected to login', () => {
    // 1. Log in programmatically
    cy.loginByAuth0Api()

    // 2. Visit the protected portfolio screen
    cy.visit('/portfolio')

    // 3. Click the Log Out button
    cy.contains('button', 'Log Out').click()

    // 4. Wait until the redirect to login finishes
    cy.url().should('include', '/login')

    // 5. Attempt to manually navigate back to the protected route
    cy.visit('/portfolio')

    // 6. Verify we are immediately bounced back to the login screen
    cy.url().should('include', '/login')
  })
})
