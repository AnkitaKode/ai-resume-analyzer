# Deployment Guide

## Environment Setup

1. Copy `.env.example` to `.env.production` and update the API URL:
```bash
cp .env.example .env.production
```

2. Update `REACT_APP_API_URL` in `.env.production` to point to your production backend.

## Build for Production

```bash
npm run build
```

## Deployment Options

### 1. Static Hosting (Vercel, Netlify, GitHub Pages)

The `build` folder contains all static files ready for deployment.

#### Vercel Deployment
- Connect your repository to Vercel
- Set environment variable `REACT_APP_API_URL` in Vercel dashboard
- Deploy automatically on push to main branch

#### Netlify Deployment
- Connect your repository to Netlify
- Set environment variable `REACT_APP_API_URL` in Netlify dashboard
- Build command: `npm run build`
- Publish directory: `build`

### 2. Docker Deployment

```dockerfile
FROM node:18-alpine as builder
WORKDIR /app
COPY package*.json ./
RUN npm ci --only=production
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=builder /app/build /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

### 3. Traditional Hosting

Upload the contents of the `build` folder to your web server's document root.

## Important Notes

- Ensure your backend API is accessible from the deployed frontend
- Configure CORS on your backend to allow requests from your frontend domain
- Update API base URL in environment variables before deployment
- The build is optimized and minified for production use
