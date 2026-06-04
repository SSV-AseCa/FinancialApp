/// <reference types="cypress" />

import { SSV_TOKEN_STORAGE_KEY } from "@ssv/ui-core";

Cypress.Commands.add("loginByAuth0Api", () => {
  const log = Cypress.log({
    displayName: "Auth0 Login",
    message: ["Mocking authentication (no test user required)"],
    autoEnd: false,
  });

  // Injects a mock token directly into localStorage during the page load,
  // under the correct application origin (not about:blank).
  // This bypasses the need for real Auth0 credentials or the deprecated ROPC grant.
  return cy
    .visit("/", {
      onBeforeLoad(win) {
        win.localStorage.setItem(SSV_TOKEN_STORAGE_KEY, "mock-access-token");
      },
      log: false,
    })
    .then(() => {
      log.snapshot("after");
      log.end();
    });
});

export {};
