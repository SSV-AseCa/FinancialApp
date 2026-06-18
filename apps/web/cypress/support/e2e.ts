/* eslint-disable @typescript-eslint/no-namespace */
/// <reference types="cypress" />
// Global Cypress support file — runs before every test file.
import "./commands";

declare global {
  namespace Cypress {
    interface Chainable {
      /**
       * Authenticates through Auth0's real login page using cy.origin().
       * Caches the session across tests so login only runs once per spec run.
       */
      loginByAuth0Api(): void;
    }
  }
}
