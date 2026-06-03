/// <reference types="cypress" />

import { SSV_TOKEN_STORAGE_KEY } from "@ssv/ui-core";

Cypress.Commands.add("loginByAuth0Api", () => {
  const log = Cypress.log({
    displayName: "Auth0 Login",
    message: ["Mocking authentication (No test user)"],
    autoEnd: false,
  });

  // To ensure we write to the application origin's localStorage (and not about:blank),
  // we visit the base url and set localStorage during onBeforeLoad.
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
