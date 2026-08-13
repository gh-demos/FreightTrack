FROM node:20-alpine AS build
WORKDIR /app

ARG NPM_REGISTRY_URL=https://registry.npmjs.org/
ARG NPM_STRICT_SSL=true
ARG PNPM_VERSION=9.7.0

COPY package*.json ./
COPY .npmrc ./.npmrc
RUN npm config set strict-ssl ${NPM_STRICT_SSL} && npm config set registry ${NPM_REGISTRY_URL}
RUN npm install -g pnpm@${PNPM_VERSION} --no-progress --loglevel=warn
RUN pnpm config set strict-ssl ${NPM_STRICT_SSL} && pnpm config set registry ${NPM_REGISTRY_URL} && pnpm install --no-frozen-lockfile --reporter=silent
COPY . .
RUN pnpm run build

FROM nginx:1.27-alpine
COPY --from=build /app/dist/freighttrack-frontend/browser /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
