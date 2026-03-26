'use client';

import { useState } from 'react';
import { apiFetch } from '@/lib/api';
import { Skill, SkillStatus } from '@/lib/types';

interface Props {
  agentId: string;
  initial?: Skill;
  onSaved: (skill: Skill) => void;
  onCancel: () => void;
}

export default function SkillForm({ agentId, initial, onSaved, onCancel }: Props) {
  const isEdit = !!initial;
  const [tab, setTab] = useState<'details' | 'instructions'>('details');
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  // Frontmatter fields
  const [name, setName] = useState(initial?.name ?? '');
  const [description, setDescription] = useState(initial?.description ?? '');
  const [license, setLicense] = useState(initial?.license ?? '');
  const [compatibility, setCompatibility] = useState(initial?.compatibility ?? '');
  const [status, setStatus] = useState<SkillStatus>(initial?.status ?? 'ACTIVE');
  const [modelTool, setModelTool] = useState(initial?.modelTool ?? false);
  const [instructions, setInstructions] = useState(initial?.instructions ?? '');

  // skillMetadata key-value pairs
  const [metaEntries, setMetaEntries] = useState<{ k: string; v: string; _key: number }[]>(
    Object.entries(initial?.skillMetadata ?? {}).map(([k, v], i) => ({ k, v, _key: i }))
  );

  // allowedTools list
  const [allowedTools, setAllowedTools] = useState<string[]>(initial?.allowedTools ?? []);

  async function handleSave() {
    if (!name.trim()) { setError('Name is required'); return; }
    setSaving(true);
    setError('');
    try {
      const body = {
        name: name.trim(),
        description: description.trim() || null,
        license: license.trim() || null,
        compatibility: compatibility.trim() || null,
        skillMetadata: Object.fromEntries(metaEntries.filter(e => e.k).map(e => [e.k, e.v])),
        allowedTools: allowedTools.filter(Boolean),
        instructions: instructions || null,
        status,
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
          placeholder="Skill name"
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
        {(['details', 'instructions'] as const).map(t => (
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
            {/* Description */}
            <div>
              <label className="text-xs text-[var(--muted)] uppercase tracking-wider mb-1 block">Description</label>
              <textarea
                className="w-full bg-[var(--panel-soft)] border border-[var(--border)] rounded-xl px-3 py-2 text-sm resize-none outline-none focus:border-[var(--brand)] placeholder:text-[var(--muted)]"
                rows={3}
                placeholder="When to trigger and what this skill does — shown to the LLM as context"
                value={description}
                onChange={e => setDescription(e.target.value)}
              />
            </div>

            {/* License + Compatibility */}
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="text-xs text-[var(--muted)] uppercase tracking-wider mb-1 block">License</label>
                <input
                  className="w-full bg-[var(--panel-soft)] border border-[var(--border)] rounded-xl px-3 py-2 text-sm outline-none focus:border-[var(--brand)] placeholder:text-[var(--muted)]"
                  placeholder="e.g. Apache-2.0"
                  value={license}
                  onChange={e => setLicense(e.target.value)}
                />
              </div>
              <div>
                <label className="text-xs text-[var(--muted)] uppercase tracking-wider mb-1 block">Compatibility</label>
                <input
                  className="w-full bg-[var(--panel-soft)] border border-[var(--border)] rounded-xl px-3 py-2 text-sm outline-none focus:border-[var(--brand)] placeholder:text-[var(--muted)]"
                  placeholder="e.g. Requires Python 3.10+"
                  value={compatibility}
                  onChange={e => setCompatibility(e.target.value)}
                />
              </div>
            </div>

            {/* Status */}
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

            {/* Allowed Tools */}
            <div>
              <div className="flex items-center justify-between mb-2">
                <label className="text-xs text-[var(--muted)] uppercase tracking-wider">Allowed Tools</label>
                <button
                  type="button"
                  onClick={() => setAllowedTools(t => [...t, ''])}
                  className="text-xs text-[var(--brand)] hover:underline"
                >
                  + Add
                </button>
              </div>
              {allowedTools.length === 0 && <p className="text-xs text-[var(--muted)]">No allowed tools added yet.</p>}
              <div className="space-y-1">
                {allowedTools.map((tool, i) => (
                  <div key={i} className="flex gap-2 items-center">
                    <input
                      className="flex-1 bg-[var(--panel-soft)] border border-[var(--border)] rounded-lg px-2 py-1.5 text-sm outline-none"
                      placeholder="e.g. Bash(git:*)"
                      value={tool}
                      onChange={e => setAllowedTools(prev => prev.map((t, ti) => ti === i ? e.target.value : t))}
                    />
                    <button
                      type="button"
                      onClick={() => setAllowedTools(prev => prev.filter((_, ti) => ti !== i))}
                      className="text-[var(--muted)] hover:text-red-400 text-lg leading-none"
                    >
                      ×
                    </button>
                  </div>
                ))}
              </div>
            </div>

            {/* Metadata */}
            <div>
              <div className="flex items-center justify-between mb-2">
                <label className="text-xs text-[var(--muted)] uppercase tracking-wider">Metadata</label>
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
          </>
        )}

        {tab === 'instructions' && (
          <div>
            <label className="text-xs text-[var(--muted)] uppercase tracking-wider mb-2 block">SKILL.md Instructions</label>
            <textarea
              className="w-full h-96 bg-[var(--panel-soft)] border border-[var(--border)] rounded-xl px-4 py-3 text-sm font-mono resize-y outline-none focus:border-[var(--brand)] placeholder:text-[var(--muted)]"
              placeholder="Write the skill instructions here. This is the body of your SKILL.md — tell the LLM what to do, how to behave, and what frameworks or steps to follow."
              value={instructions}
              onChange={e => setInstructions(e.target.value)}
              spellCheck={false}
            />
            <p className="mt-2 text-xs text-[var(--muted)]">
              This content is injected into the LLM context when this skill is active.
            </p>
          </div>
        )}
      </div>
    </div>
  );
}
