'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { apiFetch } from '@/lib/api';
import { Workflow } from '@/lib/types';

export default function WorkflowsPage() {
  const [workflows, setWorkflows] = useState<Workflow[]>([]);
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

  async function load() {
    setLoading(true);
    try {
      const data = await apiFetch<Workflow[]>('/workflows');
      setWorkflows(data);
      setError('');
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Failed to load');
    } finally {
      setLoading(false);
    }
  }

  async function create() {
    if (!name.trim()) return;
    try {
      await apiFetch<Workflow>('/workflows', {
        method: 'POST',
        body: JSON.stringify({ name, description, steps: [] }),
      });
      setName('');
      setDescription('');
      await load();
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Failed to create');
    }
  }

  async function remove(id: string) {
    if (!confirm('Delete this workflow?')) return;
    try {
      await apiFetch(`/workflows/${id}`, { method: 'DELETE' });
      await load();
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Failed to delete');
    }
  }

  useEffect(() => { void load(); }, []);

  return (
    <div className="space-y-6">
      <section className="rounded-3xl border border-[var(--border)] bg-[var(--panel)] p-6">
        <h1 className="mb-4 text-3xl font-semibold">Workflows</h1>
        <div className="flex flex-col gap-3 md:flex-row">
          <input className="flex-1 rounded-2xl border border-slate-300 px-4 py-3" value={name} onChange={(e) => setName(e.target.value)} placeholder="Workflow name" />
          <input className="flex-1 rounded-2xl border border-slate-300 px-4 py-3" value={description} onChange={(e) => setDescription(e.target.value)} placeholder="Description (optional)" />
          <button className="rounded-2xl bg-[var(--brand)] px-5 py-3 font-semibold text-slate-950" onClick={create}>Create</button>
        </div>
        {error ? <p className="mt-3 text-sm text-red-300">{error}</p> : null}
      </section>

      <section className="rounded-3xl border border-[var(--border)] bg-[var(--panel)] p-6">
        <h2 className="mb-4 text-2xl font-semibold">Your workflows</h2>
        {loading ? <p className="text-[var(--muted)]">Loading...</p> : null}
        {!loading && workflows.length === 0 ? <p className="text-[var(--muted)]">No workflows yet.</p> : null}
        <div className="grid gap-4 md:grid-cols-2">
          {workflows.map((w) => (
            <div key={w.id} className="rounded-3xl border border-[var(--border)] bg-[var(--panel-soft)] p-5">
              <Link href={`/workflows/${w.id}`} className="block">
                <div className="mb-1 text-xl font-semibold hover:text-[var(--brand)]">{w.name}</div>
                <p className="mb-2 text-sm text-[var(--muted)]">{w.description || 'No description'}</p>
                <div className="text-xs text-[var(--muted)]">{w.steps?.length ?? 0} step{w.steps?.length !== 1 ? 's' : ''}</div>
              </Link>
              <button onClick={() => remove(w.id)} className="mt-3 text-xs text-red-400 hover:text-red-300">Delete</button>
            </div>
          ))}
        </div>
      </section>
    </div>
  );
}
