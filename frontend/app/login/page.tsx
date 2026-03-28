'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { apiFetch } from '@/lib/api';
import { AuthResponse } from '@/lib/types';

export default function LoginPage() {
  const router = useRouter();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');

  async function handleSubmit() {
    try {
      const result = await apiFetch<AuthResponse>('/auth/login', {
        method: 'POST',
        body: JSON.stringify({ email, password }),
      });
      localStorage.setItem('token', result.token);
      localStorage.setItem('userId', result.userId);
      localStorage.setItem('name', result.name);
      router.push('/dashboard');
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Login failed');
    }
  }

  return (
    <div className="mx-auto max-w-md rounded-3xl border border-[var(--border)] bg-[var(--panel)] p-8">
      <h1 className="mb-6 text-3xl font-semibold">Login</h1>
      <div className="space-y-4">
        <input className="w-full rounded-2xl border border-slate-300 px-4 py-3" placeholder="Email" value={email} onChange={(e) => setEmail(e.target.value)} />
        <input className="w-full rounded-2xl border border-slate-300 px-4 py-3" placeholder="Password" type="password" value={password} onChange={(e) => setPassword(e.target.value)} />
        <button className="w-full rounded-2xl bg-[var(--brand)] px-4 py-3 font-semibold text-white" onClick={handleSubmit}>Login</button>
        {error ? <p className="text-sm text-red-600">{error}</p> : null}
      </div>
    </div>
  );
}
