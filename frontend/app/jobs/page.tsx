'use client';

import { useEffect, useState } from 'react';
import { apiFetch } from '@/lib/api';
import { ScheduledJob, Workflow } from '@/lib/types';

export default function JobsPage() {
  const [jobs, setJobs] = useState<ScheduledJob[]>([]);
  const [workflows, setWorkflows] = useState<Workflow[]>([]);
  const [name, setName] = useState('');
  const [workflowId, setWorkflowId] = useState('');
  const [scheduleType, setScheduleType] = useState<'HOURLY' | 'DAILY' | 'WEEKLY'>('DAILY');
  const [error, setError] = useState('');

  async function load() {
    const [j, w] = await Promise.all([
      apiFetch<ScheduledJob[]>('/jobs'),
      apiFetch<Workflow[]>('/workflows'),
    ]);
    setJobs(j);
    setWorkflows(w);
    if (w.length > 0) setWorkflowId(w[0].id);
  }

  async function create() {
    if (!name.trim() || !workflowId) return;
    try {
      await apiFetch<ScheduledJob>('/jobs', { method: 'POST', body: JSON.stringify({ name, workflowId, scheduleType }) });
      setName('');
      setError('');
      await load();
    } catch (e) { setError(e instanceof Error ? e.message : 'Failed to create'); }
  }

  async function toggle(id: string) {
    await apiFetch(`/jobs/${id}/enabled`, { method: 'PATCH' });
    await load();
  }

  async function remove(id: string) {
    if (!confirm('Delete this job?')) return;
    await apiFetch(`/jobs/${id}`, { method: 'DELETE' });
    await load();
  }

  useEffect(() => { void load(); }, []);

  const workflowName = (id: string) => workflows.find((w) => w.id === id)?.name ?? id;

  return (
    <div className="space-y-6">
      <section className="rounded-3xl border border-[var(--border)] bg-[var(--panel)] p-6">
        <h1 className="mb-4 text-3xl font-semibold">Scheduled Jobs</h1>
        <div className="grid gap-3 md:grid-cols-4">
          <input className="rounded-2xl border border-slate-300 px-4 py-3" value={name} onChange={(e) => setName(e.target.value)} placeholder="Job name" />
          <select className="rounded-2xl border border-slate-300 px-4 py-3 bg-transparent" value={workflowId} onChange={(e) => setWorkflowId(e.target.value)}>
            {workflows.map((w) => <option key={w.id} value={w.id}>{w.name}</option>)}
          </select>
          <select className="rounded-2xl border border-slate-300 px-4 py-3 bg-transparent" value={scheduleType} onChange={(e) => setScheduleType(e.target.value as typeof scheduleType)}>
            <option value="HOURLY">Hourly</option>
            <option value="DAILY">Daily</option>
            <option value="WEEKLY">Weekly</option>
          </select>
          <button className="rounded-2xl bg-[var(--brand)] px-5 py-3 font-semibold text-slate-950" onClick={create}>Schedule</button>
        </div>
        {error ? <p className="mt-3 text-sm text-red-300">{error}</p> : null}
      </section>

      <section className="rounded-3xl border border-[var(--border)] bg-[var(--panel)] p-6">
        <h2 className="mb-4 text-2xl font-semibold">Active jobs</h2>
        {jobs.length === 0 && <p className="text-[var(--muted)]">No scheduled jobs yet.</p>}
        <div className="space-y-3">
          {jobs.map((job) => (
            <div key={job.id} className="flex items-center justify-between rounded-2xl border border-[var(--border)] bg-[var(--panel-soft)] px-5 py-4">
              <div>
                <div className="font-semibold">{job.name}</div>
                <div className="text-sm text-[var(--muted)]">{job.scheduleType} · {workflowName(job.workflowId)}</div>
                {job.lastRunAt && <div className="text-xs text-[var(--muted)]">Last run: {new Date(job.lastRunAt).toLocaleString()}</div>}
                {job.nextRunAt && <div className="text-xs text-[var(--muted)]">Next run: {new Date(job.nextRunAt).toLocaleString()}</div>}
              </div>
              <div className="flex gap-3">
                <button onClick={() => toggle(job.id)} className={`rounded-2xl border px-4 py-1.5 text-sm ${job.enabled ? 'border-yellow-600 text-yellow-400' : 'border-green-700 text-green-400'}`}>
                  {job.enabled ? 'Disable' : 'Enable'}
                </button>
                <button onClick={() => remove(job.id)} className="rounded-2xl border border-red-800 px-4 py-1.5 text-sm text-red-400">Delete</button>
              </div>
            </div>
          ))}
        </div>
      </section>
    </div>
  );
}
