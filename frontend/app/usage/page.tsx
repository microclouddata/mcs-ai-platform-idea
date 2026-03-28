'use client';

import { useEffect, useState } from 'react';
import { apiFetch } from '@/lib/api';
import { UsageStatsResponse, UsageLog, Agent } from '@/lib/types';

export default function UsagePage() {
  const [stats, setStats] = useState<UsageStatsResponse | null>(null);
  const [logs, setLogs] = useState<UsageLog[]>([]);
  const [agents, setAgents] = useState<Agent[]>([]);
  const [error, setError] = useState('');

  useEffect(() => {
    async function load() {
      try {
        const [s, l, a] = await Promise.all([
          apiFetch<UsageStatsResponse>('/usage/stats'),
          apiFetch<UsageLog[]>('/usage/logs'),
          apiFetch<Agent[]>('/agents'),
        ]);
        setStats(s);
        setLogs(l);
        setAgents(a);
      } catch (e) {
        setError(e instanceof Error ? e.message : 'Failed to load');
      }
    }
    void load();
  }, []);

  const agentName = (id: string) => agents.find((a) => a.id === id)?.name ?? id.slice(0, 8) + '…';

  return (
    <div className="space-y-6">
      <h1 className="text-3xl font-semibold">Usage & Cost</h1>
      {error ? <p className="text-red-600">{error}</p> : null}

      {stats && (
        <>
          {/* Summary cards */}
          <div className="grid gap-4 md:grid-cols-3">
            {[
              { label: 'Total requests', value: stats.totalRequests.toLocaleString() },
              { label: 'Total tokens', value: stats.totalTokens.toLocaleString() },
              { label: 'Estimated cost', value: `$${stats.totalCost.toFixed(4)}` },
            ].map((c) => (
              <div key={c.label} className="rounded-3xl border border-[var(--border)] bg-[var(--panel)] p-6">
                <div className="text-3xl font-bold">{c.value}</div>
                <div className="mt-1 text-sm text-[var(--muted)]">{c.label}</div>
              </div>
            ))}
          </div>

          <div className="grid gap-6 md:grid-cols-2">
            {/* Tokens by model */}
            <section className="rounded-3xl border border-[var(--border)] bg-[var(--panel)] p-6">
              <h2 className="mb-4 text-xl font-semibold">Tokens by model</h2>
              {Object.entries(stats.tokensByModel).map(([model, tokens]) => (
                <div key={model} className="mb-3">
                  <div className="mb-1 flex justify-between text-sm">
                    <span>{model}</span>
                    <span className="text-[var(--muted)]">{tokens.toLocaleString()} tokens · ${(stats.costByModel[model] ?? 0).toFixed(4)}</span>
                  </div>
                  <div className="h-2 rounded-full bg-[var(--border)]">
                    <div
                      className="h-2 rounded-full bg-[var(--brand)]"
                      style={{ width: `${stats.totalTokens > 0 ? (tokens / stats.totalTokens) * 100 : 0}%` }}
                    />
                  </div>
                </div>
              ))}
              {Object.keys(stats.tokensByModel).length === 0 && <p className="text-[var(--muted)]">No data yet.</p>}
            </section>

            {/* Tokens by agent */}
            <section className="rounded-3xl border border-[var(--border)] bg-[var(--panel)] p-6">
              <h2 className="mb-4 text-xl font-semibold">Tokens by agent</h2>
              {Object.entries(stats.tokensByAgent).map(([agentId, tokens]) => (
                <div key={agentId} className="mb-3">
                  <div className="mb-1 flex justify-between text-sm">
                    <span>{agentName(agentId)}</span>
                    <span className="text-[var(--muted)]">{tokens.toLocaleString()} tokens</span>
                  </div>
                  <div className="h-2 rounded-full bg-[var(--border)]">
                    <div
                      className="h-2 rounded-full bg-purple-500"
                      style={{ width: `${stats.totalTokens > 0 ? (tokens / stats.totalTokens) * 100 : 0}%` }}
                    />
                  </div>
                </div>
              ))}
              {Object.keys(stats.tokensByAgent).length === 0 && <p className="text-[var(--muted)]">No data yet.</p>}
            </section>
          </div>
        </>
      )}

      {/* Recent log */}
      <section className="rounded-3xl border border-[var(--border)] bg-[var(--panel)] p-6">
        <h2 className="mb-4 text-xl font-semibold">Recent logs</h2>
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="text-left text-[var(--muted)] border-b border-[var(--border)]">
                <th className="pb-2 pr-6">Agent</th>
                <th className="pb-2 pr-6">Model</th>
                <th className="pb-2 pr-6">Tokens</th>
                <th className="pb-2 pr-6">Cost</th>
                <th className="pb-2">Date</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-[var(--border)]">
              {logs.slice(0, 50).map((l) => (
                <tr key={l.id}>
                  <td className="py-2 pr-6">{l.agentId ? agentName(l.agentId) : '—'}</td>
                  <td className="py-2 pr-6 text-[var(--muted)]">{l.model}</td>
                  <td className="py-2 pr-6">{l.totalTokens.toLocaleString()}</td>
                  <td className="py-2 pr-6 text-[var(--brand)]">${l.cost?.toFixed(5) ?? '—'}</td>
                  <td className="py-2 text-[var(--muted)]">{l.createdAt ? new Date(l.createdAt).toLocaleString() : '—'}</td>
                </tr>
              ))}
            </tbody>
          </table>
          {logs.length === 0 && <p className="mt-4 text-[var(--muted)]">No logs yet.</p>}
        </div>
      </section>
    </div>
  );
}
