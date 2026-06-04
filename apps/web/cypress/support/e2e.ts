/* eslint-disable @typescript-eslint/no-namespace */
/// <reference types="cypress" />
// Global Cypress support file — runs before every test file.
import "./commands";

declare global {
  namespace Cypress {
    interface Chainable {
      /**
       * Authenticates by injecting a mock token directly into localStorage,
       * bypassing the UI and any real Auth0 network calls.
       */
      loginByAuth0Api(): Chainable<Cypress.AUTWindow>;
    }
  }
}
