Cypress.Commands.add('loginByAuth0Api', () => {
  const log = Cypress.log({
    displayName: 'Auth0 Login',
    message: ['Authenticating via Management API'],
    autoEnd: false,
  });

  const domain = Cypress.env('auth0_domain');
  const clientId = Cypress.env('auth0_client_id');
  const audience = Cypress.env('auth0_audience');
  const username = Cypress.env('auth0_username');
  const password = Cypress.env('auth0_password');

  if (!username || !password) {
    throw new Error('TEST_USER_EMAIL and TEST_USER_PASSWORD must be defined in .env for Cypress to authenticate.');
  }

  cy.request({
    method: 'POST',
    url: `https://${domain}/oauth/token`,
    body: {
      grant_type: 'password',
      username,
      password,
      audience,
      scope: 'openid profile email offline_access',
      client_id: clientId,
    },
  }).then(({ body }) => {
    // Auth0 returns access_token. We need to save it where our TokenStore expects it.
    // @ssv/ui-core uses LocalStorageTokenStore with key 'ssv_access_token'
    window.localStorage.setItem('ssv_access_token', body.access_token);
    
    log.snapshot('after');
    log.end();
  });
});

export {};
