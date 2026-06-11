describe("Portfolio Performance Metrics", () => {
  beforeEach(() => {
    cy.loginByAuth0Api();
    cy.visit("/portfolio");
    cy.get('[data-testid="portfolio-page"]').should("be.visible");
  });

  it("View Portfolio Performance Metrics — panel is visible after login", () => {
    cy.get('[data-testid="performance-metrics-panel"]').should("be.visible");
  });

  it("View Portfolio Performance Metrics — total value and total P&L are displayed and non-empty", () => {
    cy.get('[data-testid="performance-total-value"]')
      .should("be.visible")
      .and("not.have.text", "");

    cy.get('[data-testid="performance-total-pnl"]')
      .should("be.visible")
      .and("not.have.text", "");
  });
});
