/// <reference types="cypress" />

describe("Auth0 Registration & Authentication Flow", () => {
  it("Security: invalid callback state -> investor redirected to register screen with error", () => {
    // 1. Visit the callback URL with fake code/state parameters
    // Without a preceding valid Auth0 login transaction stored in the browser,
    // the Auth0 SDK will throw an "Invalid state" error during handleRedirectCallback.
    cy.visit("/auth/callback?code=mock_code&state=mock_state");

    // 2. Verify that the application gracefully catches the error and redirects to /register
    cy.url().should("include", "/register");

    // 3. Verify the redirected register screen surfaces the callback error to the user
    cy.get('[data-cy="error-banner"]')
      .should("be.visible")
      .and("contain.text", "Invalid state");
  });

  it("Security: unauthenticated user trying to access protected screen -> redirected to register", () => {
    // 1. Attempt to navigate directly to the protected portfolio screen
    cy.visit("/portfolio");

    // 2. Verify that the user is redirected to the register page
    cy.url().should("include", "/register");
  });

  it("Post-registration access: authenticated investor can reach a protected screen", () => {
    // 1. Programmatically log in via the Auth0 Authentication API (/oauth/token)
    // (Bypasses UI cross-origin limitations)
    cy.loginByAuth0Api();

    // 2. Visit the protected screen directly
    cy.visit("/portfolio");

    // 3. Verify we stay on the portfolio screen and are NOT kicked out to /register
    cy.url().should("include", "/portfolio");

    // 4. Assert on the actual protected-page heading rendered for authenticated users
    cy.contains("h1", "Portfolio").should("be.visible");
  });
});
