'use client';

import { useEffect, useRef, useState } from 'react';
import { useParams } from 'next/navigation';
import { apiFetch } from '@/lib/api';
import { Workflow, WorkflowExecution, WorkflowStep, Agent } from '@/lib/types';

const STEP_TYPES = ['KNOWLEDGE_SEARCH', 'WEB_SEARCH', 'LLM_PROMPT', 'SUMMARIZE', 'HTTP_REQUEST'];
const STATUS_COLOR: Record<string, string> = {
  PENDING: 'text-yellow-400',
  RUNNING: 'text-blue-400',
  COMPLETED: 'text-green-400',
  FAILED: 'text-red-400',
};

function newStep(): WorkflowStep {
  return { id: crypto.randomUUID(), name: 'New step', type: 'LLM_PROMPT', inputTemplate: '{{input}}', outputKey: 'output', config: {} };
}

export default function WorkflowDetailPage() {
  const { id } = useParams<{ id: string }>();
  const [workflow, setWorkflow] = useState<Workflow | null>(null);
  const [executions, setExecutions] = useState<WorkflowExecution[]>([]);
  const [agents, setAgents] = useState<Agent[]>([]);
  const [input, setInput] = useState('');
  const [saving, setSaving] = useState(false);
  const [status, setStatus] = useState('');
  const pollRef = useRef<ReturnType<typeof setInterval> | null>(null);

  async function load() {
    const [w, execs, ags] = await Promise.all([
      apiFetch<Workflow>(`/workflows/${id}`),
      apiFetch<WorkflowExecution[]>(`/workflows/${id}/executions`),
      apiFetch<Agent[]>('/agents'),
    ]);
    setWorkflow(w);
    setExecutions(execs);
    setAgents(ags);
  }

  async function save() {
    if (!workflow) return;
    setSaving(true);
    try {
      await apiFetch(`/workflows/${id}`, { method: 'PUT', body: JSON.stringify(workflow) });
      setStatus('Saved');
    } catch (e) { setStatus(e instanceof Error ? e.message : 'Save failed'); }
    finally { setSaving(false); }
  }

  async function execute() {
    setSaving(true);
    setStatus('Queuing...');
    try {
      const exec = await apiFetch<WorkflowExecution>(`/workflows/${id}/execute`, {
        method: 'POST',
        body: JSON.stringify({ input }),
      });
      setExecutions((prev) => [exec, ...prev]);
      pollExecution(exec.id);
      setStatus('Running...');
    } catch (e) { setStatus(e instanceof Error ? e.message : 'Execute failed'); }
    finally { setSaving(false); }
  }

  function pollExecution(execId: string) {
    if (pollRef.current) clearInterval(pollRef.current);
    pollRef.current = setInterval(async () => {
      const updated = await apiFetch<WorkflowExecution>(`/workflows/executions/${execId}`);
      setExecutions((prev) => prev.map((e) => e.id === execId ? updated : e));
      if (updated.status === 'COMPLETED' || updated.status === 'FAILED') {
        clearInterval(pollRef.current!);
        setStatus(updated.status === 'COMPLETED' ? 'Completed' : 'Failed: ' + updated.error);
      }
    }, 2000);
  }

  function updateStep(idx: number, patch: Partial<WorkflowStep>) {
    if (!workflow) return;
    const steps = workflow.steps.map((s, i) => i === idx ? { ...s, ...patch } : s);
    setWorkflow({ ...workflow, steps });
  }

  function removeStep(idx: number) {
    if (!workflow) return;
    setWorkflow({ ...workflow, steps: workflow.steps.filter((_, i) => i !== idx) });
  }

  function addStep() {
    if (!workflow) return;
    setWorkflow({ ...workflow, steps: [...workflow.steps, newStep()] });
  }

  useEffect(() => { void load(); return () => { if (pollRef.current) clearInterval(pollRef.current); }; }, [id]);

  if (!workflow) return <p className="text-[var(--muted)]">Loading...</p>;

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-3xl font-semibold">{workflow.name}</h1>
        <div className="flex gap-3">
          <span className="text-sm text-[var(--muted)]">{status}</span>
          <button disabled={saving} onClick={save} className="rounded-2xl bg-[var(--brand)] px-5 py-2.5 text-sm font-semibold text-slate-950 disabled:opacity-50">Save</button>
        </div>
      </div>

      {/* Agent selection */}
      <section className="rounded-3xl border border-[var(--border)] bg-[var(--panel)] p-6 space-y-3">
        <h2 className="text-xl font-semibold">Settings</h2>
        <div>
          <label className="mb-1 block text-sm text-[var(--muted)]">Default agent (for LLM steps)</label>
          <select className="w-full rounded-2xl border border-slate-300 px-4 py-3 bg-transparent" value={workflow.agentId ?? ''} onChange={(e) => setWorkflow({ ...workflow, agentId: e.target.value })}>
            <option value="">— none —</option>
            {agents.map((a) => <option key={a.id} value={a.id}>{a.name}</option>)}
          </select>
        </div>
      </section>

      {/* Step builder */}
      <section className="rounded-3xl border border-[var(--border)] bg-[var(--panel)] p-6 space-y-4">
        <div className="flex items-center justify-between">
          <h2 className="text-xl font-semibold">Steps</h2>
          <button onClick={addStep} className="rounded-2xl border border-[var(--brand)] px-4 py-2 text-sm text-[var(--brand)] hover:bg-[var(--brand)]/10">+ Add step</button>
        </div>
        {workflow.steps.length === 0 && <p className="text-sm text-[var(--muted)]">No steps yet. Add a step to build your workflow.</p>}
        {workflow.steps.map((step, idx) => (
          <div key={step.id} className="rounded-2xl border border-[var(--border)] bg-black/20 p-4 space-y-3">
            <div className="flex items-center gap-2">
              <span className="text-xs text-[var(--muted)]">{idx + 1}</span>
              <input className="flex-1 rounded-xl border border-slate-300 px-3 py-1.5 text-sm" value={step.name} onChange={(e) => updateStep(idx, { name: e.target.value })} placeholder="Step name" />
              <select className="rounded-xl border border-slate-300 px-3 py-1.5 text-sm bg-transparent" value={step.type} onChange={(e) => updateStep(idx, { type: e.target.value as WorkflowStep['type'] })}>
                {STEP_TYPES.map((t) => <option key={t} value={t}>{t.replace(/_/g, ' ')}</option>)}
              </select>
              <button onClick={() => removeStep(idx)} className="text-xs text-red-400 hover:text-red-300">✕</button>
            </div>
            <div>
              <label className="mb-1 block text-xs text-[var(--muted)]">Input template — use {'{{variableName}}'} to reference context</label>
              <input className="w-full rounded-xl border border-slate-300 px-3 py-1.5 text-sm" value={step.inputTemplate} onChange={(e) => updateStep(idx, { inputTemplate: e.target.value })} placeholder="{{input}}" />
            </div>
            <div>
              <label className="mb-1 block text-xs text-[var(--muted)]">Output key (stored in context)</label>
              <input className="w-full rounded-xl border border-slate-300 px-3 py-1.5 text-sm" value={step.outputKey} onChange={(e) => updateStep(idx, { outputKey: e.target.value })} placeholder="output" />
            </div>
            {step.type === 'LLM_PROMPT' && (
              <div>
                <label className="mb-1 block text-xs text-[var(--muted)]">System prompt</label>
                <input className="w-full rounded-xl border border-slate-300 px-3 py-1.5 text-sm" value={step.config?.systemPrompt ?? ''} onChange={(e) => updateStep(idx, { config: { ...step.config, systemPrompt: e.target.value } })} placeholder="You are a helpful assistant." />
              </div>
            )}
            {step.type === 'HTTP_REQUEST' && (
              <div>
                <label className="mb-1 block text-xs text-[var(--muted)]">URL</label>
                <input className="w-full rounded-xl border border-slate-300 px-3 py-1.5 text-sm" value={step.config?.url ?? ''} onChange={(e) => updateStep(idx, { config: { ...step.config, url: e.target.value } })} placeholder="https://api.example.com/endpoint" />
              </div>
            )}
          </div>
        ))}
      </section>

      {/* Execute */}
      <section className="rounded-3xl border border-[var(--border)] bg-[var(--panel)] p-6 space-y-3">
        <h2 className="text-xl font-semibold">Execute</h2>
        <div>
          <label className="mb-1 block text-sm text-[var(--muted)]">Initial input (available as {'{{input}}'})</label>
          <textarea rows={3} className="w-full rounded-2xl border border-slate-300 px-4 py-3 text-sm" value={input} onChange={(e) => setInput(e.target.value)} placeholder="Enter your query or starting data..." />
        </div>
        <button disabled={saving} onClick={execute} className="rounded-2xl bg-[var(--brand)] px-6 py-3 font-semibold text-slate-950 disabled:opacity-50">Run workflow</button>
      </section>

      {/* Execution history */}
      <section className="rounded-3xl border border-[var(--border)] bg-[var(--panel)] p-6 space-y-4">
        <h2 className="text-xl font-semibold">Execution history</h2>
        {executions.length === 0 && <p className="text-sm text-[var(--muted)]">No executions yet.</p>}
        {executions.map((exec) => (
          <div key={exec.id} className="rounded-2xl border border-[var(--border)] bg-black/20 p-4 space-y-2">
            <div className="flex items-center justify-between">
              <span className={`text-sm font-semibold ${STATUS_COLOR[exec.status] ?? ''}`}>{exec.status}</span>
              <span className="text-xs text-[var(--muted)]">{exec.createdAt ? new Date(exec.createdAt).toLocaleString() : ''}</span>
            </div>
            {exec.stepResults?.map((r) => (
              <div key={r.stepId} className="flex items-center gap-2 text-xs">
                <span className={r.status === 'SUCCESS' ? 'text-green-400' : 'text-red-400'}>●</span>
                <span className="font-medium">{r.stepName}</span>
                <span className="text-[var(--muted)]">{r.durationMs}ms</span>
                {r.error && <span className="text-red-400">{r.error}</span>}
              </div>
            ))}
            {exec.finalOutput && (
              <div className="mt-2 rounded-xl bg-black/30 p-3 text-xs text-white whitespace-pre-wrap">{exec.finalOutput}</div>
            )}
          </div>
        ))}
      </section>
    </div>
  );
}
