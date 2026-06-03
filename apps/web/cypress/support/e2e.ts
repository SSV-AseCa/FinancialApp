/* eslint-disable @typescript-eslint/no-namespace */
// Global Cypress support file — runs before every test file.
import "./commands";

declare global {
  namespace Cypress {
    interface Chainable {
      /**
       * Authenticates with Auth0 programmatically (mocked/simulated)
       * and injects the token into localStorage, bypassing the UI.
       */
      loginByAuth0Api(): Chainable<void>;
    }
  }
}
