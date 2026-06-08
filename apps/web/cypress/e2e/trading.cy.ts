import { SSV_TOKEN_STORAGE_KEY } from "@ssv/ui-core";

const APPLE_CIK = "0000320193";

describe("Trading", () => {
  function buyViaApi(cik: string, quantity: number) {
    cy.window().its("localStorage").invoke("getItem", SSV_TOKEN_STORAGE_KEY).then((token) => {
      cy.request({
        method: "POST",
        url: `${Cypress.env("api_url")}/portfolio/transactions/buy`,
        headers: { Authorization: `Bearer ${token}` },
        body: { cik, quantity },
        failOnStatusCode: false,
      });
    });
  }

  beforeEach(() => {
    cy.loginByAuth0Api();
    cy.visit("/trading");
    cy.get('[data-testid="trading-page"]').should("be.visible");
  });

  it("Buy Shares — validation: missing fields show error", () => {
    cy.get('[data-testid="buy-submit-button"]').click();
    cy.get('[role="alert"]').first().should("be.visible");
  });

  it("Sell Shares — validation: missing fields show error", () => {
    cy.get('[data-testid="sell-submit-button"]').click();
    cy.get('[role="alert"]').last().should("be.visible");
  });

  it("Buy Shares — happy path: buy creates transaction in history", () => {
    cy.get('[data-testid="buy-cik-input"]').type(APPLE_CIK);
    cy.get('[data-testid="buy-quantity-input"]').clear().type("5");
    cy.get('[data-testid="buy-submit-button"]').click();

    cy.get('[data-testid="buy-success"]', { timeout: 15000 }).should("be.visible");
    cy.get('[data-testid="transactions-list"]').should("be.visible");
    cy.contains("BUY").should("be.visible");
  });

  it("Sell Shares — happy path: sell creates transaction in history", () => {
    // Seed a buy so there is inventory to sell
    buyViaApi(APPLE_CIK, 10);
    cy.reload();
    cy.get('[data-testid="trading-page"]').should("be.visible");

    cy.get('[data-testid="sell-cik-input"]').type(APPLE_CIK);
    cy.get('[data-testid="sell-quantity-input"]').clear().type("5");
    cy.get('[data-testid="sell-submit-button"]').click();

    cy.get('[data-testid="sell-success"]', { timeout: 15000 }).should("be.visible");
    cy.get('[data-testid="transactions-list"]').should("be.visible");
    cy.contains("SELL").should("be.visible");
  });

  it("View Transaction History — shows transactions list after activity", () => {
    buyViaApi(APPLE_CIK, 3);
    cy.reload();
    cy.get('[data-testid="trading-page"]').should("be.visible");
    cy.get('[data-testid="transactions-list"]').should("be.visible");
  });
});
