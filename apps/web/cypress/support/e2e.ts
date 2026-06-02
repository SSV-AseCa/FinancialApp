// Global Cypress support file — runs before every test file.
import './commands';

declare global {
// eslint-disable-next-line @typescript-eslint/no-namespace
  namespace Cypress {
    interface Chainable {
      /**
       * Authenticates with Auth0 programmatically using the Password grant type
       * and injects the token into localStorage, bypassing the UI.
       */
      loginByAuth0Api(): Chainable<void>;
    }
  }
}
