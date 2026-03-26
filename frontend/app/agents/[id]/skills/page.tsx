'use client';

import { useEffect, useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import Link from 'next/link';
import { apiFetch } from '@/lib/api';
import { Agent, Skill } from '@/lib/types';
import SkillForm from './SkillForm';

function getCurrentUserId(): string | null {
  return typeof window !== 'undefined' ? localStorage.getItem('userId') : null;
}

type ViewMode = 'list' | 'create' | 'edit';
type DetailTab = 'details' | 'instructions' | 'analytics';

export default function SkillsPage() {
  const { id: agentId } = useParams<{ id: string }>();
  const router = useRouter();

  const [agent, setAgent] = useState<Agent | null>(null);
  const [skills, setSkills] = useState<Skill[]>([]);
  const [selected, setSelected] = useState<Skill | null>(null);
  const [mode, setMode] = useState<ViewMode>('list');
  const [detailTab, setDetailTab] = useState<DetailTab>('details');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    async function load() {
      try {
        const [a, ns] = await Promise.all([
          apiFetch<Agent>(`/agents/${agentId}`),
          apiFetch<Skill[]>(`/agents/${agentId}/skills`),
        ]);
        if (a.userId !== getCurrentUserId()) {
          router.replace(`/agents/${agentId}`);
          return;
        }
        setAgent(a);
        setSkills(ns);
      } catch (e) {
        setError(e instanceof Error ? e.message : 'Failed to load');
      } finally {
        setLoading(false);
      }
    }
    void load();
  }, [agentId]);

  async function handleToggleStatus(skill: Skill) {
    try {
      const updated = await apiFetch<Skill>(`/agents/${agentId}/skills/${skill.id}/status`, { method: 'PATCH' });
      setSkills(prev => prev.map(n => n.id === updated.id ? updated : n));
      if (selected?.id === updated.id) setSelected(updated);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Toggle failed');
    }
  }

  async function handleDelete(skill: Skill) {
    if (!confirm(`Delete skill "${skill.name}"? This cannot be undone.`)) return;
    try {
      await apiFetch(`/agents/${agentId}/skills/${skill.id}`, { method: 'DELETE' });
      setSkills(prev => prev.filter(n => n.id !== skill.id));
      if (selected?.id === skill.id) { setSelected(null); setMode('list'); }
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Delete failed');
    }
  }

  function handleSaved(skill: Skill) {
    setSkills(prev => {
      const exists = prev.find(n => n.id === skill.id);
      return exists ? prev.map(n => n.id === skill.id ? skill : n) : [skill, ...prev];
    });
    setSelected(skill);
    setMode('list');
  }

  if (loading) return <div className="p-8 text-[var(--muted)] text-sm">Loading skills…</div>;

  const showForm = mode === 'create' || mode === 'edit';

  return (
    <div className="min-h-screen bg-[var(--background)]">
      {/* Top nav */}
      <div className="border-b border-[var(--border)] px-6 py-3 flex items-center gap-4 text-sm">
        <Link href="/dashboard" className="text-[var(--muted)] hover:text-[var(--foreground)]">Dashboard</Link>
        <span className="text-[var(--border)]">/</span>
        <Link href={`/agents/${agentId}`} className="text-[var(--muted)] hover:text-[var(--foreground)]">{agent?.name ?? agentId}</Link>
        <span className="text-[var(--border)]">/</span>
        <span className="text-[var(--foreground)] font-medium">Skills</span>
        <div className="ml-auto flex gap-3">
          <Link href={`/agents/${agentId}/settings`} className="text-xs text-[var(--muted)] hover:text-[var(--foreground)]">Settings</Link>
        </div>
      </div>

      {error && (
        <div className="mx-6 mt-4 p-3 rounded-xl bg-red-500/10 text-red-400 text-sm">{error}</div>
      )}

      <div className="flex h-[calc(100vh-49px)]">
        {/* Left panel — skill list */}
        <div className="w-80 border-r border-[var(--border)] flex flex-col">
          <div className="flex items-center justify-between px-4 py-3 border-b border-[var(--border)]">
            <span className="text-sm font-semibold text-[var(--foreground)]">Skills</span>
            <div className="flex gap-2">
              {/* Language icons */}
              <span className="text-xs px-2 py-0.5 rounded bg-yellow-500/20 text-yellow-400 font-mono">JS</span>
              <span className="text-xs px-2 py-0.5 rounded bg-blue-500/20 text-blue-400 font-mono">Py</span>
              <button
                onClick={() => { setSelected(null); setMode('create'); }}
                className="text-xs px-3 py-0.5 rounded-lg bg-[var(--brand)] text-white hover:opacity-90"
              >
                Create
              </button>
            </div>
          </div>

          <div className="flex-1 overflow-y-auto">
            {skills.length === 0 && (
              <div className="p-6 text-sm text-[var(--muted)] text-center">
                No skills yet.<br />
                <button
                  onClick={() => { setSelected(null); setMode('create'); }}
                  className="mt-2 text-[var(--brand)] hover:underline"
                >
                  Create your first skill
                </button>
              </div>
            )}
            {skills.map(skill => (
              <button
                key={skill.id}
                onClick={() => { setSelected(skill); setMode('list'); setDetailTab('details'); }}
                className={`w-full text-left px-4 py-3 border-b border-[var(--border)] hover:bg-[var(--panel-soft)] transition-colors ${selected?.id === skill.id && !showForm ? 'bg-[var(--panel-soft)] border-l-2 border-l-[var(--brand)]' : ''}`}
              >
                <div className="flex items-center gap-2">
                  <span className={`w-2 h-2 rounded-full shrink-0 ${skill.status === 'ACTIVE' ? 'bg-green-400' : 'bg-[var(--muted)]'}`} />
                  <span className="text-sm font-medium text-[var(--foreground)] truncate">{skill.name || 'Untitled'}</span>
                  <span className={`ml-auto text-xs shrink-0 ${skill.status === 'ACTIVE' ? 'text-green-400' : 'text-[var(--muted)]'}`}>
                    {skill.status}
                  </span>
                </div>
                <div className="mt-0.5 text-xs text-[var(--muted)] ml-4 truncate">
                  {skill.compatibility || 'No compatibility info'}
                </div>
              </button>
            ))}
          </div>
        </div>

        {/* Right panel */}
        <div className="flex-1 overflow-hidden">
          {showForm && (
            <SkillForm
              agentId={agentId}
              initial={mode === 'edit' ? selected ?? undefined : undefined}
              onSaved={handleSaved}
              onCancel={() => setMode('list')}
            />
          )}

          {!showForm && !selected && (
            <div className="flex items-center justify-center h-full text-[var(--muted)] text-sm">
              Select a skill or create a new one
            </div>
          )}

          {!showForm && selected && (
            <div className="flex flex-col h-full">
              {/* Detail header */}
              <div className="flex items-start justify-between px-6 py-4 border-b border-[var(--border)]">
                <div>
                  <h2 className="text-lg font-semibold text-[var(--foreground)]">{selected.name}</h2>
                  <div className="flex items-center gap-3 mt-1 text-xs text-[var(--muted)]">
                    {selected.createdAt && (
                      <span>Created {new Date(selected.createdAt).toLocaleDateString('en-US', { month: '2-digit', day: '2-digit', year: '2-digit' })}</span>
                    )}
                    <span className={`font-semibold ${selected.status === 'ACTIVE' ? 'text-green-400' : 'text-[var(--muted)]'}`}>
                      {selected.status}
                    </span>
                  </div>
                </div>
                <div className="flex items-center gap-2">
                  <button
                    onClick={() => setMode('edit')}
                    className="p-2 rounded-lg hover:bg-[var(--panel-soft)] text-[var(--muted)] hover:text-[var(--foreground)]"
                    title="Edit"
                  >
                    ✎
                  </button>
                  <button
                    onClick={() => handleDelete(selected)}
                    className="p-2 rounded-lg hover:bg-red-500/10 text-[var(--muted)] hover:text-red-400"
                    title="Delete"
                  >
                    🗑
                  </button>
                </div>
              </div>

              {/* Detail tabs */}
              <div className="flex gap-6 px-6 pt-3 border-b border-[var(--border)] text-sm">
                {(['details', 'instructions', 'analytics'] as DetailTab[]).map(t => (
                  <button
                    key={t}
                    onClick={() => setDetailTab(t)}
                    className={`pb-2 capitalize font-medium border-b-2 transition-colors ${detailTab === t ? 'border-[var(--brand)] text-[var(--brand)]' : 'border-transparent text-[var(--muted)]'}`}
                  >
                    {t}
                  </button>
                ))}
              </div>

              <div className="flex-1 overflow-y-auto p-6">
                {detailTab === 'details' && (
                  <div className="space-y-6">
                    <div>
                      <label className="text-xs text-[var(--muted)] uppercase tracking-wider">Description</label>
                      <p className="mt-1 text-sm text-[var(--foreground)]">{selected.description || '—'}</p>
                    </div>
                    <div className="grid grid-cols-2 gap-6">
                      <div>
                        <label className="text-xs text-[var(--muted)] uppercase tracking-wider">License</label>
                        <p className="mt-1 text-sm text-[var(--foreground)]">{selected.license || '—'}</p>
                      </div>
                      <div>
                        <label className="text-xs text-[var(--muted)] uppercase tracking-wider">Compatibility</label>
                        <p className="mt-1 text-sm text-[var(--foreground)]">{selected.compatibility || '—'}</p>
                      </div>
                    </div>
                    {selected.allowedTools && selected.allowedTools.length > 0 && (
                      <div>
                        <label className="text-xs text-[var(--muted)] uppercase tracking-wider">Allowed Tools</label>
                        <div className="mt-1 flex flex-wrap gap-1">
                          {selected.allowedTools.map(tool => (
                            <span key={tool} className="px-2 py-0.5 rounded-full bg-[var(--panel-soft)] border border-[var(--border)] text-xs text-[var(--foreground)]">{tool}</span>
                          ))}
                        </div>
                      </div>
                    )}
                    {selected.skillMetadata && Object.keys(selected.skillMetadata).length > 0 && (
                      <div>
                        <label className="text-xs text-[var(--muted)] uppercase tracking-wider">Metadata</label>
                        <p className="mt-1 text-sm text-[var(--foreground)]">
                          {Object.entries(selected.skillMetadata).map(([k, v]) => `${k}: ${v}`).join(' · ')}
                        </p>
                      </div>
                    )}
                    <div className="pt-2">
                      <button
                        onClick={() => handleToggleStatus(selected)}
                        className={`px-4 py-2 rounded-xl text-sm font-medium transition-colors ${selected.status === 'ACTIVE' ? 'bg-[var(--panel-soft)] text-[var(--muted)] hover:bg-red-500/10 hover:text-red-400' : 'bg-green-500/20 text-green-400 hover:bg-green-500/30'}`}
                      >
                        {selected.status === 'ACTIVE' ? 'Deactivate' : 'Activate'}
                      </button>
                    </div>
                  </div>
                )}

                {detailTab === 'instructions' && (
                  <div>
                    <div className="flex items-center gap-2 mb-3">
                      <span className="text-xs text-[var(--muted)] uppercase tracking-wider">SKILL.md Instructions</span>
                    </div>
                    <pre className="w-full h-96 bg-[var(--panel-soft)] border border-[var(--border)] rounded-xl px-4 py-3 text-sm font-mono overflow-auto text-[var(--foreground)] whitespace-pre-wrap">
                      {selected.instructions || '# No instructions yet'}
                    </pre>
                  </div>
                )}

                {detailTab === 'analytics' && (
                  <div className="text-sm text-[var(--muted)] text-center pt-12">
                    Analytics coming soon.
                  </div>
                )}
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
