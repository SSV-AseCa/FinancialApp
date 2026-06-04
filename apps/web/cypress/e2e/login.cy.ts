import { SSV_TOKEN_STORAGE_KEY } from "@ssv/ui-core";

describe("Log In Flow", () => {
  beforeEach(() => {
    cy.clearLocalStorage();
  });

  it("Happy path: programmatic Auth0 login → access token obtained → investor reaches portfolio screen", () => {
    cy.loginByAuth0Api();

    cy.visit("/portfolio");

    cy.url().should("include", "/portfolio");

    cy.window().then((win) => {
      const token = win.localStorage.getItem(SSV_TOKEN_STORAGE_KEY);
      expect(token).to.be.a("string");
      expect(token?.length).to.be.greaterThan(0);
    });

    cy.get('[data-testid="portfolio-page"]').should("be.visible");
  });

  it("Unauthenticated access: direct navigation to a protected route → redirected to login screen", () => {
    cy.visit("/portfolio");

    cy.url().should("include", "/login");
  });

  it("Post-login access: authenticated investor can reach protected screens", () => {
    cy.loginByAuth0Api();

    cy.visit("/portfolio");

    cy.url().should("include", "/portfolio");
    cy.get('[data-testid="portfolio-page"]').should("be.visible");
  });
});
