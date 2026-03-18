'use client';

import { useEffect, useState } from 'react';
import { apiFetch } from '@/lib/api';
import { MarketplaceItem, MarketplaceItemType } from '@/lib/types';

const TYPE_ICONS: Record<MarketplaceItemType, string> = { AGENT: '🤖', TEMPLATE: '📝', WORKFLOW: '⚡' };
const TYPE_COLOR: Record<MarketplaceItemType, string> = {
  AGENT: 'bg-blue-500/20 text-blue-300',
  TEMPLATE: 'bg-purple-500/20 text-purple-300',
  WORKFLOW: 'bg-[var(--brand)]/20 text-[var(--brand)]',
};

export default function MarketplacePage() {
  const [items, setItems] = useState<MarketplaceItem[]>([]);
  const [myItems, setMyItems] = useState<MarketplaceItem[]>([]);
  const [tab, setTab] = useState<'browse' | 'publish' | 'my'>('browse');
  const [typeFilter, setTypeFilter] = useState('');
  const [search, setSearch] = useState('');
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [type, setType] = useState<MarketplaceItemType>('AGENT');
  const [category, setCategory] = useState('');
  const [tags, setTags] = useState('');
  const [error, setError] = useState('');
  const [cloned, setCloned] = useState('');

  async function loadBrowse() {
    const params = new URLSearchParams();
    if (typeFilter) params.set('type', typeFilter);
    if (search) params.set('search', search);
    const data = await apiFetch<MarketplaceItem[]>(`/marketplace?${params}`);
    setItems(data);
  }

  async function loadMy() {
    const data = await apiFetch<MarketplaceItem[]>('/marketplace/my');
    setMyItems(data);
  }

  async function publish() {
    if (!title.trim()) return;
    try {
      await apiFetch<MarketplaceItem>('/marketplace', {
        method: 'POST',
        body: JSON.stringify({ type, title, description, category, tags: tags.split(',').map(t => t.trim()).filter(Boolean), config: {} }),
      });
      setTitle(''); setDescription(''); setCategory(''); setTags(''); setError('');
      setTab('my'); loadMy();
    } catch (e) { setError(e instanceof Error ? e.message : 'Failed'); }
  }

  async function clone(itemId: string) {
    try {
      const item = await apiFetch<MarketplaceItem>(`/marketplace/${itemId}/clone`, { method: 'POST' });
      setCloned(`Cloned "${item.title}" — check Agents/Templates/Workflows to use it.`);
    } catch (e) { setError(e instanceof Error ? e.message : 'Failed'); }
  }

  async function unpublish(itemId: string) {
    if (!confirm('Remove from marketplace?')) return;
    await apiFetch(`/marketplace/${itemId}`, { method: 'DELETE' });
    loadMy();
  }

  useEffect(() => { void loadBrowse(); }, [typeFilter, search]);
  useEffect(() => { if (tab === 'my') loadMy(); }, [tab]);

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-3xl font-semibold">Marketplace</h1>
        <div className="flex rounded-2xl border border-[var(--border)] overflow-hidden text-sm">
          {(['browse', 'publish', 'my'] as const).map(t => (
            <button key={t} onClick={() => setTab(t)} className={`px-5 py-2.5 font-medium capitalize transition ${tab === t ? 'bg-[var(--brand)] text-slate-950' : 'text-[var(--muted)] hover:text-white'}`}>
              {t === 'my' ? 'My Items' : t}
            </button>
          ))}
        </div>
      </div>

      {error && <p className="text-red-300 text-sm">{error}</p>}
      {cloned && <p className="text-green-400 text-sm">{cloned}</p>}

      {tab === 'browse' && (
        <>
          <div className="flex gap-3">
            <input className="flex-1 rounded-2xl border border-slate-300 px-4 py-2.5 text-sm" value={search} onChange={e => setSearch(e.target.value)} placeholder="Search marketplace..." />
            <select className="rounded-2xl border border-slate-300 px-4 py-2.5 text-sm bg-transparent" value={typeFilter} onChange={e => setTypeFilter(e.target.value)}>
              <option value="">All types</option>
              <option value="AGENT">Agents</option>
              <option value="TEMPLATE">Templates</option>
              <option value="WORKFLOW">Workflows</option>
            </select>
          </div>

          <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
            {items.map(item => (
              <div key={item.id} className="rounded-3xl border border-[var(--border)] bg-[var(--panel)] p-5 flex flex-col">
                <div className="flex items-start justify-between mb-3">
                  <span className={`rounded-full px-3 py-1 text-xs font-medium ${TYPE_COLOR[item.type]}`}>
                    {TYPE_ICONS[item.type]} {item.type}
                  </span>
                  <span className="text-xs text-[var(--muted)]">{item.downloads} downloads</span>
                </div>
                <h3 className="font-semibold">{item.title}</h3>
                <p className="mt-1 text-sm text-[var(--muted)] flex-1">{item.description}</p>
                <div className="mt-2 text-xs text-[var(--muted)]">by {item.authorName}</div>
                {item.tags && item.tags.length > 0 && (
                  <div className="mt-2 flex flex-wrap gap-1">
                    {item.tags.map(t => <span key={t} className="rounded-full bg-[var(--border)] px-2 py-0.5 text-xs text-[var(--muted)]">{t}</span>)}
                  </div>
                )}
                <button onClick={() => clone(item.id)} className="mt-4 w-full rounded-2xl border border-[var(--brand)] py-2 text-sm text-[var(--brand)] hover:bg-[var(--brand)]/10">
                  Clone to my workspace
                </button>
              </div>
            ))}
            {items.length === 0 && <p className="text-[var(--muted)] col-span-3">No items found.</p>}
          </div>
        </>
      )}

      {tab === 'publish' && (
        <section className="rounded-3xl border border-[var(--border)] bg-[var(--panel)] p-6 space-y-4 max-w-2xl">
          <h2 className="text-xl font-semibold">Publish to Marketplace</h2>
          <div className="grid gap-3">
            <select className="rounded-2xl border border-slate-300 px-4 py-3 bg-transparent" value={type} onChange={e => setType(e.target.value as MarketplaceItemType)}>
              <option value="AGENT">Agent</option>
              <option value="TEMPLATE">Prompt Template</option>
              <option value="WORKFLOW">Workflow</option>
            </select>
            <input className="rounded-2xl border border-slate-300 px-4 py-3" value={title} onChange={e => setTitle(e.target.value)} placeholder="Title" />
            <textarea rows={3} className="rounded-2xl border border-slate-300 px-4 py-3 text-sm" value={description} onChange={e => setDescription(e.target.value)} placeholder="Description" />
            <input className="rounded-2xl border border-slate-300 px-4 py-3" value={category} onChange={e => setCategory(e.target.value)} placeholder="Category (e.g. Productivity, Customer Service)" />
            <input className="rounded-2xl border border-slate-300 px-4 py-3" value={tags} onChange={e => setTags(e.target.value)} placeholder="Tags (comma-separated)" />
          </div>
          <button onClick={publish} className="rounded-2xl bg-[var(--brand)] px-6 py-3 font-semibold text-slate-950">Publish</button>
        </section>
      )}

      {tab === 'my' && (
        <div className="space-y-3">
          {myItems.length === 0 && <p className="text-[var(--muted)]">You haven't published anything yet.</p>}
          {myItems.map(item => (
            <div key={item.id} className="flex items-center justify-between rounded-2xl border border-[var(--border)] bg-[var(--panel-soft)] px-5 py-4">
              <div>
                <div className="font-semibold">{item.title}</div>
                <div className="text-xs text-[var(--muted)] mt-0.5">{TYPE_ICONS[item.type]} {item.type} · {item.downloads} downloads · {item.published ? 'Published' : 'Draft'}</div>
              </div>
              <button onClick={() => unpublish(item.id)} className="text-xs text-red-400 hover:text-red-300">Remove</button>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
