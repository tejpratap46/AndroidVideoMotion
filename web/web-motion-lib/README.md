# web-motion-lib

MotionLib feature showcase built with React + Vite + Tailwind, deployable to Cloudflare Workers/Assets.

## Scripts

- `npm run dev` - local Vite development server
- `npm run build` - type check + production build
- `npm run preview` - build and run with Wrangler locally
- `npm run deploy` - build and deploy to Cloudflare

## Cloudflare Deploy

1. Authenticate:
   ```bash
   npx wrangler login
   ```
2. Deploy:
   ```bash
   npm run deploy
   ```

Worker config is in `wrangler.jsonc`.
