/// <reference types="cypress" />

import { SSV_TOKEN_STORAGE_KEY } from "@ssv/ui-core";

Cypress.Commands.add("loginByAuth0Api", () => {
  cy.session(
    Cypress.env("auth0_username"),
    () => {
      cy.visit("/login");
      cy.get("#login-button").click();

      cy.origin(
        `https://${Cypress.env("auth0_domain")}`,
        {
          args: {
            username: Cypress.env("auth0_username") as string,
            password: Cypress.env("auth0_password") as string,
          },
        },
        ({ username, password }) => {
          // Auth0 Universal Login: email step then password step
          cy.get("input[name=username]", { timeout: 10000 }).type(username);
          cy.get("button[type=submit]").click();
          cy.get("input[name=password]", { timeout: 10000 }).type(password, { log: false });
          cy.get("button[type=submit]").click();
        },
      );

      cy.url({ timeout: 15000 }).should("include", "/portfolio");
    },
  );
});

export {};

// Re-exported so specs can reference the key without importing ui-core directly
export { SSV_TOKEN_STORAGE_KEY };
