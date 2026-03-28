'use client';

import Link from 'next/link';
import { usePathname, useRouter } from 'next/navigation';
import { useEffect, useState } from 'react';

export default function NavBar() {
  const router = useRouter();
  const pathname = usePathname();
  const [isLoggedIn, setIsLoggedIn] = useState(false);

  useEffect(() => {
    setIsLoggedIn(!!localStorage.getItem('token'));
  }, [pathname]);

  function handleLogout() {
    localStorage.removeItem('token');
    localStorage.removeItem('userId');
    localStorage.removeItem('name');
    setIsLoggedIn(false);
    router.push('/login');
  }

  return (
    <nav className="flex items-center gap-6 text-sm text-[var(--muted)]">
      {isLoggedIn ? (
        <>
          <Link href="/dashboard" className="hover:text-[var(--text)] transition-colors">Dashboard</Link>
          <Link href="/usage" className="hover:text-[var(--text)] transition-colors">Usage</Link>
          <Link href="/organization" className="hover:text-[var(--text)] transition-colors">Organization</Link>
          <Link href="/billing" className="hover:text-[var(--text)] transition-colors">Billing</Link>
          <Link href="/admin" className="hover:text-[var(--text)] transition-colors">Admin</Link>
          <button
            onClick={handleLogout}
            className="rounded-full border border-[var(--border)] px-5 py-2 text-sm font-medium text-[var(--text)] hover:bg-[var(--panel-soft)] transition-colors"
          >
            Log out
          </button>
        </>
      ) : (
        <>
          <Link href="/login" className="hover:text-[var(--text)] transition-colors">Login</Link>
          <Link
            href="/register"
            className="rounded-full bg-[var(--brand)] px-5 py-2 text-sm font-semibold text-white hover:bg-blue-700 transition-colors"
          >
            Get Started
          </Link>
        </>
      )}
    </nav>
  );
}
