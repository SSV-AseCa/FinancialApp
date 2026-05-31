type Auth0TokenResponse = {
    access_token: string
    id_token?: string
    token_type: string
    expires_in: number
}

function requiredEnv(name: string): string {
    const value = process.env[name]

    if (!value) {
        throw new Error(`Missing required environment variable: ${name}`)
    }

    return value
}

export async function getAuth0TestAccessToken(): Promise<string> {
    const domain = requiredEnv('AUTH0_DOMAIN')
    const clientId = requiredEnv('AUTH0_TEST_CLIENT_ID')
    const clientSecret = process.env.AUTH0_TEST_CLIENT_SECRET
    const audience = requiredEnv('AUTH0_AUDIENCE')
    const username = requiredEnv('AUTH0_TEST_USERNAME')
    const password = requiredEnv('AUTH0_TEST_PASSWORD')

    const body: Record<string, string> = {
        grant_type: 'password',
        username,
        password,
        audience,
        client_id: clientId,
        scope: 'openid profile email',
    }

    if (clientSecret) {
        body.client_secret = clientSecret
    }

    const response = await fetch(`https://${domain}/oauth/token`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(body),
    })

    if (!response.ok) {
        throw new Error(
            `Auth0 token request failed: ${response.status} ${await response.text()}`,
        )
    }

    const tokenResponse = (await response.json()) as Auth0TokenResponse

    return tokenResponse.access_token
}