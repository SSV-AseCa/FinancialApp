FROM node:24-alpine AS build
RUN corepack enable && corepack prepare pnpm@9 --activate
WORKDIR /app

# Workspace manifest and root lockfile — changes here bust the install cache
COPY pnpm-workspace.yaml package.json pnpm-lock.yaml ./
COPY packages/ui-core/package.json packages/ui-core/
COPY apps/web/package.json apps/web/

RUN pnpm install --frozen-lockfile --filter ssv-web...

# Source copied after install so the layer above is cached on manifest-only changes.
# ui-core/dist is pre-built by CI and present in the build context.
COPY packages/ui-core packages/ui-core
COPY apps/web apps/web

RUN pnpm --filter ssv-web build

FROM nginx:alpine
COPY --from=build /app/apps/web/dist /usr/share/nginx/html
COPY apps/web/nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
