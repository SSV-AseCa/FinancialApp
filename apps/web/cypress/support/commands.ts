Cypress.Commands.add("loginByAuth0Api", () => {
  const log = Cypress.log({
    displayName: "Auth0 Login",
    message: ["Authenticating via Authentication API"],
    autoEnd: false,
  });

  const domain = Cypress.env("auth0_domain");
  const clientId = Cypress.env("auth0_client_id");
  const audience = Cypress.env("auth0_audience");
  const username = Cypress.env("auth0_username");
  const password = Cypress.env("auth0_password");

  if (!domain || !clientId || !username || !password) {
    throw new Error(
      "Auth0 domain, client_id, username, and password must be defined in Cypress environment to authenticate.",
    );
  }

  cy.request({
    method: "POST",
    url: `https://${domain}/oauth/token`,
    body: {
      grant_type: "password",
      username,
      password,
      audience,
      scope: "openid profile email",
      client_id: clientId,
    },
  }).then(({ body }) => {
    // Auth0 returns access_token. We need to save it where our TokenStore expects it.
    // @ssv/ui-core uses LocalStorageTokenStore with key 'ssv_access_token'
    // To ensure we write to the application origin's localStorage (and not about:blank),
    // we visit the base url and set localStorage during onBeforeLoad.
    cy.visit("/", {
      onBeforeLoad(win) {
        win.localStorage.setItem("ssv_access_token", body.access_token);
      },
      log: false,
    }).then(() => {
      log.snapshot("after");
      log.end();
    });
  });
});

export {};
