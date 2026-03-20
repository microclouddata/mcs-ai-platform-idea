'use client';

import { useState } from 'react';
import { apiFetch } from '@/lib/api';
import { Skill, SkillLanguage, SkillParameter, SkillStatus } from '@/lib/types';

interface Props {
  agentId: string;
  initial?: Skill;
  onSaved: (skill: Skill) => void;
  onCancel: () => void;
}

const LANGUAGES: SkillLanguage[] = ['PYTHON', 'JAVASCRIPT', 'JAVA'];

function emptyParam(): SkillParameter & { _key: number } {
  return { name: '', type: 'string', description: '', _key: Date.now() + Math.random() };
}

export default function SkillForm({ agentId, initial, onSaved, onCancel }: Props) {
  const isEdit = !!initial;
  const [tab, setTab] = useState<'details' | 'code'>('details');
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  // Fields
  const [name, setName] = useState(initial?.name ?? '');
  const [description, setDescription] = useState(initial?.description ?? '');
  const [language, setLanguage] = useState<SkillLanguage>(initial?.language ?? 'PYTHON');
  const [status, setStatus] = useState<SkillStatus>(initial?.status ?? 'ACTIVE');
  const [modelTool, setModelTool] = useState(initial?.modelTool ?? false);
  const [docId, setDocId] = useState(initial?.docId ?? '');
  const [code, setCode] = useState(initial?.code ?? '');

  // Dynamic lists
  const [controlFlags, setControlFlags] = useState<string[]>(initial?.controlFlags ?? []);
  const [tags, setTags] = useState<string[]>(initial?.tags ?? []);
  const [params, setParams] = useState<(SkillParameter & { _key: number })[]>(
    (initial?.parameters ?? []).map((p, i) => ({ ...p, _key: i }))
  );
  const [metaEntries, setMetaEntries] = useState<{ k: string; v: string; _key: number }[]>(
    Object.entries(initial?.metadata ?? {}).map(([k, v], i) => ({ k, v, _key: i }))
  );

  async function handleSave() {
    if (!name.trim()) { setError('Name is required'); return; }
    setSaving(true);
    setError('');
    try {
      const body = {
        name: name.trim(),
        description: description.trim(),
        code,
        language,
        status,
        skillType: 'CODE',
        docId: docId.trim() || null,
        controlFlags,
        metadata: Object.fromEntries(metaEntries.filter(e => e.k).map(e => [e.k, e.v])),
        tags,
        parameters: params.map(({ name, type, description }) => ({ name, type, description })),
        modelTool,
      };
      const path = isEdit ? `/agents/${agentId}/skills/${initial!.id}` : `/agents/${agentId}/skills`;
      const saved = await apiFetch<Skill>(path, {
        method: isEdit ? 'PUT' : 'POST',
        body: JSON.stringify(body),
      });
      onSaved(saved);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Save failed');
    } finally {
      setSaving(false);
    }
  }

  return (
    <div className="flex flex-col h-full">
      {/* Header */}
      <div className="flex items-center justify-between px-6 py-4 border-b border-[var(--border)]">
        <input
          className="text-lg font-semibold bg-transparent border-none outline-none text-[var(--foreground)] placeholder:text-[var(--muted)] w-full"
          placeholder="Click to add title"
          value={name}
          onChange={e => setName(e.target.value)}
        />
        <div className="flex items-center gap-3 ml-4 shrink-0">
          <label className="flex items-center gap-2 text-sm text-[var(--muted)] cursor-pointer">
            <span>Model Tool:</span>
            <button
              type="button"
              onClick={() => setModelTool(v => !v)}
              className={`relative w-10 h-5 rounded-full transition-colors ${modelTool ? 'bg-[var(--brand)]' : 'bg-[var(--border)]'}`}
            >
              <span className={`absolute top-0.5 left-0.5 w-4 h-4 rounded-full bg-white transition-transform ${modelTool ? 'translate-x-5' : ''}`} />
            </button>
            <span className={modelTool ? 'text-[var(--brand)]' : ''}>{modelTool ? 'ON' : 'OFF'}</span>
          </label>
          <button onClick={onCancel} className="px-4 py-1.5 text-sm rounded-lg border border-[var(--border)] hover:bg-[var(--panel-soft)]">Close</button>
          <button
            onClick={handleSave}
            disabled={saving}
            className="px-4 py-1.5 text-sm rounded-lg bg-[var(--brand)] text-white hover:opacity-90 disabled:opacity-50"
          >
            {saving ? 'Saving…' : 'Save'}
          </button>
        </div>
      </div>

      {/* Tabs */}
      <div className="flex gap-6 px-6 pt-3 border-b border-[var(--border)] text-sm">
        {(['details', 'code'] as const).map(t => (
          <button
            key={t}
            onClick={() => setTab(t)}
            className={`pb-2 capitalize font-medium border-b-2 transition-colors ${tab === t ? 'border-[var(--brand)] text-[var(--brand)]' : 'border-transparent text-[var(--muted)]'}`}
          >
            {t}
          </button>
        ))}
      </div>

      {error && <p className="px-6 pt-3 text-xs text-red-400">{error}</p>}

      <div className="flex-1 overflow-y-auto p-6 space-y-5">
        {tab === 'details' && (
          <>
            {/* Prompt / Description */}
            <div>
              <label className="text-xs text-[var(--muted)] uppercase tracking-wider mb-1 block">Prompt / Description</label>
              <textarea
                className="w-full bg-[var(--panel-soft)] border border-[var(--border)] rounded-xl px-3 py-2 text-sm resize-none outline-none focus:border-[var(--brand)] placeholder:text-[var(--muted)]"
                rows={3}
                placeholder="Click to add prompt"
                value={description}
                onChange={e => setDescription(e.target.value)}
              />
            </div>

            {/* Language + Status */}
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="text-xs text-[var(--muted)] uppercase tracking-wider mb-1 block">Language</label>
                <select
                  value={language}
                  onChange={e => setLanguage(e.target.value as SkillLanguage)}
                  className="w-full bg-[var(--panel-soft)] border border-[var(--border)] rounded-xl px-3 py-2 text-sm outline-none"
                >
                  {LANGUAGES.map(l => <option key={l} value={l}>{l}</option>)}
                </select>
              </div>
              <div>
                <label className="text-xs text-[var(--muted)] uppercase tracking-wider mb-1 block">Status</label>
                <button
                  type="button"
                  onClick={() => setStatus(s => s === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE')}
                  className={`px-4 py-2 rounded-xl text-sm font-medium ${status === 'ACTIVE' ? 'bg-green-500/20 text-green-400' : 'bg-[var(--panel-soft)] text-[var(--muted)]'}`}
                >
                  {status}
                </button>
              </div>
            </div>

            {/* Doc ID */}
            <div>
              <label className="text-xs text-[var(--muted)] uppercase tracking-wider mb-1 block">Doc ID</label>
              <input
                className="w-full bg-[var(--panel-soft)] border border-[var(--border)] rounded-xl px-3 py-2 text-sm outline-none focus:border-[var(--brand)] placeholder:text-[var(--muted)]"
                placeholder="NONE"
                value={docId}
                onChange={e => setDocId(e.target.value)}
              />
            </div>

            {/* Parameters */}
            <div>
              <div className="flex items-center justify-between mb-2">
                <label className="text-xs text-[var(--muted)] uppercase tracking-wider">Parameters</label>
                <button
                  type="button"
                  onClick={() => setParams(p => [...p, emptyParam()])}
                  className="text-xs text-[var(--brand)] hover:underline"
                >
                  + Add
                </button>
              </div>
              {params.length === 0 && <p className="text-xs text-[var(--muted)]">No parameters added yet.</p>}
              <div className="space-y-2">
                {params.map((p, i) => (
                  <div key={p._key} className="flex gap-2 items-start">
                    <input
                      placeholder="name"
                      className="flex-1 bg-[var(--panel-soft)] border border-[var(--border)] rounded-lg px-2 py-1.5 text-sm outline-none"
                      value={p.name}
                      onChange={e => setParams(prev => prev.map((x, xi) => xi === i ? { ...x, name: e.target.value } : x))}
                    />
                    <input
                      placeholder="type"
                      className="w-24 bg-[var(--panel-soft)] border border-[var(--border)] rounded-lg px-2 py-1.5 text-sm outline-none"
                      value={p.type}
                      onChange={e => setParams(prev => prev.map((x, xi) => xi === i ? { ...x, type: e.target.value } : x))}
                    />
                    <input
                      placeholder="description"
                      className="flex-1 bg-[var(--panel-soft)] border border-[var(--border)] rounded-lg px-2 py-1.5 text-sm outline-none"
                      value={p.description}
                      onChange={e => setParams(prev => prev.map((x, xi) => xi === i ? { ...x, description: e.target.value } : x))}
                    />
                    <button
                      type="button"
                      onClick={() => setParams(prev => prev.filter((_, xi) => xi !== i))}
                      className="text-[var(--muted)] hover:text-red-400 text-lg leading-none mt-0.5"
                    >
                      ×
                    </button>
                  </div>
                ))}
              </div>
            </div>

            {/* MetaData */}
            <div>
              <div className="flex items-center justify-between mb-2">
                <label className="text-xs text-[var(--muted)] uppercase tracking-wider">MetaData</label>
                <button
                  type="button"
                  onClick={() => setMetaEntries(m => [...m, { k: '', v: '', _key: Date.now() + Math.random() }])}
                  className="text-xs text-[var(--brand)] hover:underline"
                >
                  + Add
                </button>
              </div>
              {metaEntries.length === 0 && <p className="text-xs text-[var(--muted)]">No metadata added yet.</p>}
              <div className="space-y-2">
                {metaEntries.map((entry, i) => (
                  <div key={entry._key} className="flex gap-2 items-center">
                    <input
                      placeholder="key"
                      className="flex-1 bg-[var(--panel-soft)] border border-[var(--border)] rounded-lg px-2 py-1.5 text-sm outline-none"
                      value={entry.k}
                      onChange={e => setMetaEntries(prev => prev.map((x, xi) => xi === i ? { ...x, k: e.target.value } : x))}
                    />
                    <input
                      placeholder="value"
                      className="flex-1 bg-[var(--panel-soft)] border border-[var(--border)] rounded-lg px-2 py-1.5 text-sm outline-none"
                      value={entry.v}
                      onChange={e => setMetaEntries(prev => prev.map((x, xi) => xi === i ? { ...x, v: e.target.value } : x))}
                    />
                    <button
                      type="button"
                      onClick={() => setMetaEntries(prev => prev.filter((_, xi) => xi !== i))}
                      className="text-[var(--muted)] hover:text-red-400 text-lg leading-none"
                    >
                      ×
                    </button>
                  </div>
                ))}
              </div>
            </div>

            {/* Tags */}
            <div>
              <div className="flex items-center justify-between mb-2">
                <label className="text-xs text-[var(--muted)] uppercase tracking-wider">Tags</label>
                <button
                  type="button"
                  onClick={() => setTags(t => [...t, ''])}
                  className="text-xs text-[var(--brand)] hover:underline"
                >
                  + Add
                </button>
              </div>
              {tags.length === 0 && <p className="text-xs text-[var(--muted)]">No tags added yet.</p>}
              <div className="flex flex-wrap gap-2">
                {tags.map((tag, i) => (
                  <div key={i} className="flex items-center gap-1 bg-[var(--panel-soft)] border border-[var(--border)] rounded-full px-2 py-0.5">
                    <input
                      className="bg-transparent text-sm outline-none w-20"
                      value={tag}
                      onChange={e => setTags(prev => prev.map((t, ti) => ti === i ? e.target.value : t))}
                    />
                    <button
                      type="button"
                      onClick={() => setTags(prev => prev.filter((_, ti) => ti !== i))}
                      className="text-[var(--muted)] hover:text-red-400"
                    >
                      ×
                    </button>
                  </div>
                ))}
              </div>
            </div>

            {/* Control Flags */}
            <div>
              <div className="flex items-center justify-between mb-2">
                <label className="text-xs text-[var(--muted)] uppercase tracking-wider">Control Flags</label>
                <button
                  type="button"
                  onClick={() => setControlFlags(f => [...f, ''])}
                  className="text-xs text-[var(--brand)] hover:underline"
                >
                  + Add
                </button>
              </div>
              {controlFlags.length === 0 && <p className="text-xs text-[var(--muted)]">No control flags added yet.</p>}
              <div className="space-y-1">
                {controlFlags.map((flag, i) => (
                  <div key={i} className="flex gap-2 items-center">
                    <input
                      className="flex-1 bg-[var(--panel-soft)] border border-[var(--border)] rounded-lg px-2 py-1.5 text-sm outline-none"
                      value={flag}
                      onChange={e => setControlFlags(prev => prev.map((f, fi) => fi === i ? e.target.value : f))}
                    />
                    <button
                      type="button"
                      onClick={() => setControlFlags(prev => prev.filter((_, fi) => fi !== i))}
                      className="text-[var(--muted)] hover:text-red-400 text-lg leading-none"
                    >
                      ×
                    </button>
                  </div>
                ))}
              </div>
            </div>
          </>
        )}

        {tab === 'code' && (
          <div>
            <div className="flex items-center justify-between mb-2">
              <label className="text-xs text-[var(--muted)] uppercase tracking-wider">{language} Editor</label>
            </div>
            <textarea
              className="w-full h-96 bg-[var(--panel-soft)] border border-[var(--border)] rounded-xl px-4 py-3 text-sm font-mono resize-y outline-none focus:border-[var(--brand)] placeholder:text-[var(--muted)]"
              placeholder={`# Write your ${language.toLowerCase()} code here\n# The variable 'input' contains the user's message`}
              value={code}
              onChange={e => setCode(e.target.value)}
              spellCheck={false}
            />
            <p className="mt-2 text-xs text-[var(--muted)]">
              Available variable: <code className="font-mono text-[var(--brand)]">input</code> — contains the user message passed to this skill.
            </p>
          </div>
        )}
      </div>
    </div>
  );
}
