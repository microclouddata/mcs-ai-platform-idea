import './globals.css';
import Link from 'next/link';
import type { Metadata } from 'next';
import NavBar from './components/NavBar';

export const metadata: Metadata = {
  title: 'MCS AI Platform MVP',
  description: 'Single-project Spring Boot + Next.js MVP scaffold',
};

export default function RootLayout({ children }: Readonly<{ children: React.ReactNode }>) {
  return (
    <html lang="en">
      <body>
        <header className="border-b border-[var(--border)] bg-black/20 backdrop-blur">
          <div className="mx-auto flex max-w-6xl items-center justify-between px-6 py-4">
            <Link href="/" className="text-lg font-semibold text-white">MCS AI Platform</Link>
            <NavBar />
          </div>
        </header>
        <main className="mx-auto max-w-6xl px-6 py-8">{children}</main>
      </body>
    </html>
  );
}
