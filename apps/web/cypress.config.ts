import { defineConfig } from "cypress";
import { loadEnv } from "vite";

export default defineConfig({
  e2e: {
    baseUrl: process.env.CYPRESS_BASE_URL ?? "http://localhost:3000",
    supportFile: "cypress/support/e2e.ts",
    specPattern: "cypress/e2e/**/*.cy.ts",
    experimentalModifyObstructiveThirdPartyCode: true,
    allowCypressEnv: false,
    setupNodeEvents(on, config) {
      const mode = process.env.NODE_ENV ?? "development";
      const env = loadEnv(mode, config.projectRoot, ["VITE_", "TEST_"]);

      // Sensitive values: stay in Node, read in tests via the async cy.env() command.
      // Fall back to process.env so CI can inject these without a .env file.
      config.env.auth0_username = env.TEST_USER_EMAIL ?? process.env.TEST_USER_EMAIL;
      config.env.auth0_password = env.TEST_USER_PASSWORD ?? process.env.TEST_USER_PASSWORD;

      // Public configuration: safe to expose to the browser, read synchronously via Cypress.expose().
      config.expose.auth0_domain = env.VITE_AUTH0_DOMAIN ?? process.env.VITE_AUTH0_DOMAIN;
      config.expose.api_url = env.VITE_API_BASE_URL ?? "http://localhost:8080";

      return config;
    },
  },
});
