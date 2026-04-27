# Deployment Guide

This project uses:

- JDK 25
- Spring Boot 4.0.6
- Nginx for public traffic
- Docker for the backend container
- MySQL as the database

The current frontend files are stored in `src/main/resources/static`.

This document is written for the deployment mode you described:

1. One public server with one public IP.
2. One existing Nginx instance serving multiple services.
3. No virtual hosts or separate domains for different services.
4. Different services are distinguished by different ports such as `80`, `81`,
   and `82`.
5. This project uses port `80`.
6. The backend image has already been pulled to the server.

Your deployment goal is:

1. Extract frontend files from the already-pulled backend image.
2. Let Nginx serve those frontend static files on port `80`.
3. Let Nginx reverse proxy `/api/` to the Spring Boot container on
   `127.0.0.1:8080`.

## Recommended topology

```text
Browser
  -> http://your-public-ip:80
     -> Nginx :80
        -> /index.html /css/* /js/* from /var/www/my-blog
        -> /api/* proxy to 127.0.0.1:8080
           -> Docker container: my-blog
              -> MySQL

Other services on the same server
  -> http://your-public-ip:81
  -> http://your-public-ip:82
```

This topology is a good fit for your current project because the frontend is
pure static HTML/CSS/JavaScript and the backend API is already separated under
`/api`. It also fits your current server constraint: one Nginx instance, one
public IP, multiple services separated by port.

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
- Existing deployments must add the note cover column manually:

```sql
ALTER TABLE note ADD COLUMN cover_image_url VARCHAR(512) NULL AFTER title;
```

- Existing deployments must also create Spring Session tables:

```sql
SOURCE 003-session.sql;
```

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
docker run -d \
  --name my-blog \
  --restart unless-stopped \
  -p 127.0.0.1:8080:8080 \
  -e APP_DATASOURCE_URL='jdbc:mysql://your-mysql-host:3306/my_blog?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai&characterEncoding=utf8' \
  -e APP_DATASOURCE_USERNAME='root' \
  -e APP_DATASOURCE_PASSWORD='change-me' \
  -e APP_OSS_ENDPOINT='oss-cn-hangzhou.aliyuncs.com' \
  -e APP_OSS_BUCKET='your-bucket-name' \
  -e APP_OSS_ACCESS_KEY_ID='replace-me' \
  -e APP_OSS_ACCESS_KEY_SECRET='replace-me' \
  -e APP_OSS_DEFAULT_COVER_URL='https://your-bucket-name.oss-cn-hangzhou.aliyuncs.com/defaults/note-cover.png' \
  -e APP_MULTIPART_MAX_FILE_SIZE='5MB' \
  -e APP_MULTIPART_MAX_REQUEST_SIZE='5MB' \
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
      APP_OSS_ENDPOINT: ${APP_OSS_ENDPOINT}
      APP_OSS_BUCKET: ${APP_OSS_BUCKET}
      APP_OSS_ACCESS_KEY_ID: ${APP_OSS_ACCESS_KEY_ID}
      APP_OSS_ACCESS_KEY_SECRET: ${APP_OSS_ACCESS_KEY_SECRET}
      APP_OSS_DEFAULT_COVER_URL: ${APP_OSS_DEFAULT_COVER_URL}
      APP_MULTIPART_MAX_FILE_SIZE: ${APP_MULTIPART_MAX_FILE_SIZE:-5MB}
      APP_MULTIPART_MAX_REQUEST_SIZE: ${APP_MULTIPART_MAX_REQUEST_SIZE:-5MB}
      SERVER_PORT: ${SERVER_PORT:-8080}
      APP_SESSION_TIMEOUT: ${APP_SESSION_TIMEOUT:-7d}
```

`.env` example:

```env
APP_DATASOURCE_URL=jdbc:mysql://your-mysql-host:3306/my_blog?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai&characterEncoding=utf8
APP_DATASOURCE_USERNAME=root
APP_DATASOURCE_PASSWORD=replace-with-a-strong-password
APP_OSS_ENDPOINT=oss-cn-hangzhou.aliyuncs.com
APP_OSS_BUCKET=your-bucket-name
APP_OSS_ACCESS_KEY_ID=replace-with-oss-access-key-id
APP_OSS_ACCESS_KEY_SECRET=replace-with-oss-access-key-secret
APP_OSS_DEFAULT_COVER_URL=https://your-bucket-name.oss-cn-hangzhou.aliyuncs.com/defaults/note-cover.png
APP_MULTIPART_MAX_FILE_SIZE=5MB
APP_MULTIPART_MAX_REQUEST_SIZE=5MB
SERVER_PORT=8080
APP_SESSION_TIMEOUT=7d
```

Start the backend:

```bash
docker compose up -d
docker compose ps
docker compose logs -f
```

If your backend image is already present on the server, you do not need to
pull it again before starting the container.

## 4. Extract the frontend files from the existing image

This is the core step for your current deployment plan.

Because the frontend files are packaged into the backend image, you can extract
them directly from the image and place them into Nginx's static directory.

For your release flow, this is the best approach because:

- the server already has the image
- the server does not need the full repository
- the frontend files are guaranteed to match the backend image version

Run the following commands on the server:

```bash
docker rm -f my-blog-static 2>/dev/null || true
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
  index.html
  js/
```

You can verify:

```bash
ls -R /var/www/my-blog
```

At this point, the frontend static files have been deployed into Nginx's local
directory. The browser will later access these files through port `80`.

### Alternative method: copy frontend files directly from source

If the server also has the repository code, you can copy directly:

```bash
sudo mkdir -p /var/www/my-blog
sudo cp -r backend/src/main/resources/static/. /var/www/my-blog/
```

This is simpler during manual debugging, but it is less consistent than using
the exact same image artifact that you deploy for the backend.

## 5. Configure Nginx to use port 80 for this service

Because you want one Nginx instance to host multiple services on different
ports, the clean approach is:

- this project listens on `80`
- another project can listen on `81`
- another project can listen on `82`

For this project, create one dedicated Nginx server block that listens on
`80`.

Use `deploy/nginx/static-frontend.conf` as the base template. Its behavior is:

- `/` serves `index.html`
- `/css/*` and `/js/*` are served by Nginx
- `/api/*` is proxied to `127.0.0.1:8080`

If your Linux distribution uses `/etc/nginx/conf.d/`, create:

```bash
sudo tee /etc/nginx/conf.d/my-blog-80.conf > /dev/null <<'EOF'
server {
    listen 80;
    server_name _;

    root /var/www/my-blog;
    index index.html;
    client_max_body_size 20m;

    location = / {
        try_files /index.html =404;
    }

    location /api/ {
        proxy_pass http://127.0.0.1:8080;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_set_header X-Forwarded-Host $host;
    }

    location /css/ {
        expires 7d;
        add_header Cache-Control "public, max-age=604800";
        try_files $uri =404;
    }

    location /js/ {
        expires 7d;
        add_header Cache-Control "public, max-age=604800";
        try_files $uri =404;
    }

    location / {
        try_files $uri $uri/ /index.html;
    }
}
EOF
```

Then verify and reload Nginx:

```bash
sudo nginx -t
sudo systemctl reload nginx
```

If your system uses `/etc/nginx/sites-available/` and
`/etc/nginx/sites-enabled/`, create:

```bash
sudo tee /etc/nginx/sites-available/my-blog-80.conf > /dev/null <<'EOF'
server {
    listen 80;
    server_name _;

    root /var/www/my-blog;
    index index.html;
    client_max_body_size 20m;

    location = / {
        try_files /index.html =404;
    }

    location /api/ {
        proxy_pass http://127.0.0.1:8080;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_set_header X-Forwarded-Host $host;
    }

    location /css/ {
        expires 7d;
        add_header Cache-Control "public, max-age=604800";
        try_files $uri =404;
    }

    location /js/ {
        expires 7d;
        add_header Cache-Control "public, max-age=604800";
        try_files $uri =404;
    }

    location / {
        try_files $uri $uri/ /index.html;
    }
}
EOF

sudo ln -sf /etc/nginx/sites-available/my-blog-80.conf /etc/nginx/sites-enabled/my-blog-80.conf
sudo nginx -t
sudo systemctl reload nginx
```

Important:

- Make sure no other Nginx site is already listening on port `80`, otherwise
  Nginx may fail to start or traffic may go to the wrong service.
- If another service is already using `80`, then either move that service to
  `81` or `82`, or change this project to another port instead.
- Under your current plan, users access this project through
  `http://your-public-ip/` or `http://your-public-ip:80/`.

If you later deploy another service on port `81`, its config is conceptually
the same, only the `listen` port changes:

```nginx
server {
    listen 81;
    server_name _;
    ...
}
```

## 6. How frontend requests work after deployment

Once deployed:

- Visiting `http://your-public-ip/` or `http://your-public-ip:80/` will load
  `index.html`
- `index.html` is served directly by Nginx
- JavaScript calls such as `/api/...` still hit the same IP and the same port
- Nginx forwards those `/api/...` requests to the Spring Boot container on
  `127.0.0.1:8080`

That means:

- No CORS setup is needed in this topology.
- Frontend and backend stay on the same origin.
- The browser only sees one public entry point for this service:
  `http://your-public-ip:80`

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

Check frontend file availability from the server itself:

```bash
curl -I http://127.0.0.1/
curl -I http://127.0.0.1/index.html
curl -I http://127.0.0.1/css/styles.css
curl -I http://127.0.0.1/js/api.js
```

Check backend API routing:

```bash
curl -I http://127.0.0.1/api/auth/login
```

Then verify from your local machine or a browser:

```text
http://your-public-ip/
http://your-public-ip:80/
```

Expected result:

- `index.html` returns `200 OK`
- Static CSS and JS return `200 OK`
- `/api/...` reaches Spring Boot
- The frontend can be opened through the server public IP on port `80`

Note:

- `GET /api/auth/login` may return `405 Method Not Allowed` if the endpoint only
  accepts `POST`
- That still proves Nginx forwarded the request correctly

## 8. How to update later

Each time you release a new version:

1. Build and push a new image tag.
2. Pull the new image on the server if needed.
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

## 9. Notes for your multi-service Nginx setup

- This project occupies public port `80`.
- If you deploy another service on the same machine, give that service another
  public port such as `81` or `82`.
- Different services should each have their own Nginx config file, root
  directory, and upstream target.
- The backend container for this project should still stay on an internal
  address such as `127.0.0.1:8080`; only Nginx is exposed publicly.
- Make sure your server firewall and cloud security group allow inbound traffic
  on the public port you choose. For this project, that means port `80`.

## 10. HTTPS

After HTTP is working, add HTTPS with Certbot or your cloud provider's
certificate service. If you stay with the "same IP + different port" model,
you can still keep the same routing logic and add TLS later.

## Notes

- The backend reads database settings from environment variables.
- Note create/update APIs now require `multipart/form-data`, not JSON.
- OSS credentials must be supplied through environment variables or deployment
  secrets; do not hardcode them in source control.
- `server.forward-headers-strategy=framework` is already enabled, so Spring
  Boot can correctly understand the original request protocol and host behind
  Nginx.
- If you later decide not to split static files into Nginx, you can still use
  `deploy/nginx/reverse-proxy.conf` and proxy everything to Spring Boot.
