import type { WatchlistCompany } from "@ssv/ui-core";

const AAPL_CIK = "0000320193";
const MSFT_CIK = "0000789019";

const AAPL_COMPANY: WatchlistCompany = {
  companyId: "mock-co-id-" + AAPL_CIK,
  cik: AAPL_CIK,
  symbol: "AAPL",
  name: "Apple Inc.",
  metrics: {
    revenue: 391000000000,
    netIncome: 93000000000,
    assets: 365000000000,
    equity: 62000000000
  }
};

const MSFT_COMPANY: WatchlistCompany = {
  companyId: "mock-co-id-" + MSFT_CIK,
  cik: MSFT_CIK,
  symbol: "MSFT",
  name: "Microsoft Corporation",
  metrics: {
    revenue: 245000000000,
    netIncome: 88000000000,
    assets: 410000000000,
    equity: 110000000000
  }
};

describe("Remove Company from Watchlist E2E Tests (Mocked API)", () => {
  let mockWatchlist: WatchlistCompany[] = [];

  beforeEach(() => {
    mockWatchlist = [];
    cy.loginByAuth0Api();
    const apiUrl = Cypress.expose("api_url");

    // Intercept GET /watchlist to simulate loading watchlist items
    cy.intercept("GET", `${apiUrl}/watchlist`, (req) => {
      req.reply({
        statusCode: 200,
        body: mockWatchlist
      });
    }).as("getWatchlist");

    // Ensure we are logged in and starting from the portfolio page
    cy.visit("/portfolio");
    cy.get('[data-testid="portfolio-page"]').should("be.visible");
  });

  it("Scenario 1: Happy path - Investor removes a watched company and it disappears from the watchlist view", () => {
    // Seed the watchlist with Apple
    mockWatchlist.push(AAPL_COMPANY);

    const apiUrl = Cypress.expose("api_url");
    // Intercept DELETE /watchlist/{cik} to respond with success
    cy.intercept("DELETE", `${apiUrl}/watchlist/${AAPL_CIK}`, (req) => {
      mockWatchlist = mockWatchlist.filter((item) => item.cik !== AAPL_CIK);
      req.reply({
        statusCode: 204
      });
    }).as("removeFromWatchlistSuccess");

    // Visit Watchlist Page
    cy.visit("/watchlist");
    cy.get('[data-testid="watchlist-page"]').should("be.visible");

    // Verify company is visible initially
    cy.get(`[data-testid="watchlist-item-${AAPL_CIK}"]`).should("be.visible");

    // Click remove button
    cy.get(`[data-testid="remove-watchlist-${AAPL_CIK}"]`).click();

    // Verify it calls the DELETE intercept
    cy.wait("@removeFromWatchlistSuccess");

    // Verify it is gone and watchlist empty page is shown (since it was the only item)
    cy.get(`[data-testid="watchlist-item-${AAPL_CIK}"]`).should("not.exist");
    cy.get('[data-testid="watchlist-empty"]').should("be.visible");
  });

  it("Scenario 2: Not on watchlist / Error handling - Attempting to remove returns an error and item remains", () => {
    // Seed the watchlist with Apple
    mockWatchlist.push(AAPL_COMPANY);

    const apiUrl = Cypress.expose("api_url");
    // Intercept DELETE /watchlist/{cik} to respond with a 400 Bad Request error (e.g. not found on watchlist)
    cy.intercept("DELETE", `${apiUrl}/watchlist/${AAPL_CIK}`, {
      statusCode: 400,
      body: { message: "Company not in watchlist" }
    }).as("removeFromWatchlistFail");

    // Visit Watchlist Page
    cy.visit("/watchlist");
    cy.get('[data-testid="watchlist-page"]').should("be.visible");

    // Click remove button
    cy.get(`[data-testid="remove-watchlist-${AAPL_CIK}"]`).click();

    // Verify it calls the DELETE intercept
    cy.wait("@removeFromWatchlistFail");

    // Verify that the UI displays a clean error banner/alert
    cy.get('[data-testid="watchlist-error"]')
      .should("be.visible")
      .contains("Company not in watchlist");

    // Verify the item remains in the watchlist DOM
    cy.get(`[data-testid="watchlist-item-${AAPL_CIK}"]`).should("be.visible");
  });

  it("Scenario 3: Multiple items - Removing one company keeps the other in the list and does not show empty state", () => {
    // Seed the watchlist with Apple and Microsoft
    mockWatchlist.push(AAPL_COMPANY);
    mockWatchlist.push(MSFT_COMPANY);

    const apiUrl = Cypress.expose("api_url");
    // Intercept DELETE /watchlist/{cik} to respond with success
    cy.intercept("DELETE", `${apiUrl}/watchlist/${AAPL_CIK}`, (req) => {
      mockWatchlist = mockWatchlist.filter((item) => item.cik !== AAPL_CIK);
      req.reply({
        statusCode: 204
      });
    }).as("removeFromWatchlistSuccess");

    // Visit Watchlist Page
    cy.visit("/watchlist");
    cy.get('[data-testid="watchlist-page"]').should("be.visible");

    // Verify both companies are visible initially
    cy.get(`[data-testid="watchlist-item-${AAPL_CIK}"]`).should("be.visible");
    cy.get(`[data-testid="watchlist-item-${MSFT_CIK}"]`).should("be.visible");

    // Click remove button for Apple
    cy.get(`[data-testid="remove-watchlist-${AAPL_CIK}"]`).click();

    // Verify it calls the DELETE intercept
    cy.wait("@removeFromWatchlistSuccess");

    // Verify Apple is gone, but Microsoft is still present, and empty state is NOT shown
    cy.get(`[data-testid="watchlist-item-${AAPL_CIK}"]`).should("not.exist");
    cy.get(`[data-testid="watchlist-item-${MSFT_CIK}"]`).should("be.visible");
    cy.get('[data-testid="watchlist-empty"]').should("not.exist");
  });

  it("Scenario 4: Dismiss error banner - Clicking the dismiss button removes the error alert from the UI", () => {
    // Seed the watchlist with Apple
    mockWatchlist.push(AAPL_COMPANY);

    const apiUrl = Cypress.expose("api_url");
    // Intercept DELETE /watchlist/{cik} to respond with a 400 Bad Request error
    cy.intercept("DELETE", `${apiUrl}/watchlist/${AAPL_CIK}`, {
      statusCode: 400,
      body: { message: "Removal error details" }
    }).as("removeFromWatchlistFail");

    // Visit Watchlist Page
    cy.visit("/watchlist");
    cy.get('[data-testid="watchlist-page"]').should("be.visible");

    // Click remove button
    cy.get(`[data-testid="remove-watchlist-${AAPL_CIK}"]`).click();

    // Verify it calls the DELETE intercept and shows the error
    cy.wait("@removeFromWatchlistFail");
    cy.get('[data-testid="watchlist-error"]').should("be.visible");

    // Click the Dismiss button inside the error banner
    cy.get('[data-testid="watchlist-error"]').contains("Dismiss").click();

    // Verify that the error banner is removed from the DOM
    cy.get('[data-testid="watchlist-error"]').should("not.exist");
  });
});
