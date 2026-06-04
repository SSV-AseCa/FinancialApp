/// <reference types="cypress" />

describe("Auth0 Authentication Flow - Callback & Security", () => {
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

    // 3. Verify the redirected login screen surfaces the callback error to the user
    cy.get('[data-cy="error-banner"]')
      .should("be.visible")
      .and("contain.text", "Invalid state");
  });
});
