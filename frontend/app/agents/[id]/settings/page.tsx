'use client';

import { useEffect, useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { apiFetch } from '@/lib/api';
import { Agent, KnowledgeBase } from '@/lib/types';

const MODELS = ['gpt-4.1-mini', 'gpt-4o', 'gpt-4o-mini', 'gpt-3.5-turbo'];
const TOOL_TYPES = ['KNOWLEDGE_SEARCH', 'WEB_SEARCH', 'CALCULATOR'];

export default function AgentSettingsPage() {
  const { id } = useParams<{ id: string }>();
  const router = useRouter();

  const [agent, setAgent] = useState<Agent | null>(null);
  const [kbs, setKbs] = useState<KnowledgeBase[]>([]);
  const [enabledTools, setEnabledTools] = useState<string[]>([]);
  const [agentEnabled, setAgentEnabled] = useState(true);
  const [status, setStatus] = useState('');
  const [saving, setSaving] = useState(false);

  // Form fields
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [systemPrompt, setSystemPrompt] = useState('');
  const [model, setModel] = useState('');
  const [temperature, setTemperature] = useState(0.2);
  const [memoryEnabled, setMemoryEnabled] = useState(false);
  const [toolsEnabled, setToolsEnabled] = useState(true);
  const [knowledgeBaseIds, setKnowledgeBaseIds] = useState<string[]>([]);

  useEffect(() => {
    async function load() {
      const [a, bindings, kbList] = await Promise.all([
        apiFetch<Agent>(`/agents/${id}`),
        apiFetch<{ toolType: string }[]>(`/agents/${id}/tools`),
        apiFetch<KnowledgeBase[]>('/knowledge-bases'),
      ]);
      setAgent(a);
      setName(a.name);
      setDescription(a.description);
      setSystemPrompt(a.systemPrompt);
      setModel(a.model);
      setTemperature(a.temperature);
      setAgentEnabled(a.enabled ?? true);
      setMemoryEnabled(a.memoryEnabled);
      setToolsEnabled(a.toolsEnabled);
      setKnowledgeBaseIds(a.knowledgeBaseIds ?? []);
      setEnabledTools(bindings.map((b) => b.toolType));
      setKbs(kbList);
    }
    void load();
  }, [id]);

  async function toggleDisable() {
    setSaving(true);
    try {
      const updated = await apiFetch<Agent>(`/agents/${id}/enabled`, { method: 'PATCH' });
      setAgentEnabled(updated.enabled);
      setStatus(updated.enabled ? 'Agent enabled' : 'Agent disabled');
    } catch (e) {
      setStatus(e instanceof Error ? e.message : 'Failed');
    } finally {
      setSaving(false);
    }
  }

  async function deleteAgent() {
    if (!confirm('Permanently delete this agent? This cannot be undone.')) return;
    setSaving(true);
    try {
      await apiFetch(`/agents/${id}`, { method: 'DELETE' });
      router.push('/dashboard');
    } catch (e) {
      setStatus(e instanceof Error ? e.message : 'Delete failed');
      setSaving(false);
    }
  }

  async function save() {
    setSaving(true);
    setStatus('Saving...');
    try {
      await apiFetch(`/agents/${id}`, {
        method: 'PUT',
        body: JSON.stringify({
          name,
          description,
          systemPrompt,
          model,
          temperature,
          memoryEnabled,
          toolsEnabled,
          knowledgeBaseIds,
        }),
      });
      await apiFetch(`/agents/${id}/tools`, {
        method: 'PUT',
        body: JSON.stringify(enabledTools),
      });
      setStatus('Saved');
    } catch (e) {
      setStatus(e instanceof Error ? e.message : 'Save failed');
    } finally {
      setSaving(false);
    }
  }

  function toggleTool(tool: string) {
    setEnabledTools((prev) =>
      prev.includes(tool) ? prev.filter((t) => t !== tool) : [...prev, tool]
    );
  }

  function toggleKb(kbId: string) {
    setKnowledgeBaseIds((prev) =>
      prev.includes(kbId) ? prev.filter((k) => k !== kbId) : [...prev, kbId]
    );
  }

  if (!agent) return <p className="text-[var(--muted)]">Loading...</p>;

  return (
    <div className="mx-auto max-w-2xl space-y-6">
      <div className="flex items-center gap-4">
        <button onClick={() => router.back()} className="text-sm text-[var(--muted)] hover:text-white">
          ← Back
        </button>
        <h1 className="text-3xl font-semibold">Agent Settings</h1>
      </div>

      <section className="rounded-3xl border border-[var(--border)] bg-[var(--panel)] p-6 space-y-4">
        <h2 className="text-xl font-semibold">General</h2>

        <div>
          <label className="mb-1 block text-sm text-[var(--muted)]">Name</label>
          <input className="w-full rounded-2xl border border-slate-300 px-4 py-3" value={name} onChange={(e) => setName(e.target.value)} />
        </div>

        <div>
          <label className="mb-1 block text-sm text-[var(--muted)]">Description</label>
          <input className="w-full rounded-2xl border border-slate-300 px-4 py-3" value={description} onChange={(e) => setDescription(e.target.value)} />
        </div>

        <div>
          <label className="mb-1 block text-sm text-[var(--muted)]">System Prompt</label>
          <textarea
            rows={4}
            className="w-full rounded-2xl border border-slate-300 px-4 py-3"
            value={systemPrompt}
            onChange={(e) => setSystemPrompt(e.target.value)}
          />
        </div>

        <div>
          <label className="mb-1 block text-sm text-[var(--muted)]">Model</label>
          <select className="w-full rounded-2xl border border-slate-300 px-4 py-3 bg-transparent" value={model} onChange={(e) => setModel(e.target.value)}>
            {MODELS.map((m) => <option key={m} value={m}>{m}</option>)}
          </select>
        </div>

        <div>
          <label className="mb-1 block text-sm text-[var(--muted)]">Temperature: {temperature}</label>
          <input type="range" min={0} max={2} step={0.1} value={temperature} onChange={(e) => setTemperature(parseFloat(e.target.value))} className="w-full" />
        </div>
      </section>

      <section className="rounded-3xl border border-[var(--border)] bg-[var(--panel)] p-6 space-y-4">
        <h2 className="text-xl font-semibold">Memory & Tools</h2>

        <label className="flex items-center gap-3 cursor-pointer">
          <input type="checkbox" checked={memoryEnabled} onChange={(e) => setMemoryEnabled(e.target.checked)} className="h-4 w-4" />
          <span>Enable memory (include conversation history in context)</span>
        </label>

        <label className="flex items-center gap-3 cursor-pointer">
          <input type="checkbox" checked={toolsEnabled} onChange={(e) => setToolsEnabled(e.target.checked)} className="h-4 w-4" />
          <span>Enable tools</span>
        </label>

        {toolsEnabled && (
          <div className="space-y-2">
            <p className="text-sm text-[var(--muted)]">Active tools:</p>
            {TOOL_TYPES.map((t) => (
              <label key={t} className="flex items-center gap-3 cursor-pointer">
                <input type="checkbox" checked={enabledTools.includes(t)} onChange={() => toggleTool(t)} className="h-4 w-4" />
                <span className="text-sm">{t.replace(/_/g, ' ')}</span>
              </label>
            ))}
          </div>
        )}
      </section>

      {kbs.length > 0 && (
        <section className="rounded-3xl border border-[var(--border)] bg-[var(--panel)] p-6 space-y-4">
          <h2 className="text-xl font-semibold">Knowledge Bases</h2>
          <p className="text-sm text-[var(--muted)]">Select which knowledge bases this agent can search:</p>
          {kbs.map((kb) => (
            <label key={kb.id} className="flex items-center gap-3 cursor-pointer">
              <input type="checkbox" checked={knowledgeBaseIds.includes(kb.id)} onChange={() => toggleKb(kb.id)} className="h-4 w-4" />
              <span className="text-sm">{kb.name}</span>
            </label>
          ))}
        </section>
      )}

      <div className="flex items-center justify-between">
        <p className="text-sm text-[var(--muted)]">{status}</p>
        <button
          disabled={saving}
          onClick={save}
          className="rounded-2xl bg-[var(--brand)] px-6 py-3 font-semibold text-slate-950 disabled:opacity-50"
        >
          Save settings
        </button>
      </div>

      <section className="rounded-3xl border border-red-900/40 bg-red-950/20 p-6 space-y-4">
        <h2 className="text-xl font-semibold text-red-300">Danger zone</h2>

        <div className="flex items-center justify-between">
          <div>
            <p className="font-medium">{agentEnabled ? 'Disable agent' : 'Enable agent'}</p>
            <p className="text-sm text-[var(--muted)]">
              {agentEnabled
                ? 'Disabled agents cannot be used for chat.'
                : 'Re-enable this agent to allow chat again.'}
            </p>
          </div>
          <button
            disabled={saving}
            onClick={toggleDisable}
            className="rounded-2xl border border-red-500 px-5 py-2.5 text-sm font-semibold text-red-400 hover:bg-red-500/10 disabled:opacity-50"
          >
            {agentEnabled ? 'Disable' : 'Enable'}
          </button>
        </div>

        <div className="border-t border-red-900/40 pt-4 flex items-center justify-between">
          <div>
            <p className="font-medium">Delete agent</p>
            <p className="text-sm text-[var(--muted)]">Permanently remove this agent. This cannot be undone.</p>
          </div>
          <button
            disabled={saving}
            onClick={deleteAgent}
            className="rounded-2xl bg-red-600 px-5 py-2.5 text-sm font-semibold text-white hover:bg-red-700 disabled:opacity-50"
          >
            Delete
          </button>
        </div>
      </section>
    </div>
  );
}
