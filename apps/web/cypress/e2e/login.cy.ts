<<<<<<< HEAD
=======
import { SSV_TOKEN_STORAGE_KEY } from "@ssv/ui-core";

>>>>>>> 67b62820afddd09a3e2502800c384928ce0b8375
describe("Log In Flow", () => {
  beforeEach(() => {
    // Ensure a clean localStorage state before each test
    cy.clearLocalStorage();
  });

  it("Happy path: programmatic Auth0 login → access token obtained → investor reaches portfolio screen", () => {
    // 1. Log in programmatically (visits "/" and sets the mock token in the app window context)
    cy.loginByAuth0Api();

    // 2. Visit the portfolio screen
    cy.visit("/portfolio");

    // 3. Verify we successfully reach the portfolio screen
    cy.url().should("include", "/portfolio");

    // 4. Verify token is placed in localStorage of the application window
    cy.window().then((win) => {
<<<<<<< HEAD
      const token = win.localStorage.getItem("ssv_access_token");
=======
      const token = win.localStorage.getItem(SSV_TOKEN_STORAGE_KEY);
>>>>>>> 67b62820afddd09a3e2502800c384928ce0b8375
      expect(token).to.be.a("string");
      expect(token?.length).to.be.greaterThan(0);
    });

    // 5. Verify the portfolio page itself is rendered using the data-testid attribute
    cy.get('[data-testid="portfolio-page"]').should("be.visible");
  });

  it("Unauthenticated access: direct navigation to a protected route → redirected to login screen", () => {
    // 1. Visit a protected route without being logged in
    cy.visit("/portfolio");

    // 2. Verify we are redirected to the login screen
    cy.url().should("include", "/login");
  });

  it("Post-login access: authenticated investor can reach protected screens", () => {
    // 1. Log in programmatically
    cy.loginByAuth0Api();

    // 2. Visit the protected screen
    cy.visit("/portfolio");

    // 3. Verify we stay on the portfolio screen and it renders
    cy.url().should("include", "/portfolio");
    cy.get('[data-testid="portfolio-page"]').should("be.visible");
  });
});
