import type { NextConfig } from 'next';

const nextConfig: NextConfig = {
  output: 'standalone',
  async rewrites() {
    return [
      {
        source: '/api/:path*',
        // In Docker/K8s, BACKEND_URL is set to http://gateway:8080 (see docker-compose.yml).
        // In local dev without docker, point to the local gateway instance.
        destination: `${process.env.BACKEND_URL || 'http://localhost:8080'}/api/:path*`,
      },
    ];
  },
};

export default nextConfig;
