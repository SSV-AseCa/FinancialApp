import { defineConfig } from "cypress";
import { loadEnv } from "vite";

export default defineConfig({
  e2e: {
    baseUrl: process.env.CYPRESS_BASE_URL ?? "http://localhost:3000",
    supportFile: "cypress/support/e2e.ts",
    specPattern: "cypress/e2e/**/*.cy.ts",
    experimentalModifyObstructiveThirdPartyCode: true,
    setupNodeEvents(on, config) {
      const mode = process.env.NODE_ENV ?? "development";
      const env = loadEnv(mode, config.projectRoot, ["VITE_", "TEST_"]);

      config.env.auth0_domain = env.VITE_AUTH0_DOMAIN;
      config.env.auth0_audience = env.VITE_AUTH0_AUDIENCE;
      config.env.auth0_client_id = env.VITE_AUTH0_CLIENT_ID;
      // Fall back to process.env so CI can inject these without a .env file
      config.env.auth0_username = env.TEST_USER_EMAIL ?? process.env.TEST_USER_EMAIL;
      config.env.auth0_password = env.TEST_USER_PASSWORD ?? process.env.TEST_USER_PASSWORD;
      config.env.api_url = env.VITE_API_BASE_URL ?? "http://localhost:8080";

      return config;
    },
  },
});
