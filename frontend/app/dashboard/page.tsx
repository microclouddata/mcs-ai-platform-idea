'use client';

import Link from 'next/link';
import { useEffect, useState } from 'react';
import { apiFetch } from '@/lib/api';
import { Agent } from '@/lib/types';

export default function DashboardPage() {
  const [agents, setAgents] = useState<Agent[]>([]);
  const [name, setName] = useState('My Knowledge Agent');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  async function loadAgents() {
    setLoading(true);
    try {
      const data = await apiFetch<Agent[]>('/agents');
      setAgents(data);
      setError('');
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Failed to load agents');
    } finally {
      setLoading(false);
    }
  }

  async function createAgent() {
    try {
      await apiFetch<Agent>('/agents', {
        method: 'POST',
        body: JSON.stringify({
          name,
          description: 'Document Q&A agent',
          systemPrompt: 'You are a helpful assistant. Use the uploaded document context when possible and say when information is not found.',
          provider: 'OPENAI',
          model: 'gpt-3.5-turbo',
          temperature: 0.2,
          tools: ['KNOWLEDGE_SEARCH'],
        }),
      });
      setName('My Knowledge Agent');
      await loadAgents();
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Failed to create agent');
    }
  }

  useEffect(() => {
    void loadAgents();
  }, []);

  return (
    <div className="space-y-6">
      <section className="rounded-3xl border border-[var(--border)] bg-[var(--panel)] p-6">
        <h1 className="mb-4 text-3xl font-semibold">Dashboard</h1>
        <div className="flex flex-col gap-3 md:flex-row">
          <input className="flex-1 rounded-2xl border border-slate-300 px-4 py-3" value={name} onChange={(e) => setName(e.target.value)} placeholder="Agent name" />
          <button className="rounded-2xl bg-[var(--brand)] px-5 py-3 font-semibold text-slate-950" onClick={createAgent}>Create Agent</button>
        </div>
        {error ? <p className="mt-3 text-sm text-red-300">{error}</p> : null}
      </section>

      <section className="rounded-3xl border border-[var(--border)] bg-[var(--panel)] p-6">
        <h2 className="mb-4 text-2xl font-semibold">Your agents</h2>
        {loading ? <p className="text-[var(--muted)]">Loading...</p> : null}
        {!loading && agents.length === 0 ? <p className="text-[var(--muted)]">Create your first agent.</p> : null}
        <div className="grid gap-4 md:grid-cols-2">
          {agents.map((agent) => (
            <Link
              key={agent.id}
              href={`/agents/${agent.id}`}
              className={`rounded-3xl border border-[var(--border)] bg-[var(--panel-soft)] p-5 transition hover:-translate-y-0.5 hover:border-[var(--brand)] ${agent.enabled === false ? 'opacity-50 grayscale' : ''}`}
            >
              <div className="mb-2 flex items-center gap-2 text-xl font-semibold">
                {agent.name}
                {agent.enabled === false && (
                  <span className="rounded-full border border-slate-600 px-2 py-0.5 text-xs font-normal text-[var(--muted)]">Disabled</span>
                )}
              </div>
              <p className="mb-3 text-sm text-[var(--muted)]">{agent.description}</p>
              <div className="text-xs text-[var(--muted)]">{agent.provider} · {agent.model}</div>
            </Link>
          ))}
        </div>
      </section>
    </div>
  );
}
