'use client';

import { useEffect, useState } from 'react';
import { apiFetch } from '@/lib/api';
import { AuditLog } from '@/lib/types';

const ACTION_COLOR: Record<string, string> = {
  USER_LOGIN: 'text-green-400',
  USER_REGISTER: 'text-blue-400',
  AGENT_CREATE: 'text-[var(--brand)]',
  AGENT_DELETE: 'text-red-400',
  AGENT_DISABLE: 'text-yellow-400',
  CHAT_MESSAGE: 'text-slate-300',
  DOCUMENT_UPLOAD: 'text-purple-400',
  DOCUMENT_DELETE: 'text-red-400',
  WORKFLOW_EXECUTE: 'text-[var(--brand)]',
  API_KEY_CREATE: 'text-green-400',
  API_KEY_REVOKE: 'text-red-400',
  SUBSCRIPTION_UPGRADE: 'text-yellow-400',
};

export default function AuditPage() {
  const [logs, setLogs] = useState<AuditLog[]>([]);
  const [filter, setFilter] = useState('');

  useEffect(() => {
    apiFetch<AuditLog[]>('/audit/logs').then(setLogs).catch(console.error);
  }, []);

  const filtered = filter ? logs.filter(l => l.action.includes(filter.toUpperCase()) || l.resourceType?.includes(filter)) : logs;

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-3xl font-semibold">Audit Logs</h1>
        <input className="rounded-2xl border border-slate-300 px-4 py-2.5 text-sm w-64" value={filter} onChange={e => setFilter(e.target.value)} placeholder="Filter by action..." />
      </div>

      <section className="rounded-3xl border border-[var(--border)] bg-[var(--panel)] p-6">
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="text-left text-[var(--muted)] border-b border-[var(--border)]">
                <th className="pb-2 pr-6">Action</th>
                <th className="pb-2 pr-6">Resource</th>
                <th className="pb-2 pr-6">Detail</th>
                <th className="pb-2 pr-6">IP</th>
                <th className="pb-2">Date</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-[var(--border)]">
              {filtered.slice(0, 100).map(log => (
                <tr key={log.id}>
                  <td className={`py-2 pr-6 font-medium ${ACTION_COLOR[log.action] ?? 'text-[var(--muted)]'}`}>{log.action}</td>
                  <td className="py-2 pr-6 text-[var(--muted)]">{log.resourceType ?? '—'}</td>
                  <td className="py-2 pr-6 text-xs max-w-xs truncate">{log.detail ?? '—'}</td>
                  <td className="py-2 pr-6 text-xs text-[var(--muted)]">{log.ipAddress ?? '—'}</td>
                  <td className="py-2 text-xs text-[var(--muted)]">{log.createdAt ? new Date(log.createdAt).toLocaleString() : '—'}</td>
                </tr>
              ))}
            </tbody>
          </table>
          {filtered.length === 0 && <p className="mt-4 text-[var(--muted)]">No audit logs yet.</p>}
        </div>
      </section>
    </div>
  );
}
