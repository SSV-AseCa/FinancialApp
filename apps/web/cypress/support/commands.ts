/// <reference types="cypress" />

import { SSV_TOKEN_STORAGE_KEY } from "@ssv/ui-core";

Cypress.Commands.add("loginByAuth0Api", () => {
  // Credentials are sensitive: read asynchronously via cy.env() so they stay in Node
  // and are never hydrated into the browser context.
  cy.env(["auth0_username", "auth0_password"]).then(({ auth0_username, auth0_password }) => {
    cy.session(
      auth0_username,
      () => {
        cy.visit("/login");
        cy.get("#login-button").click();

        cy.origin(
          // auth0_domain is public configuration, exposed synchronously.
          `https://${Cypress.expose("auth0_domain")}`,
          {
            args: {
              username: auth0_username as string,
              password: auth0_password as string,
            },
          },
          ({ username, password }) => {
            cy.get("input[name=username]", { timeout: 10000 }).type(username);
            cy.get("input[name=password]").type(password, { log: false });
            cy.get("button[type=submit]").first().click();
          },
        );

        cy.url({ timeout: 15000 }).should("include", "/portfolio");
      },
    );
  });
});

export {};

// Re-exported so specs can reference the key without importing ui-core directly
export { SSV_TOKEN_STORAGE_KEY };
