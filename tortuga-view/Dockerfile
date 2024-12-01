# Multistage Dockerfile: Stage 1
# "as builder" just names this stage "builder" to use that name below later
FROM node:10-alpine as builder
# Set the working directory aka all following commands will be executed from here.
WORKDIR /app
# Copies all files from your local project directory (where this Dockerfile resides) to the /app directory inside the container.
COPY . /app
# Installs bower and gulp globally inside the container.
# Bower: A package manager for web assets.
# Gulp: A task runner used for automating build tasks (like minifying files, compiling CSS, etc.).
RUN npm install -g bower gulp
# Installs all dependencies defined in the package.json file
RUN npm install
# Executes the gulp build task to build the production-ready frontend files.
RUN gulp build

# Stage 2
# Starts a new stage based on Nginx (also built on Alpine Linux).
FROM nginx:alpine
# Copies a custom Nginx configuration file (nginx-default.conf) from this project to the container's Nginx configuration directory.
# It listens on port 80 and serves static files from /usr/share/nginx/html. If a requested file is not found ($uri), it falls back to serving index.html
ADD nginx-default.conf /etc/nginx/conf.d/default.conf
# Copies the built files from the builder stage (/app) to the Nginx web root (/usr/share/nginx/html).
# Only the static files needed for the frontend app are included, excluding development dependencies and build tools, resulting in a smaller, more efficient image.
COPY --from=builder /app /usr/share/nginx/html