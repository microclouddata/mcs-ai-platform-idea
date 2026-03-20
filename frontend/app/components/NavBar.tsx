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
    <nav className="flex flex-wrap gap-4 text-sm text-[var(--muted)]">
      {isLoggedIn ? (
        <>
          <Link href="/dashboard" className="hover:text-white transition-colors">Dashboard</Link>
          <Link href="/usage" className="hover:text-white transition-colors">Usage</Link>
          <Link href="/organization" className="hover:text-white transition-colors">Organization</Link>
          <Link href="/billing" className="hover:text-white transition-colors">Billing</Link>
          <Link href="/integrations" className="hover:text-white transition-colors">Integrations</Link>
          <Link href="/admin" className="hover:text-white transition-colors">Admin</Link>
          <button onClick={handleLogout} className="hover:text-white transition-colors">
            Log out
          </button>
        </>
      ) : (
        <>
          <Link href="/login">Login</Link>
          <Link href="/register">Register</Link>
        </>
      )}
    </nav>
  );
}