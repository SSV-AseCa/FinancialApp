describe("Company Financial Metrics", () => {
  // Apple — a stable, well-known filer for deterministic EDGAR data.
  const APPLE_CIK = "0000320193";

  function searchAndOpenApple() {
    cy.get('[data-testid="company-search-input"]').type("apple");
    cy.get('[data-testid="company-search-submit"]').click();
    cy.get(`[data-testid="company-result-${APPLE_CIK}"]`, { timeout: 20000 })
      .should("be.visible")
      .click();
  }

  beforeEach(() => {
    cy.loginByAuth0Api();
    cy.visit("/companies");
    cy.get('[data-testid="company-search-page"]').should("be.visible");
  });

  it("View Company Financial Metrics — investor searches a company, selects it, and sees metrics", () => {
    searchAndOpenApple();

    cy.url().should("include", `/companies/${APPLE_CIK}`);
    cy.get('[data-testid="company-detail-page"]').should("be.visible");
    cy.get('[data-testid="metrics-grid"]', { timeout: 20000 }).should("be.visible");
  });

  it("View Company Financial Metrics — at least one metric field is visible and non-empty", () => {
    searchAndOpenApple();

    cy.get('[data-testid="metric-card-0"]', { timeout: 20000 })
      .should("be.visible")
      .and("not.have.text", "");
  });
});
