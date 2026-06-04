/// <reference types="cypress" />

describe("Auth0 Registration & Authentication Flow", () => {
  it("Security: invalid callback state -> investor redirected to register screen with error", () => {
    cy.visit("/auth/callback?code=mock_code&state=mock_state");

    cy.url().should("include", "/register");

    cy.get('[data-cy="error-banner"]')
      .should("be.visible")
      .and("contain.text", "Invalid state");
  });

  it("Security: unauthenticated user trying to access protected screen -> redirected to register", () => {
    cy.visit("/portfolio");

    cy.url().should("include", "/register");
  });
});
