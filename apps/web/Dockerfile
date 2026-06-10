FROM node:24-alpine AS build
RUN corepack enable && corepack prepare pnpm@9 --activate
WORKDIR /app

# Workspace manifest and lockfile — changes here bust the install cache
COPY pnpm-workspace.yaml package.json pnpm-lock.yaml ./
COPY ui-core/package.json ui-core/
COPY web/package.json web/

RUN pnpm install --frozen-lockfile --filter ssv-web...

# Source copied after install so the layer above is cached on manifest-only changes.
# ui-core/dist is pre-built by CI and present in the build context.
COPY ui-core ui-core
COPY web web

ARG VITE_AUTH0_DOMAIN
ARG VITE_AUTH0_CLIENT_ID
ARG VITE_AUTH0_AUDIENCE
ARG VITE_API_BASE_URL

# Promote build args to environment variables so Vite's loadEnv picks them up
# from process.env during `vite build`. Without this, the ARGs are inert for the
# build and values only leak in via a copied .env file (absent in CI).
ENV VITE_AUTH0_DOMAIN=$VITE_AUTH0_DOMAIN \
    VITE_AUTH0_CLIENT_ID=$VITE_AUTH0_CLIENT_ID \
    VITE_AUTH0_AUDIENCE=$VITE_AUTH0_AUDIENCE \
    VITE_API_BASE_URL=$VITE_API_BASE_URL

RUN pnpm --filter ssv-web build

FROM nginx:alpine
COPY --from=build /app/web/dist /usr/share/nginx/html
COPY web/nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
