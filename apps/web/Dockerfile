FROM node:24-alpine AS build

RUN corepack enable && corepack prepare pnpm@9 --activate
WORKDIR /app

# Workspace manifests
COPY pnpm-workspace.yaml package.json pnpm-lock.yaml ./
COPY ui-core/package.json ui-core/
COPY web/package.json web/

# Install dependencies for web and its workspace deps
RUN pnpm install --frozen-lockfile --filter ssv-web...

# Copy sources
COPY ui-core ui-core
COPY web web

ARG VITE_AUTH0_DOMAIN
ARG VITE_AUTH0_CLIENT_ID
ARG VITE_AUTH0_AUDIENCE
ARG VITE_API_BASE_URL

ENV VITE_AUTH0_DOMAIN=$VITE_AUTH0_DOMAIN \
  VITE_AUTH0_CLIENT_ID=$VITE_AUTH0_CLIENT_ID \
  VITE_AUTH0_AUDIENCE=$VITE_AUTH0_AUDIENCE \
  VITE_API_BASE_URL=$VITE_API_BASE_URL

# Build ui-core first so dist/index.js and dist/index.d.ts exist
RUN pnpm --filter @ssv/ui-core build

# Then build the web app
RUN pnpm --filter ssv-web build

FROM nginx:alpine

COPY --from=build /app/web/dist /usr/share/nginx/html
COPY web/nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
