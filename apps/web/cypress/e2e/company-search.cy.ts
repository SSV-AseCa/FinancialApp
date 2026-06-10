describe("Company Research", () => {
  beforeEach(() => {
    cy.loginByAuth0Api();
    cy.visit("/companies");
    cy.get('[data-testid="company-search-page"]').should("be.visible");
  });

  it("Search Companies — happy path: query returns matching companies", () => {
    cy.get('[data-testid="company-search-input"]').type("apple");
    cy.get('[data-testid="company-search-submit"]').click();

    cy.get('[data-testid="company-search-results"]', { timeout: 20000 }).should("be.visible");
    cy.get('[data-testid="company-result-0000320193"]').should("be.visible");
    cy.contains("Apple").should("be.visible");
  });

  it("Search Companies — empty result: shows no-results message", () => {
    cy.get('[data-testid="company-search-input"]').type("ZZZNONE9999XYZ");
    cy.get('[data-testid="company-search-submit"]').click();

    cy.get('[data-testid="no-results"]', { timeout: 20000 }).should("be.visible");
  });

  it("Search Companies — pressing Enter triggers search", () => {
    cy.get('[data-testid="company-search-input"]').type("apple{enter}");

    cy.get('[data-testid="company-search-results"]', { timeout: 20000 }).should("be.visible");
  });

  it("Search Companies — results show company name, CIK and tickers", () => {
    cy.get('[data-testid="company-search-input"]').type("apple");
    cy.get('[data-testid="company-search-submit"]').click();

    cy.get('[data-testid="company-result-0000320193"]', { timeout: 20000 }).should("be.visible");
    cy.get('[data-testid="company-result-0000320193"]').contains("0000320193");
    cy.get('[data-testid="company-result-0000320193"]').contains("AAPL");
  });
});
