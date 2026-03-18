'use client';

import { useEffect, useState } from 'react';
import { apiFetch } from '@/lib/api';
import { User, Agent, UsageLog } from '@/lib/types';

type Tab = 'users' | 'agents' | 'usage';

export default function AdminPage() {
  const [tab, setTab] = useState<Tab>('users');
  const [users, setUsers] = useState<User[]>([]);
  const [agents, setAgents] = useState<Agent[]>([]);
  const [logs, setLogs] = useState<UsageLog[]>([]);
  const [error, setError] = useState('');

  useEffect(() => {
    async function load() {
      try {
        if (tab === 'users') {
          const data = await apiFetch<User[]>('/admin/users');
          setUsers(data);
        } else if (tab === 'agents') {
          const data = await apiFetch<Agent[]>('/admin/agents');
          setAgents(data);
        } else {
          const data = await apiFetch<{ content: UsageLog[] }>('/admin/usage-logs');
          setLogs(data.content);
        }
        setError('');
      } catch (e) {
        setError(e instanceof Error ? e.message : 'Failed to load');
      }
    }
    void load();
  }, [tab]);

  const tabClass = (t: Tab) =>
    `px-4 py-2 rounded-2xl text-sm font-semibold transition ${
      tab === t
        ? 'bg-[var(--brand)] text-slate-950'
        : 'text-[var(--muted)] hover:text-white'
    }`;

  return (
    <div className="space-y-6">
      <section className="rounded-3xl border border-[var(--border)] bg-[var(--panel)] p-6">
        <h1 className="mb-4 text-3xl font-semibold">Admin</h1>
        <div className="flex gap-2">
          <button className={tabClass('users')} onClick={() => setTab('users')}>Users</button>
          <button className={tabClass('agents')} onClick={() => setTab('agents')}>Agents</button>
          <button className={tabClass('usage')} onClick={() => setTab('usage')}>Usage Logs</button>
        </div>
      </section>

      {error ? <p className="text-sm text-red-300">{error}</p> : null}

      {tab === 'users' && (
        <section className="rounded-3xl border border-[var(--border)] bg-[var(--panel)] p-6">
          <h2 className="mb-4 text-xl font-semibold">Users ({users.length})</h2>
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="text-left text-[var(--muted)] border-b border-[var(--border)]">
                  <th className="pb-2 pr-6">Name</th>
                  <th className="pb-2 pr-6">Email</th>
                  <th className="pb-2 pr-6">Role</th>
                  <th className="pb-2">Joined</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-[var(--border)]">
                {users.map((u) => (
                  <tr key={u.id}>
                    <td className="py-3 pr-6">{u.name}</td>
                    <td className="py-3 pr-6 text-[var(--muted)]">{u.email}</td>
                    <td className="py-3 pr-6">
                      <span className="rounded-full bg-[var(--panel-soft)] px-2 py-0.5 text-xs">{u.role}</span>
                    </td>
                    <td className="py-3 text-[var(--muted)]">
                      {u.createdAt ? new Date(u.createdAt).toLocaleDateString() : '—'}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </section>
      )}

      {tab === 'agents' && (
        <section className="rounded-3xl border border-[var(--border)] bg-[var(--panel)] p-6">
          <h2 className="mb-4 text-xl font-semibold">Agents ({agents.length})</h2>
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="text-left text-[var(--muted)] border-b border-[var(--border)]">
                  <th className="pb-2 pr-6">Name</th>
                  <th className="pb-2 pr-6">Model</th>
                  <th className="pb-2 pr-6">Memory</th>
                  <th className="pb-2">Tools</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-[var(--border)]">
                {agents.map((a) => (
                  <tr key={a.id}>
                    <td className="py-3 pr-6">{a.name}</td>
                    <td className="py-3 pr-6 text-[var(--muted)]">{a.provider} / {a.model}</td>
                    <td className="py-3 pr-6">{a.memoryEnabled ? 'On' : 'Off'}</td>
                    <td className="py-3">{a.toolsEnabled ? 'On' : 'Off'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </section>
      )}

      {tab === 'usage' && (
        <section className="rounded-3xl border border-[var(--border)] bg-[var(--panel)] p-6">
          <h2 className="mb-4 text-xl font-semibold">Usage Logs</h2>
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="text-left text-[var(--muted)] border-b border-[var(--border)]">
                  <th className="pb-2 pr-6">Model</th>
                  <th className="pb-2 pr-6">Prompt</th>
                  <th className="pb-2 pr-6">Completion</th>
                  <th className="pb-2 pr-6">Total</th>
                  <th className="pb-2">Date</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-[var(--border)]">
                {logs.map((l) => (
                  <tr key={l.id}>
                    <td className="py-3 pr-6">{l.provider} / {l.model}</td>
                    <td className="py-3 pr-6 text-[var(--muted)]">{l.promptTokens}</td>
                    <td className="py-3 pr-6 text-[var(--muted)]">{l.completionTokens}</td>
                    <td className="py-3 pr-6 font-semibold">{l.totalTokens}</td>
                    <td className="py-3 text-[var(--muted)]">
                      {l.createdAt ? new Date(l.createdAt).toLocaleString() : '—'}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </section>
      )}
    </div>
  );
}
