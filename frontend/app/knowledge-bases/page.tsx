'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { apiFetch } from '@/lib/api';
import { KnowledgeBase } from '@/lib/types';

export default function KnowledgeBasesPage() {
  const [kbs, setKbs] = useState<KnowledgeBase[]>([]);
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  async function load() {
    setLoading(true);
    try {
      const data = await apiFetch<KnowledgeBase[]>('/knowledge-bases');
      setKbs(data);
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
      await apiFetch<KnowledgeBase>('/knowledge-bases', {
        method: 'POST',
        body: JSON.stringify({ name, description }),
      });
      setName('');
      setDescription('');
      await load();
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Failed to create');
    }
  }

  async function remove(id: string) {
    try {
      await apiFetch(`/knowledge-bases/${id}`, { method: 'DELETE' });
      await load();
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Failed to delete');
    }
  }

  useEffect(() => {
    void load();
  }, []);

  return (
    <div className="space-y-6">
      <section className="rounded-3xl border border-[var(--border)] bg-[var(--panel)] p-6">
        <h1 className="mb-4 text-3xl font-semibold">Knowledge Bases</h1>
        <div className="flex flex-col gap-3 md:flex-row">
          <input
            className="flex-1 rounded-2xl border border-slate-300 px-4 py-3"
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder="Knowledge base name"
          />
          <input
            className="flex-1 rounded-2xl border border-slate-300 px-4 py-3"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            placeholder="Description (optional)"
          />
          <button
            className="rounded-2xl bg-[var(--brand)] px-5 py-3 font-semibold text-slate-950"
            onClick={create}
          >
            Create
          </button>
        </div>
        {error ? <p className="mt-3 text-sm text-red-300">{error}</p> : null}
      </section>

      <section className="rounded-3xl border border-[var(--border)] bg-[var(--panel)] p-6">
        <h2 className="mb-4 text-2xl font-semibold">Your knowledge bases</h2>
        {loading ? <p className="text-[var(--muted)]">Loading...</p> : null}
        {!loading && kbs.length === 0 ? (
          <p className="text-[var(--muted)]">No knowledge bases yet.</p>
        ) : null}
        <div className="grid gap-4 md:grid-cols-2">
          {kbs.map((kb) => (
            <div
              key={kb.id}
              className="rounded-3xl border border-[var(--border)] bg-[var(--panel-soft)] p-5"
            >
              <div className="mb-1 text-xl font-semibold">{kb.name}</div>
              <p className="mb-3 text-sm text-[var(--muted)]">{kb.description || 'No description'}</p>
              <div className="flex items-center justify-between text-xs text-[var(--muted)]">
                <span>{kb.documentCount} document{kb.documentCount !== 1 ? 's' : ''}</span>
                <button
                  className="text-red-400 hover:text-red-300"
                  onClick={() => remove(kb.id)}
                >
                  Delete
                </button>
              </div>
            </div>
          ))}
        </div>
      </section>
    </div>
  );
}
