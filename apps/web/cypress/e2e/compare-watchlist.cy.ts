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
    netIncome: -12000000000,
    assets: 410000000000,
    equity: 110000000000
  }
};

describe("Compare Watchlist Companies E2E Tests (Mocked API)", () => {
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

  it("Scenario 1: Investor selects two watched companies and sees their metrics side by side", () => {
    mockWatchlist.push(AAPL_COMPANY);
    mockWatchlist.push(MSFT_COMPANY);

    const apiUrl = Cypress.expose("api_url");
    // Intercept GET /watchlist to return the list
    cy.intercept("GET", `${apiUrl}/watchlist`, {
      statusCode: 200,
      body: mockWatchlist
    }).as("getWatchlist");

    // Intercept GET /watchlist/compare to return comparison data
    cy.intercept(
      "GET",
      `${apiUrl}/watchlist/compare?ciks=${encodeURIComponent(`${AAPL_CIK},${MSFT_CIK}`)}`,
      {
        statusCode: 200,
        body: {
          companies: [AAPL_COMPANY, MSFT_COMPANY]
        }
      }
    ).as("compareWatchlist");

    visitWatchlistPage();
    cy.wait("@getWatchlist");

    // Initially compare button is disabled (0 selected)
    cy.get('[data-testid="compare-button"]').should("be.disabled");

    // Select Apple
    cy.get(`[data-testid="compare-select-${AAPL_CIK}"]`).check();
    cy.get('[data-testid="compare-button"]').should("be.disabled"); // Only 1 selected, still disabled

    // Select Microsoft
    cy.get(`[data-testid="compare-select-${MSFT_CIK}"]`).check();
    cy.get('[data-testid="compare-button"]').should("not.be.disabled"); // 2 selected, now enabled

    // Verify Clear All button deselects correctly
    cy.contains("Clear All").should("be.visible").click();
    cy.get(`[data-testid="compare-select-${AAPL_CIK}"]`).should("not.be.checked");
    cy.get(`[data-testid="compare-select-${MSFT_CIK}"]`).should("not.be.checked");
    cy.get('[data-testid="compare-button"]').should("be.disabled");

    // Reselect both to proceed to comparison
    cy.get(`[data-testid="compare-select-${AAPL_CIK}"]`).check();
    cy.get(`[data-testid="compare-select-${MSFT_CIK}"]`).check();

    // Click compare
    cy.get('[data-testid="compare-button"]').click();
    
    // Verify loading skeleton is shown during fetch
    cy.get('[data-testid="comparison-loading"]').should("be.visible");
    cy.wait("@compareWatchlist");
    cy.get('[data-testid="comparison-loading"]').should("not.exist");

    // Verify side-by-side comparison values
    cy.get('[data-testid="comparison-view"]').should("be.visible");
    cy.get('[data-testid="comparison-view"]').contains("Company Comparison");

    // Verify Apple metrics in comparison and check if Apple is winner of Revenue and Net Income
    cy.get(`[data-testid="compare-revenue-${AAPL_CIK}"]`).should("contain", "$391.00B").find('svg').should('exist');
    cy.get(`[data-testid="compare-net-income-${AAPL_CIK}"]`).should("contain", "$93.00B").find('svg').should('exist');
    cy.get(`[data-testid="compare-assets-${AAPL_CIK}"]`).should("contain", "$365.00B").find('svg').should('not.exist');
    cy.get(`[data-testid="compare-equity-${AAPL_CIK}"]`).should("contain", "$62.00B").find('svg').should('not.exist');

    // Verify Microsoft metrics in comparison and check if Microsoft is winner of Assets and Equity
    cy.get(`[data-testid="compare-revenue-${MSFT_CIK}"]`).should("contain", "$245.00B").find('svg').should('not.exist');
    cy.get(`[data-testid="compare-net-income-${MSFT_CIK}"]`).should("contain", "$-12.00B").find('svg').should('not.exist');
    cy.get(`[data-testid="compare-assets-${MSFT_CIK}"]`).should("contain", "$410.00B").find('svg').should('exist');
    cy.get(`[data-testid="compare-equity-${MSFT_CIK}"]`).should("contain", "$110.00B").find('svg').should('exist');

    // Close comparison and verify it is hidden
    cy.get('[data-testid="close-comparison"]').click();
    cy.get('[data-testid="comparison-view"]').should("not.exist");
  });

  it("Scenario 2: Investor with only one watched company cannot trigger a comparison", () => {
    mockWatchlist.push(AAPL_COMPANY);

    const apiUrl = Cypress.expose("api_url");
    cy.intercept("GET", `${apiUrl}/watchlist`, {
      statusCode: 200,
      body: mockWatchlist
    }).as("getWatchlist");

    visitWatchlistPage();
    cy.wait("@getWatchlist");

    // Select the only watched company
    cy.get(`[data-testid="compare-select-${AAPL_CIK}"]`).check();

    // Verify that the compare button remains disabled because at least two selections are required
    cy.get('[data-testid="compare-button"]').should("be.disabled");
  });
});
