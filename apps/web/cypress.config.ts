import { defineConfig } from "cypress";
import { loadEnv } from "vite";

export default defineConfig({
  e2e: {
    baseUrl: process.env.CYPRESS_BASE_URL ?? "http://localhost:3000",
    supportFile: "cypress/support/e2e.ts",
    specPattern: "cypress/e2e/**/*.cy.ts",
    setupNodeEvents(on, config) {
      // Load environment variables using Vite
      const env = loadEnv("", config.projectRoot, "");

      config.env.auth0_domain = env.VITE_AUTH0_DOMAIN;
      config.env.auth0_audience = env.VITE_AUTH0_AUDIENCE;
      config.env.auth0_client_id = env.VITE_AUTH0_CLIENT_ID;
      config.env.auth0_username = env.TEST_USER_EMAIL;
      config.env.auth0_password = env.TEST_USER_PASSWORD;

      return config;
    },
  },
});
