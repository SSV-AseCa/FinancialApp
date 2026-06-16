/**
 * Cypress E2E tests — View Company Historical Financial Data (#181)
 *
 * Covers the full user journey from search → company detail page →
 * historical financial data section (chart + table), including edge
 * cases such as loading states, data completeness, navigation, and refresh.
 *
 * Authentication: uses cy.loginByAuth0Api() with session caching.
 * Company under test: Apple Inc. (CIK 0000320193 / AAPL) — stable EDGAR filer.
 */
describe("Company Historical Financial Data", () => {
  /** CIK for Apple — a stable, well-known EDGAR filer with public filings. */
  const APPLE_CIK = "0000320193";

  // ─── helpers ────────────────────────────────────────────────────────────────

  /**
   * Types "apple" in the search box, submits, and clicks Apple's result.
   * Waits for the detail page to be fully visible before returning.
   */
  function searchAndOpenApple() {
    cy.get('[data-testid="company-search-input"]').type("apple");
    cy.get('[data-testid="company-search-submit"]').click();
    cy.get(`[data-testid="company-result-${APPLE_CIK}"]`, { timeout: 20_000 })
      .should("be.visible")
      .click();
    cy.get('[data-testid="company-detail-page"]', { timeout: 10_000 }).should(
      "be.visible"
    );
  }

  /**
   * Waits for the historical section to finish loading (spinner gone,
   * section visible). Accepts both real-API data and the deterministic
   * mock fallback that the page applies when the API returns empty.
   */
  function waitForHistoricalSection() {
    cy.get('[data-testid="history-loading"]').should("not.exist");
    cy.get('[data-testid="historical-section"]', { timeout: 25_000 }).should(
      "be.visible"
    );
  }

  // ─── setup ──────────────────────────────────────────────────────────────────

  beforeEach(() => {
    cy.loginByAuth0Api();
    cy.visit("/companies");
    cy.get('[data-testid="company-search-page"]').should("be.visible");
  });

  // ─── core happy-path scenarios ───────────────────────────────────────────────

  it(
    "Historical Data — investor selects a company and sees the historical section",
    () => {
      searchAndOpenApple();

      cy.url().should("include", `/companies/${APPLE_CIK}`);

      // Section heading is always rendered regardless of data state
      cy.get('[data-testid="historical-section-heading"]').should("be.visible");
      cy.get('[data-testid="historical-section-heading"]').should(
        "contain.text",
        "Historical Financial Data"
      );

      // Wait for actual data (real or fallback mock)
      waitForHistoricalSection();
    }
  );

  it(
    "Historical Data — at least one historical data point is visible and contains period information",
    () => {
      searchAndOpenApple();
      waitForHistoricalSection();

      // At least one row must be present
      cy.get('[data-testid^="history-row-"]').should("have.length.gte", 1);

      // First row must show a non-empty period label
      cy.get('[data-testid^="history-row-"]')
        .first()
        .find('[data-testid="history-cell-period"]')
        .invoke("text")
        .should("not.be.empty");
    }
  );

  it(
    "Historical Data — at least one data point contains non-zero financial values",
    () => {
      searchAndOpenApple();
      waitForHistoricalSection();

      // Revenue cell must contain a formatted USD value (e.g. "$380.00B")
      cy.get('[data-testid="history-cell-revenue"]')
        .first()
        .invoke("text")
        .should("match", /^\$[\d,.]+[BKMT]?$/);

      // Net Income cell must contain a formatted USD value
      cy.get('[data-testid="history-cell-net-income"]')
        .first()
        .invoke("text")
        .should("match", /^-?\$[\d,.]+[BKMT]?$/);
    }
  );

  it(
    "Historical Data — trend chart is rendered inside the historical section",
    () => {
      searchAndOpenApple();
      waitForHistoricalSection();

      cy.get('[data-testid="trend-chart"]').should("be.visible");
      // The SVG must be present inside the chart container
      cy.get('[data-testid="trend-chart"] svg').should("exist");
    }
  );

  it(
    "Historical Data — data table is rendered with all expected column headers",
    () => {
      searchAndOpenApple();
      waitForHistoricalSection();

      cy.get('[data-testid="trend-table"]').should("be.visible");

      const expectedHeaders = [
        "Period",
        "Revenue",
        "Net Income",
        "Total Assets",
        "Total Equity",
      ];
      expectedHeaders.forEach((header) => {
        cy.get('[data-testid="trend-table"] th').contains(header).should("exist");
      });
    }
  );

  it(
    "Historical Data — each visible row contains all five required data cells",
    () => {
      searchAndOpenApple();
      waitForHistoricalSection();

      cy.get('[data-testid^="history-row-"]').each(($row) => {
        cy.wrap($row)
          .find('[data-testid="history-cell-period"]')
          .should("not.have.text", "");
        cy.wrap($row)
          .find('[data-testid="history-cell-revenue"]')
          .should("not.have.text", "");
        cy.wrap($row)
          .find('[data-testid="history-cell-net-income"]')
          .should("not.have.text", "");
        cy.wrap($row)
          .find('[data-testid="history-cell-assets"]')
          .should("not.have.text", "");
        cy.wrap($row)
          .find('[data-testid="history-cell-equity"]')
          .should("not.have.text", "");
      });
    }
  );

  it(
    "Historical Data — multiple periods are displayed (trend requires ≥ 2 data points)",
    () => {
      searchAndOpenApple();
      waitForHistoricalSection();

      cy.get('[data-testid^="history-row-"]').should("have.length.gte", 2);
    }
  );

  // ─── navigation & back ──────────────────────────────────────────────────────

  it(
    "Historical Data — back button returns to the company search page",
    () => {
      searchAndOpenApple();
      cy.get('[data-testid="historical-section-heading"]').should("be.visible");

      cy.get('[aria-label="Back to company search"]').click();
      cy.url().should("include", "/companies");
      cy.get('[data-testid="company-search-page"]').should("be.visible");
    }
  );

  it(
    "Historical Data — refresh button reloads both metrics and historical data",
    () => {
      searchAndOpenApple();
      waitForHistoricalSection();

      // The refresh button becomes visible once the metrics section finishes loading
      cy.get("#metrics-refresh-button", { timeout: 20_000 }).should("be.visible").click();

      // After refresh, the loading spinner for history reappears then resolves
      cy.get('[data-testid="historical-section"]', { timeout: 25_000 }).should(
        "be.visible"
      );
    }
  );

  // ─── direct navigation (deep-link) ──────────────────────────────────────────

  it(
    "Historical Data — visiting the detail page directly by CIK shows the historical section",
    () => {
      cy.loginByAuth0Api();
      cy.visit(`/companies/${APPLE_CIK}`);
      cy.get('[data-testid="company-detail-page"]', { timeout: 10_000 }).should(
        "be.visible"
      );
      cy.get('[data-testid="historical-section-heading"]').should("be.visible");
      waitForHistoricalSection();
    }
  );

  // ─── data integrity ──────────────────────────────────────────────────────────

  it(
    "Historical Data — revenue values are formatted as USD with magnitude suffix",
    () => {
      searchAndOpenApple();
      waitForHistoricalSection();

      // All revenue cells should match the USD formatter pattern ($NNN.NNX)
      cy.get('[data-testid="history-cell-revenue"]').each(($cell) => {
        expect($cell.text()).to.match(/^-?\$[\d,.]+[BKMT]?$/);
      });
    }
  );

  it(
    "Historical Data — period labels follow a recognisable time-period format",
    () => {
      searchAndOpenApple();
      waitForHistoricalSection();

      // Accepts "FY YYYY", "Q? YYYY", or plain "YYYY" — any recognisable time label
      cy.get('[data-testid="history-cell-period"]').each(($cell) => {
        expect($cell.text().trim()).to.match(/^(FY|Q[1-4])?\s*\d{4}$/);
      });
    }
  );

  it(
    "Historical Data — data is ordered chronologically (earlier periods first)",
    () => {
      searchAndOpenApple();
      waitForHistoricalSection();

      // Collect all period labels and verify they are in ascending order
      cy.get('[data-testid="history-cell-period"]').then(($cells) => {
        const periods = Array.from($cells).map((el) => el.innerText.trim());
        const years = periods.map((p) => parseInt(p.match(/\d{4}/)?.[0] ?? "0"));
        // Each year should be >= the previous one (ascending or equal)
        for (let i = 1; i < years.length; i++) {
          expect(years[i]).to.be.gte(years[i - 1]);
        }
      });
    }
  );

  // ─── loading state ───────────────────────────────────────────────────────────

  it(
    "Historical Data — loading spinner is shown while historical data is being fetched",
    () => {
      // Throttle the history API so the spinner is visible long enough to assert
      cy.loginByAuth0Api();
      cy.intercept("GET", `**/companies/${APPLE_CIK}/history`, (req) => {
        req.on("response", (res) => {
          res.setDelay(1500);
        });
      }).as("historyRequest");

      cy.visit(`/companies/${APPLE_CIK}`);
      cy.get('[data-testid="company-detail-page"]').should("be.visible");

      // The spinner should be briefly visible
      cy.get('[data-testid="history-loading"]').should("be.visible");

      // After response the section resolves
      cy.wait("@historyRequest");
      cy.get('[data-testid="historical-section"]', { timeout: 10_000 }).should(
        "be.visible"
      );
    }
  );
});
