'use client';

import { useEffect, useState } from 'react';
import { apiFetch } from '@/lib/api';
import { PromptTemplate } from '@/lib/types';

export default function TemplatesPage() {
  const [templates, setTemplates] = useState<PromptTemplate[]>([]);
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [content, setContent] = useState('');
  const [selected, setSelected] = useState<PromptTemplate | null>(null);
  const [renderVars, setRenderVars] = useState<Record<string, string>>({});
  const [rendered, setRendered] = useState('');
  const [error, setError] = useState('');

  async function load() {
    const data = await apiFetch<PromptTemplate[]>('/templates');
    setTemplates(data);
  }

  async function create() {
    if (!name.trim() || !content.trim()) return;
    try {
      await apiFetch<PromptTemplate>('/templates', { method: 'POST', body: JSON.stringify({ name, description, content }) });
      setName(''); setDescription(''); setContent('');
      setError('');
      await load();
    } catch (e) { setError(e instanceof Error ? e.message : 'Failed to create'); }
  }

  async function render() {
    if (!selected) return;
    try {
      const result = await apiFetch<string>(`/templates/${selected.id}/render`, {
        method: 'POST',
        body: JSON.stringify({ variables: renderVars }),
      });
      setRendered(result);
    } catch (e) { setError(e instanceof Error ? e.message : 'Failed to render'); }
  }

  async function remove(id: string) {
    await apiFetch(`/templates/${id}`, { method: 'DELETE' });
    if (selected?.id === id) setSelected(null);
    await load();
  }

  function selectTemplate(t: PromptTemplate) {
    setSelected(t);
    setRenderVars(Object.fromEntries(t.variables.map((v) => [v, ''])));
    setRendered('');
  }

  useEffect(() => { void load(); }, []);

  return (
    <div className="grid gap-6 lg:grid-cols-[1fr_420px]">
      <div className="space-y-6">
        <section className="rounded-3xl border border-[var(--border)] bg-[var(--panel)] p-6 space-y-3">
          <h1 className="text-3xl font-semibold">Prompt Templates</h1>
          <input className="w-full rounded-2xl border border-slate-300 px-4 py-3" value={name} onChange={(e) => setName(e.target.value)} placeholder="Template name" />
          <input className="w-full rounded-2xl border border-slate-300 px-4 py-3" value={description} onChange={(e) => setDescription(e.target.value)} placeholder="Description (optional)" />
          <textarea rows={4} className="w-full rounded-2xl border border-slate-300 px-4 py-3 text-sm" value={content} onChange={(e) => setContent(e.target.value)} placeholder="Write an email about {{topic}} in a {{tone}} tone." />
          <p className="text-xs text-[var(--muted)]">Use {'{{variableName}}'} for dynamic placeholders.</p>
          <button onClick={create} className="rounded-2xl bg-[var(--brand)] px-5 py-3 font-semibold text-slate-950">Create</button>
          {error ? <p className="text-sm text-red-300">{error}</p> : null}
        </section>

        <section className="rounded-3xl border border-[var(--border)] bg-[var(--panel)] p-6">
          <h2 className="mb-4 text-xl font-semibold">Your templates</h2>
          {templates.length === 0 && <p className="text-[var(--muted)]">No templates yet.</p>}
          <div className="space-y-3">
            {templates.map((t) => (
              <div key={t.id} className={`rounded-2xl border px-4 py-3 cursor-pointer transition ${selected?.id === t.id ? 'border-[var(--brand)]' : 'border-[var(--border)]'} bg-[var(--panel-soft)]`} onClick={() => selectTemplate(t)}>
                <div className="flex items-center justify-between">
                  <span className="font-semibold">{t.name}</span>
                  <button onClick={(e) => { e.stopPropagation(); remove(t.id); }} className="text-xs text-red-400 hover:text-red-300">Delete</button>
                </div>
                <p className="mt-1 text-sm text-[var(--muted)]">{t.description}</p>
                {t.variables.length > 0 && (
                  <div className="mt-2 flex flex-wrap gap-1">
                    {t.variables.map((v) => <span key={v} className="rounded-full bg-[var(--brand)]/20 px-2 py-0.5 text-xs text-[var(--brand)]">{v}</span>)}
                  </div>
                )}
              </div>
            ))}
          </div>
        </section>
      </div>

      {selected && (
        <section className="rounded-3xl border border-[var(--border)] bg-[var(--panel)] p-6 space-y-4 h-fit">
          <h2 className="text-xl font-semibold">Render: {selected.name}</h2>
          <pre className="rounded-2xl bg-black/30 p-3 text-xs text-[var(--muted)] whitespace-pre-wrap">{selected.content}</pre>
          {selected.variables.map((v) => (
            <div key={v}>
              <label className="mb-1 block text-sm text-[var(--muted)]">{v}</label>
              <input className="w-full rounded-2xl border border-slate-300 px-3 py-2 text-sm" value={renderVars[v] ?? ''} onChange={(e) => setRenderVars({ ...renderVars, [v]: e.target.value })} placeholder={`Value for ${v}`} />
            </div>
          ))}
          <button onClick={render} className="w-full rounded-2xl bg-[var(--brand)] py-3 font-semibold text-slate-950">Render</button>
          {rendered && <div className="rounded-2xl bg-black/30 p-4 text-sm whitespace-pre-wrap">{rendered}</div>}
        </section>
      )}
    </div>
  );
}
