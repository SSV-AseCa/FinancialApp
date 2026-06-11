describe("Company SEC Filings", () => {
  // Apple — a stable, well-known filer with frequent public EDGAR filings.
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

  it("View Company SEC Filings — investor selects a company and sees a list of recent filings", () => {
    searchAndOpenApple();

    cy.url().should("include", `/companies/${APPLE_CIK}`);
    cy.get('[data-testid="company-detail-page"]').should("be.visible");
    cy.get('[data-testid="filings-list"]', { timeout: 20000 }).should("be.visible");
    cy.get('[data-testid^="filing-row-"]').should("have.length.greaterThan", 0);
  });

  it("View Company SEC Filings — each filing row shows at minimum form type and date", () => {
    searchAndOpenApple();

    cy.get('[data-testid="filing-row-0"]', { timeout: 20000 }).should("be.visible");
    cy.get('[data-testid="filing-row-0"]')
      .find('[data-testid="filing-form-type"]')
      .should("be.visible")
      .and("not.have.text", "");
    cy.get('[data-testid="filing-row-0"]')
      .find('[data-testid="filing-date"]')
      .should("be.visible")
      .and("not.have.text", "");
  });
});
