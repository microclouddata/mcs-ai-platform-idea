import './globals.css';
import Link from 'next/link';
import type { Metadata } from 'next';
import NavBar from './components/NavBar';

export const metadata: Metadata = {
  title: 'MCS AI Platform',
  description: 'Next-generation AI platform for intelligent systems',
};

export default function RootLayout({ children }: Readonly<{ children: React.ReactNode }>) {
  return (
    <html lang="en">
      <body>
        <header className="border-b border-[var(--border)] bg-white/80 backdrop-blur-md sticky top-0 z-50">
          <div className="mx-auto flex max-w-7xl items-center justify-between px-6 py-4">
            <Link href="/" className="flex items-center gap-2.5">
              <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-gradient-to-br from-indigo-600 to-blue-500 text-white text-sm font-bold shadow-sm">
                M
              </div>
              <span className="text-base font-semibold text-[var(--text)]">MCS AI Platform</span>
            </Link>
            <NavBar />
          </div>
        </header>
        <main className="mx-auto max-w-7xl px-6 py-8">{children}</main>
      </body>
    </html>
  );
}
