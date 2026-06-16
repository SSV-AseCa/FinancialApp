import type { WatchlistCompany } from "@ssv/ui-core";

describe("Add Company to Watchlist E2E Tests (Mocked API)", () => {
  const AAPL_CIK = "0000320193";
  let mockWatchlist: WatchlistCompany[] = [];

  beforeEach(() => {
    mockWatchlist = [];
    cy.loginByAuth0Api();
    const apiUrl = Cypress.expose("api_url");

    // Intercept GET /watchlist to simulate loading the list of watched companies
    cy.intercept("GET", `${apiUrl}/watchlist`, (req) => {
      req.reply({
        statusCode: 200,
        body: mockWatchlist
      });
    }).as("getWatchlist");

    // Intercept POST /watchlist to simulate successfully adding a company
    cy.intercept("POST", `${apiUrl}/watchlist`, (req) => {
      const { cik } = req.body;
      const newEntry = {
        id: "mock-entry-id-" + cik,
        companyId: "mock-co-id-" + cik,
        cik: cik
      };

      // Add to mock watchlist for subsequent GET requests
      if (!mockWatchlist.some((item) => item.cik === cik)) {
        mockWatchlist.push({
          companyId: "mock-co-id-" + cik,
          cik: cik,
          symbol: "AAPL",
          name: "Apple Inc.",
          metrics: {
            revenue: 391000000000,
            netIncome: 93000000000,
            assets: 365000000000,
            equity: 62000000000
          }
        });
      }

      req.reply({
        statusCode: 201,
        body: newEntry
      });
    }).as("addToWatchlist");

    // Ensure we are logged in and starting from the portfolio page
    cy.visit("/portfolio");
    cy.get('[data-testid="portfolio-page"]').should("be.visible");
  });

  it("Scenario 1: Happy path - Search AAPL, add to watchlist, and verify it appears in the watchlist view", () => {
    // Navigate to Company Search
    cy.visit("/companies");
    cy.get('[data-testid="company-search-page"]').should("be.visible");

    // Search for AAPL
    cy.get('[data-testid="company-search-input"]').type("apple");
    cy.get('[data-testid="company-search-submit"]').click();

    // Verify search result appears and click it
    cy.get(`[data-testid="company-result-${AAPL_CIK}"]`, { timeout: 15000 })
      .should("be.visible")
      .click();

    // Verify detail page loaded and show "Add to Watchlist" button
    cy.get('[data-testid="company-detail-page"]').should("be.visible");
    cy.get('[data-testid="add-watchlist-button"]')
      .should("be.visible")
      .should("not.be.disabled")
      .click();

    // Verify success state badge
    cy.get('[data-testid="watching-badge"]', { timeout: 10000 })
      .should("be.visible")
      .contains("Watching");

    // Navigate to Watchlist page to verify it appears in the watchlist view
    cy.visit("/watchlist");
    cy.get('[data-testid="watchlist-page"]').should("be.visible");

    // Verify AAPL is in the watchlist
    cy.get(`[data-testid="watchlist-item-${AAPL_CIK}"]`).should("be.visible");
    cy.get(`[data-testid="watchlist-item-${AAPL_CIK}"]`).contains("Apple");
  });

  it("Scenario 2: Already watched - shows as Watching if the company is already watched", () => {
    // Pre-populate mock watchlist to simulate company already watched
    mockWatchlist.push({
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
    });

    // Go to search page and search AAPL
    cy.visit("/companies");
    cy.get('[data-testid="company-search-input"]').type("apple");
    cy.get('[data-testid="company-search-submit"]').click();
    
    // Click result to navigate to details
    cy.get(`[data-testid="company-result-${AAPL_CIK}"]`, { timeout: 15000 }).click();

    // Verify detail page directly shows already watched state on load and does not show add button
    cy.get('[data-testid="watching-badge"]').should("be.visible");
    cy.get('[data-testid="add-watchlist-button"]').should("not.exist");
  });
});
