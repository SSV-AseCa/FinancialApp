import type { WatchlistCompany } from "@ssv/ui-core";
import { AAPL_CIK, MSFT_CIK, AAPL_COMPANY, MSFT_COMPANY } from "../support/watchlist-fixtures";

describe("View Watchlist Financial Metrics E2E Tests (Mocked API)", () => {
  let mockWatchlist: WatchlistCompany[] = [];

  const visitWatchlistPage = () => {
    cy.visit("/watchlist");
    cy.get('[data-testid="watchlist-page"]').should("be.visible");
  };

  beforeEach(() => {
    mockWatchlist = [];
    cy.loginByAuth0Api();
    
    // Ensure we start from portfolio page to simulate natural flow
    cy.visit("/portfolio");
    cy.get('[data-testid="portfolio-page"]').should("be.visible");
  });

  it("Scenario 1: Empty watchlist view (displays empty state and redirects to search page)", () => {
    const apiUrl = Cypress.expose("api_url");
    cy.intercept("GET", `${apiUrl}/watchlist`, {
      statusCode: 200,
      body: []
    }).as("getEmptyWatchlist");

    visitWatchlistPage();
    cy.wait("@getEmptyWatchlist");

    // Verify empty state container and text
    cy.get('[data-testid="watchlist-empty"]').should("be.visible");
    cy.get('[data-testid="watchlist-empty"]').contains("Your watchlist is currently empty");

    // Verify "Search Companies" button click redirects to search page
    cy.get('[data-testid="watchlist-empty"]').contains("Search Companies").click();
    cy.get('[data-testid="company-search-page"]').should("be.visible");
  });

  it("Scenario 2: Watchlist view with companies (displays all formatted metrics and checks net income color coding)", () => {
    mockWatchlist.push(AAPL_COMPANY);
    mockWatchlist.push(MSFT_COMPANY);

    const apiUrl = Cypress.expose("api_url");
    cy.intercept("GET", `${apiUrl}/watchlist`, {
      statusCode: 200,
      body: mockWatchlist
    }).as("getWatchlistMultiple");

    visitWatchlistPage();
    cy.wait("@getWatchlistMultiple");

    // Verify Apple elements
    cy.get(`[data-testid="watchlist-item-${AAPL_CIK}"]`).within(() => {
      cy.contains("Apple Inc.").should("be.visible");
      cy.contains("AAPL").should("be.visible");
      cy.contains("CIK: " + AAPL_CIK).should("be.visible");

      // Verify formatted metrics
      cy.contains("Revenue").closest("div").should("contain", "$391.00B");
      cy.contains("Net Income").closest("div").should("contain", "$93.00B");
      cy.contains("Assets").closest("div").should("contain", "$365.00B");
      cy.contains("Equity").closest("div").should("contain", "$62.00B");

      // Net income is positive, verify green color styling (e.g. text-emerald-400)
      cy.contains("Net Income").closest("div").find("p").last().should("have.class", "text-emerald-400");
    });

    // Verify Microsoft elements
    cy.get(`[data-testid="watchlist-item-${MSFT_CIK}"]`).within(() => {
      cy.contains("Microsoft Corporation").should("be.visible");
      cy.contains("MSFT").should("be.visible");

      // Verify formatted metrics
      cy.contains("Revenue").closest("div").should("contain", "$245.00B");
      cy.contains("Net Income").closest("div").should("contain", "$-12.00B");
      cy.contains("Assets").closest("div").should("contain", "$410.00B");
      cy.contains("Equity").closest("div").should("contain", "$110.00B");

      // Net income is negative, verify red/rose color styling (e.g. text-rose-400)
      cy.contains("Net Income").closest("div").find("p").last().should("have.class", "text-rose-400");
    });
  });

  it("Scenario 3: Watchlist item navigation (clicking on name navigates to details page)", () => {
    mockWatchlist.push(AAPL_COMPANY);

    const apiUrl = Cypress.expose("api_url");
    cy.intercept("GET", `${apiUrl}/watchlist`, {
      statusCode: 200,
      body: mockWatchlist
    }).as("getWatchlistSingle");

    visitWatchlistPage();
    cy.wait("@getWatchlistSingle");

    // Click Apple company link/header
    cy.get(`[data-testid="watchlist-item-${AAPL_CIK}"]`).contains("Apple Inc.").click();

    // Verify redirection to company details page
    cy.url().should("include", `/companies/${AAPL_CIK}`);
  });

  it("Scenario 4: Error loading watchlist (displays error state, click retry successfully loads list)", () => {
    const apiUrl = Cypress.expose("api_url");
    
    // Intercept first load with error, second load with success
    let shouldFail = true;
    cy.intercept("GET", `${apiUrl}/watchlist`, (req) => {
      if (shouldFail) {
        req.reply({
          statusCode: 500,
          body: { message: "Internal Server Error" }
        });
      } else {
        req.reply({
          statusCode: 200,
          body: [AAPL_COMPANY]
        });
      }
    }).as("getWatchlistRetryFlow");

    cy.visit("/watchlist");
    cy.wait("@getWatchlistRetryFlow");

    // Verify error banner state is displayed with a "Retry" button
    cy.contains("Failed to load watchlist").should("be.visible");
    cy.contains("Internal Server Error").should("be.visible");
    
    // Change the flag so subsequent requests (retries) will succeed
    cy.then(() => {
      shouldFail = false;
    });

    // Click retry
    cy.contains("Retry").click();
    cy.wait("@getWatchlistRetryFlow");

    // Verify watchlist page loaded successfully and displays Apple
    cy.get(`[data-testid="watchlist-item-${AAPL_CIK}"]`).should("be.visible");
  });
});
