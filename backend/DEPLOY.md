# Deployment Guide

This project uses:

- JDK 25
- Spring Boot 4.0.6
- Nginx for public traffic
- Docker for the backend container
- MySQL as the database

The current frontend files are stored in `src/main/resources/static`. Your
deployment goal is:

1. Let Nginx serve the frontend static files.
2. Let Nginx reverse proxy `/api/` to the Spring Boot container.
3. Build the backend as an image, push it to a registry, then pull and run it
   on the server.

## Recommended topology

```text
Browser
  -> Nginx :80 / :443
     -> /pages/* /css/* /js/* from /var/www/my-blog
     -> /api/* proxy to 127.0.0.1:8080
        -> Docker container: my-blog
           -> MySQL
```

This topology is a good fit for your current project because the frontend is
pure static HTML/CSS/JavaScript and the backend API is already separated under
`/api`.

## 1. Prepare MySQL

Create the database:

```sql
CREATE DATABASE my_blog
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

Import the schema during first-time initialization:

```bash
mysql -uroot -p my_blog < schema.sql
```

Important:

- `schema.sql` contains destructive statements such as `DROP TABLE IF EXISTS`.
- Only run it for initial setup or when you explicitly want to rebuild the
  schema.

## 2. Build and push the backend image

The Dockerfile already builds with JDK 25 and runs with JRE 25.

On your build machine:

```bash
cd backend
docker build -t your-registry/my-blog:1.0.0 .
docker push your-registry/my-blog:1.0.0
```

Example:

```bash
docker build -t registry.cn-hangzhou.aliyuncs.com/your-namespace/my-blog:1.0.0 .
docker push registry.cn-hangzhou.aliyuncs.com/your-namespace/my-blog:1.0.0
```

## 3. Run the backend container on the server

### Option A: use `docker run`

```bash
docker pull your-registry/my-blog:1.0.0

docker run -d \
  --name my-blog \
  --restart unless-stopped \
  -p 127.0.0.1:8080:8080 \
  -e APP_DATASOURCE_URL='jdbc:mysql://your-mysql-host:3306/my_blog?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai&characterEncoding=utf8' \
  -e APP_DATASOURCE_USERNAME='root' \
  -e APP_DATASOURCE_PASSWORD='change-me' \
  -e SERVER_PORT='8080' \
  -e APP_SESSION_TIMEOUT='7d' \
  your-registry/my-blog:1.0.0
```

Why bind to `127.0.0.1:8080`:

- The backend only needs to be reachable by local Nginx.
- This avoids exposing port `8080` directly to the public network.

### Option B: use Docker Compose

On the server:

1. Create a working directory such as `/opt/my-blog`.
2. Copy `deploy/docker-compose.image.yml.example` to `docker-compose.yml`.
3. Copy `.env.example` to `.env`.
4. Fill in the real image name and database credentials.
5. Start the service.

Example:

```bash
mkdir -p /opt/my-blog
cd /opt/my-blog
```

`docker-compose.yml` example:

```yaml
services:
  my-blog:
    image: your-registry/my-blog:1.0.0
    container_name: my-blog
    restart: unless-stopped
    ports:
      - "127.0.0.1:8080:8080"
    env_file:
      - .env
    environment:
      APP_DATASOURCE_URL: ${APP_DATASOURCE_URL}
      APP_DATASOURCE_USERNAME: ${APP_DATASOURCE_USERNAME}
      APP_DATASOURCE_PASSWORD: ${APP_DATASOURCE_PASSWORD}
      SERVER_PORT: ${SERVER_PORT:-8080}
      APP_SESSION_TIMEOUT: ${APP_SESSION_TIMEOUT:-7d}
```

`.env` example:

```env
APP_DATASOURCE_URL=jdbc:mysql://your-mysql-host:3306/my_blog?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai&characterEncoding=utf8
APP_DATASOURCE_USERNAME=root
APP_DATASOURCE_PASSWORD=replace-with-a-strong-password
SERVER_PORT=8080
APP_SESSION_TIMEOUT=7d
```

Start the backend:

```bash
docker compose up -d
docker compose ps
docker compose logs -f
```

## 4. Deploy the frontend files into Nginx

This is the part you asked for in detail.

Because the frontend files are in `backend/src/main/resources/static`, there
are two ways to place them into Nginx:

- Copy them directly from source code.
- Extract them from the built image.

For your planned release flow, the image-extraction method is better, because
the server only needs Docker and does not need the full source code repository.

### Recommended method: extract frontend files from the image

After `docker pull` on the server:

```bash
docker pull your-registry/my-blog:1.0.0
docker create --name my-blog-static your-registry/my-blog:1.0.0
sudo mkdir -p /var/www/my-blog
sudo docker cp my-blog-static:/app/static/. /var/www/my-blog/
docker rm my-blog-static
sudo chown -R nginx:nginx /var/www/my-blog
```

If your server is Ubuntu or Debian and Nginx runs as `www-data`, replace the
last command with:

```bash
sudo chown -R www-data:www-data /var/www/my-blog
```

After extraction, your Nginx static directory should look like this:

```text
/var/www/my-blog/
  css/
  js/
  pages/
```

You can verify:

```bash
ls -R /var/www/my-blog
```

### Alternative method: copy frontend files directly from source

If the server also has the repository code, you can copy directly:

```bash
sudo mkdir -p /var/www/my-blog
sudo cp -r backend/src/main/resources/static/. /var/www/my-blog/
```

This is simpler during manual debugging, but it is less consistent than using
the exact same image artifact that you deploy for the backend.

## 5. Configure Nginx to serve the frontend and proxy the backend

Use `deploy/nginx/static-frontend.conf` as the production config template.

Its behavior is:

- `/` redirects to `/pages/login.html`
- `/pages/*`, `/css/*`, `/js/*` are served by Nginx
- `/api/*` is proxied to `127.0.0.1:8080`

On a typical Linux server:

```bash
sudo cp deploy/nginx/static-frontend.conf /etc/nginx/conf.d/my-blog.conf
sudo nginx -t
sudo systemctl reload nginx
```

If your system uses `sites-available` and `sites-enabled`, use:

```bash
sudo cp deploy/nginx/static-frontend.conf /etc/nginx/sites-available/my-blog.conf
sudo ln -sf /etc/nginx/sites-available/my-blog.conf /etc/nginx/sites-enabled/my-blog.conf
sudo nginx -t
sudo systemctl reload nginx
```

If you use a real domain, edit the config first and replace:

```nginx
server_name _;
```

with:

```nginx
server_name your-domain.com www.your-domain.com;
```

## 6. How frontend requests work after deployment

Once deployed:

- Visiting `http://your-domain.com/` will jump to `/pages/login.html`
- `login.html`, `home.html`, `note.html` are served directly by Nginx
- JavaScript calls such as `/api/...` still hit the same domain
- Nginx forwards those `/api/...` requests to the Spring Boot container

That means:

- No CORS setup is needed in this topology.
- Frontend and backend stay on the same origin.
- The browser only sees one public entry point: Nginx.

## 7. Verification checklist

Check the backend container:

```bash
docker ps
docker logs -f my-blog
```

Check Nginx config:

```bash
sudo nginx -t
```

Check frontend file availability:

```bash
curl -I http://127.0.0.1/pages/login.html
curl -I http://127.0.0.1/css/app.css
curl -I http://127.0.0.1/js/api.js
```

Check backend API routing:

```bash
curl -I http://127.0.0.1/api/auth/login
```

Expected result:

- Static pages return `200 OK`
- Static CSS and JS return `200 OK`
- `/api/...` reaches Spring Boot

Note:

- `GET /api/auth/login` may return `405 Method Not Allowed` if the endpoint only
  accepts `POST`
- That still proves Nginx forwarded the request correctly

## 8. How to update after you change frontend code later

Each time you release a new version:

1. Build and push a new image tag.
2. Pull the new image on the server.
3. Recreate the backend container.
4. Re-extract `/app/static` from the new image into `/var/www/my-blog`.
5. Reload Nginx if needed.

Typical update flow:

```bash
docker pull your-registry/my-blog:1.0.1
docker compose up -d

docker rm -f my-blog-static 2>/dev/null || true
docker create --name my-blog-static your-registry/my-blog:1.0.1
sudo rm -rf /var/www/my-blog/*
sudo docker cp my-blog-static:/app/static/. /var/www/my-blog/
docker rm my-blog-static
sudo systemctl reload nginx
```

If you do not refresh `/var/www/my-blog`, Nginx will keep serving the old
frontend files even if the backend container has already been updated.

## 9. HTTPS

After HTTP is working, add HTTPS with Certbot or your cloud provider's
certificate service. Keep the same Nginx routing logic and only add the TLS
configuration on top.

## Notes

- The backend reads database settings from environment variables.
- `server.forward-headers-strategy=framework` is already enabled, so Spring
  Boot can correctly understand the original request protocol and host behind
  Nginx.
- If you later decide not to split static files into Nginx, you can still use
  `deploy/nginx/reverse-proxy.conf` and proxy everything to Spring Boot.
