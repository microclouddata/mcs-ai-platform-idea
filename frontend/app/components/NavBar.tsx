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
          <Link href="/knowledge-bases" className="hover:text-white transition-colors">Knowledge Bases</Link>
          <Link href="/workflows" className="hover:text-white transition-colors">Workflows</Link>
          <Link href="/jobs" className="hover:text-white transition-colors">Jobs</Link>
          <Link href="/templates" className="hover:text-white transition-colors">Templates</Link>
          <Link href="/usage" className="hover:text-white transition-colors">Usage</Link>
          <Link href="/organization" className="hover:text-white transition-colors">Organization</Link>
          <Link href="/billing" className="hover:text-white transition-colors">Billing</Link>
          <Link href="/api-keys" className="hover:text-white transition-colors">API Keys</Link>
          <Link href="/audit" className="hover:text-white transition-colors">Audit</Link>
          <Link href="/marketplace" className="hover:text-white transition-colors">Marketplace</Link>
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