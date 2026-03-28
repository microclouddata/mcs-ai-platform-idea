'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { apiFetch } from '@/lib/api';
import { AuthResponse } from '@/lib/types';

export default function RegisterPage() {
  const router = useRouter();
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');

  async function handleSubmit() {
    try {
      const result = await apiFetch<AuthResponse>('/auth/register', {
        method: 'POST',
        body: JSON.stringify({ name, email, password }),
      });
      localStorage.setItem('token', result.token);
      localStorage.setItem('userId', result.userId);
      localStorage.setItem('name', result.name);
      router.push('/dashboard');
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Register failed');
    }
  }

  return (
    <div className="mx-auto max-w-md rounded-3xl border border-[var(--border)] bg-[var(--panel)] p-8">
      <h1 className="mb-6 text-3xl font-semibold">Register</h1>
      <div className="space-y-4">
        <input type="text" className="w-full p-3 rounded-lg border border-[var(--border)] bg-white text-[var(--text)] placeholder:text-[var(--muted)] focus:outline-none focus:ring-2 focus:ring-[var(--brand)]/30" placeholder="Name" value={name} onChange={(e) => setName(e.target.value)} />
        <input className="w-full p-3 rounded-lg border border-[var(--border)] bg-white text-[var(--text)] placeholder:text-[var(--muted)] focus:outline-none focus:ring-2 focus:ring-[var(--brand)]/30" placeholder="Email" value={email} onChange={(e) => setEmail(e.target.value)} />
        <input className="w-full p-3 rounded-lg border border-[var(--border)] bg-white text-[var(--text)] placeholder:text-[var(--muted)] focus:outline-none focus:ring-2 focus:ring-[var(--brand)]/30" placeholder="Password" type="password" value={password} onChange={(e) => setPassword(e.target.value)} />
        <button className="w-full rounded-2xl bg-[var(--brand)] px-4 py-3 font-semibold text-white" onClick={handleSubmit}>Create account</button>
        {error ? <p className="text-sm text-red-600">{error}</p> : null}
      </div>
    </div>
  );
}
