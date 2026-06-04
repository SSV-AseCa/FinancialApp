/// <reference types="cypress" />

describe("Auth0 Authentication Flow - Callback & Security", () => {
  beforeEach(() => {
    // Ensure a clean localStorage state before each test
    cy.clearLocalStorage();
  });

  it("Security: invalid callback state -> investor redirected to login screen with error", () => {
    cy.visit("/auth/callback?code=mock_code&state=mock_state");

    cy.url().should("include", "/login");

    cy.get('[data-cy="error-banner"]')
      .should("be.visible")
      .and("contain.text", "Invalid state");
  });
});
