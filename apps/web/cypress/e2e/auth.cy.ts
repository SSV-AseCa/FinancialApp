/// <reference types="cypress" />

describe("Auth0 Authentication Flow", () => {
  beforeEach(() => {
    // Ensure a clean localStorage state before each test
    cy.clearLocalStorage();
  });

  it("Security: invalid callback state -> investor redirected to login screen with error", () => {
    // 1. Visit the callback URL with fake code/state parameters.
    // Without a preceding valid Auth0 login transaction stored in the browser,
    // the Auth0 SDK will throw an "Invalid state" error during handleRedirectCallback.
    cy.visit("/auth/callback?code=mock_code&state=mock_state");

    // 2. Verify that the application gracefully catches the error and redirects to /login
    cy.url().should("include", "/login");
  });

  it("Security: unauthenticated user trying to access protected screen -> redirected to login", () => {
    // 1. Attempt to navigate directly to the protected portfolio screen
    cy.visit("/portfolio");

    // 2. Verify that the AuthGuard redirects to /login
    cy.url().should("include", "/login");
  });

  it("Post-registration access: authenticated investor can reach a protected screen", () => {
    // 1. Programmatically authenticate by injecting a mock token into localStorage.
    // This bypasses the UI and avoids ROPC or any real Auth0 credentials.
    cy.loginByAuth0Api();

    // 2. Visit the protected screen directly
    cy.visit("/portfolio");

    // 3. Verify we stay on the portfolio screen and are NOT kicked out to /login
    cy.url().should("include", "/portfolio");

    // 4. Assert on the actual protected-page heading rendered for authenticated users
    cy.contains("h1", "Portfolio").should("be.visible");
  });
});
