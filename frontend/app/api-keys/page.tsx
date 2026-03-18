'use client';

import { useEffect, useState } from 'react';
import { apiFetch } from '@/lib/api';
import { ApiKey } from '@/lib/types';

export default function ApiKeysPage() {
  const [keys, setKeys] = useState<ApiKey[]>([]);
  const [name, setName] = useState('');
  const [scopes, setScopes] = useState('*');
  const [newKey, setNewKey] = useState<ApiKey | null>(null);
  const [error, setError] = useState('');
  const [copied, setCopied] = useState(false);

  async function load() {
    const data = await apiFetch<ApiKey[]>('/api-keys');
    setKeys(data);
  }

  async function create() {
    if (!name.trim()) return;
    try {
      const key = await apiFetch<ApiKey>('/api-keys', {
        method: 'POST',
        body: JSON.stringify({ name, scopes: scopes.split(',').map(s => s.trim()).filter(Boolean) }),
      });
      setNewKey(key);
      setName(''); setScopes('*'); setError('');
      await load();
    } catch (e) { setError(e instanceof Error ? e.message : 'Failed'); }
  }

  async function revoke(id: string) {
    if (!confirm('Revoke this API key? This cannot be undone.')) return;
    await apiFetch(`/api-keys/${id}`, { method: 'DELETE' });
    if (newKey?.id === id) setNewKey(null);
    await load();
  }

  function copyKey(key: string) {
    navigator.clipboard.writeText(key);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  }

  useEffect(() => { void load(); }, []);

  return (
    <div className="space-y-6">
      <h1 className="text-3xl font-semibold">API Keys</h1>
      <p className="text-sm text-[var(--muted)]">Use API keys to authenticate programmatic access. Pass as <code className="rounded bg-black/30 px-1.5 py-0.5">X-API-Key</code> header.</p>

      <section className="rounded-3xl border border-[var(--border)] bg-[var(--panel)] p-6 space-y-4">
        <h2 className="text-xl font-semibold">Create New Key</h2>
        <div className="flex gap-3">
          <input className="flex-1 rounded-2xl border border-slate-300 px-4 py-3" value={name} onChange={e => setName(e.target.value)} placeholder="Key name (e.g. Production App)" />
          <input className="w-48 rounded-2xl border border-slate-300 px-4 py-3" value={scopes} onChange={e => setScopes(e.target.value)} placeholder="Scopes (e.g. *)" />
          <button onClick={create} className="rounded-2xl bg-[var(--brand)] px-5 py-3 font-semibold text-slate-950">Generate</button>
        </div>
        {error && <p className="text-red-300 text-sm">{error}</p>}
      </section>

      {newKey?.plainKey && (
        <section className="rounded-3xl border border-yellow-600 bg-yellow-900/20 p-6 space-y-3">
          <div className="flex items-center gap-2">
            <span className="text-yellow-400 text-lg">⚠</span>
            <h2 className="text-lg font-semibold text-yellow-300">Save your API key now</h2>
          </div>
          <p className="text-sm text-yellow-200">This key will not be shown again.</p>
          <div className="flex items-center gap-3">
            <code className="flex-1 rounded-2xl bg-black/40 px-4 py-3 text-sm font-mono text-yellow-100 break-all">{newKey.plainKey}</code>
            <button onClick={() => copyKey(newKey.plainKey!)} className="rounded-2xl border border-yellow-600 px-4 py-3 text-sm text-yellow-300 hover:bg-yellow-900/40">
              {copied ? 'Copied!' : 'Copy'}
            </button>
          </div>
        </section>
      )}

      <section className="rounded-3xl border border-[var(--border)] bg-[var(--panel)] p-6">
        <h2 className="text-xl font-semibold mb-4">Your API Keys</h2>
        {keys.length === 0 && <p className="text-[var(--muted)]">No API keys yet.</p>}
        <div className="space-y-3">
          {keys.map(k => (
            <div key={k.id} className="flex items-center justify-between rounded-2xl border border-[var(--border)] bg-[var(--panel-soft)] px-5 py-4">
              <div>
                <div className="font-semibold">{k.name}</div>
                <div className="mt-0.5 flex items-center gap-3 text-xs text-[var(--muted)]">
                  <code className="rounded bg-black/30 px-2 py-0.5">{k.keyPrefix}••••••••</code>
                  <span>Scopes: {k.scopes?.join(', ') ?? '*'}</span>
                  {k.lastUsedAt && <span>Last used: {new Date(k.lastUsedAt).toLocaleDateString()}</span>}
                  {k.expiresAt && <span>Expires: {new Date(k.expiresAt).toLocaleDateString()}</span>}
                </div>
              </div>
              <button onClick={() => revoke(k.id)} className="rounded-2xl border border-red-800 px-4 py-1.5 text-sm text-red-400 hover:text-red-300">Revoke</button>
            </div>
          ))}
        </div>
      </section>
    </div>
  );
}
