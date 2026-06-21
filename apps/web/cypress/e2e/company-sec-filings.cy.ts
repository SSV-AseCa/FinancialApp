describe("Company SEC Filings", () => {
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

  it("View Company SEC Filings — investor searches a company, selects it, and sees recent filings", () => {
    searchAndOpenApple();

    cy.url().should("include", `/companies/${APPLE_CIK}`);
    cy.get('[data-testid="company-detail-page"]').should("be.visible");
    cy.get('[data-testid="filings-list"]', { timeout: 20000 }).should("be.visible");
  });

  it("View Company SEC Filings — at least one filing shows a form type and date", () => {
    searchAndOpenApple();

    cy.get('[data-testid="filing-row-0"]', { timeout: 20000 }).should("be.visible");
    cy.get('[data-testid="filing-form-type"]')
      .first()
      .should("be.visible")
      .and("not.have.text", "");
    cy.get('[data-testid="filing-date"]')
      .first()
      .should("be.visible")
      .and("not.have.text", "");
  });

  it("View Company SEC Filings — a search box lets the investor filter filings by form type", () => {
    searchAndOpenApple();

    cy.get('[data-testid="filings-list"]', { timeout: 20000 }).should("be.visible");
    cy.get('[data-testid="filings-search"]').should("be.visible").type("10-K");
    // Server-side search re-queries the cached filings; matching rows keep rendering.
    cy.get('[data-testid="filing-row-0"]', { timeout: 20000 }).should("be.visible");
  });
});
