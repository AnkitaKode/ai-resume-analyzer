# Vercel Deployment Guide

## Quick Setup

### 1. Install Vercel CLI
```bash
npm i -g vercel
```

### 2. Deploy from Root Directory
```bash
# From the project root (d:\Programming\ai-resume-analyzer)
vercel --prod
```

## Environment Variables

### Required Variables
Set these in your Vercel project dashboard (Settings → Environment Variables):

```
REACT_APP_API_URL=https://your-production-backend.com/api
```

### Optional Variables
```
REACT_APP_GOOGLE_ANALYTICS_ID=GA-XXXXXXXXX
REACT_APP_SENTRY_DSN=your-sentry-dsn
```

## Deployment Methods

### Method 1: Vercel CLI (Recommended)
```bash
# From project root
vercel login
vercel --prod
```

### Method 2: GitHub Integration
1. Push your code to GitHub
2. Connect your repository to Vercel
3. Set environment variables in Vercel dashboard
4. Deploy automatically on push to main branch

### Method 3: Vercel Dashboard
1. Go to [vercel.com](https://vercel.com)
2. Click "New Project"
3. Import your GitHub repository
4. Configure build settings:
   - **Build Command**: `cd ai-resume-analyzer-frontend && npm run build`
   - **Output Directory**: `ai-resume-analyzer-frontend/build`
   - **Install Command**: `cd ai-resume-analyzer-frontend && npm install`

## Configuration Files

### vercel.json
- Handles client-side routing
- Optimizes static asset caching
- Configures API proxy if needed

### .env.vercel
- Template for required environment variables
- Reference for setting up Vercel environment

## Build Optimization

The build is automatically optimized for Vercel:
- Static files are cached efficiently
- Client-side routing works out of the box
- API requests are proxied correctly

## Testing Locally

```bash
# Test the build locally
cd ai-resume-analyzer-frontend
npm run build
npm run serve
```

## Deployment Checklist

- [ ] Backend API is deployed and accessible
- [ ] `REACT_APP_API_URL` is set in Vercel environment variables
- [ ] Backend CORS allows requests from your Vercel domain
- [ ] All environment variables are configured
- [ ] Build completes successfully

## Troubleshooting

### Common Issues

1. **API Requests Failing**
   - Check `REACT_APP_API_URL` environment variable
   - Verify backend CORS configuration
   - Ensure backend is deployed and accessible

2. **Build Errors**
   - Run `npm run build` locally first
   - Check for missing dependencies
   - Verify all environment variables are set

3. **Routing Issues**
   - The vercel.json configuration handles client-side routing
   - Ensure React Router is properly configured

### Debug Commands

```bash
# Check environment variables
vercel env ls

# View deployment logs
vercel logs

# Redeploy latest commit
vercel --prod
```

## Production URL

After deployment, your app will be available at:
`https://your-project-name.vercel.app`

## Custom Domain

To add a custom domain:
1. Go to Vercel dashboard → Settings → Domains
2. Add your custom domain
3. Update DNS records as instructed
4. Update backend CORS to allow your custom domain
