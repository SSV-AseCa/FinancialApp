import { SSV_TOKEN_STORAGE_KEY } from "@ssv/ui-core";

const TODAY = new Date().toISOString().slice(0, 10);

describe("Portfolio Management", () => {
  function getToken(): Cypress.Chainable<string> {
    return cy.window().its("localStorage").invoke("getItem", SSV_TOKEN_STORAGE_KEY) as Cypress.Chainable<string>;
  }

  function clearPositions() {
    getToken().then((token) => {
      cy.request({
        method: "GET",
        url: `${Cypress.expose("api_url")}/portfolio`,
        headers: { Authorization: `Bearer ${token}` },
      }).then(({ body }) => {
        (body.positions ?? []).forEach(({ id }: { id: string }) => {
          cy.request({
            method: "DELETE",
            url: `${Cypress.expose("api_url")}/portfolio/positions/${id}`,
            headers: { Authorization: `Bearer ${token}` },
            failOnStatusCode: false,
          });
        });
      });
    });
  }

  function addPositionViaApi(ticker: string, quantity: number) {
    getToken().then((token) => {
      cy.request({
        method: "POST",
        url: `${Cypress.expose("api_url")}/portfolio/positions`,
        headers: { Authorization: `Bearer ${token}` },
        body: { ticker, quantity, operationDate: TODAY },
      });
    });
  }

  beforeEach(() => {
    cy.loginByAuth0Api();
    cy.visit("/portfolio");
    cy.get('[data-testid="portfolio-page"]').should("be.visible");
    clearPositions();
    cy.reload();
    cy.get('[data-testid="portfolio-page"]').should("be.visible");
  });

  it("View Portfolio — empty state: shows empty message when no positions", () => {
    cy.get('[data-testid="portfolio-empty"]').should("be.visible");
  });

  it("View Portfolio — happy path: authenticated investor sees their positions", () => {
    addPositionViaApi("AAPL", 10);
    cy.reload();
    cy.get('[data-testid="portfolio-positions"]').should("be.visible");
    cy.contains("AAPL").should("be.visible");
  });

  it("Add Position — happy path: form submitted → position appears in portfolio", () => {
    cy.get('[data-testid="add-position-button"]').click();
    cy.get('[data-testid="add-position-form"]').should("be.visible");

    cy.get('[data-testid="add-ticker-input"]').type("MSFT");
    cy.get('[data-testid="add-quantity-input"]').clear().type("5");
    cy.get('[data-testid="add-date-input"]').invoke("val", TODAY).trigger("change");

    cy.get('[data-testid="confirm-add-position-button"]').click();

    cy.get('[data-testid="portfolio-positions"]').should("be.visible");
    cy.contains("MSFT").should("be.visible");
  });

  it("Modify Position — happy path: edit form submitted → updated position shown", () => {
    addPositionViaApi("AAPL", 10);
    cy.reload();
    cy.get('[data-testid="portfolio-positions"]').should("be.visible");

    cy.get('[data-testid^="position-row-"]').first().trigger("mouseover");
    cy.get('[data-testid^="edit-position-"]').first().click({ force: true });

    cy.get('[data-testid="edit-quantity-input"]').clear().type("20");
    cy.get('[data-testid="save-position-button"]').click();

    cy.contains("20").should("be.visible");
  });

  it("Remove Position — happy path: position deleted and disappears from list", () => {
    addPositionViaApi("AAPL", 10);
    cy.reload();
    cy.get('[data-testid="portfolio-positions"]').should("be.visible");

    cy.get('[data-testid^="position-row-"]').first().trigger("mouseover");
    cy.get('[data-testid^="remove-position-"]').first().click({ force: true });

    cy.get('[data-testid="portfolio-empty"]').should("be.visible");
  });

  it("View Total Value — empty state: investor with no positions sees a zero total value", () => {
    cy.get('[data-testid="portfolio-empty"]').should("be.visible");
    cy.get('[data-testid="portfolio-total-value"]')
      .should("be.visible")
      .and("contain", "$0.00");
  });

  it("View Total Value — happy path: investor with positions sees a non-zero total value", () => {
    addPositionViaApi("AAPL", 10);
    cy.reload();
    cy.get('[data-testid="portfolio-positions"]').should("be.visible");
    cy.get('[data-testid="portfolio-total-value"]')
      .should("be.visible")
      .and("not.contain", "$0.00");
  });
});
