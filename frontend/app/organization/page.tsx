'use client';

import { useEffect, useState } from 'react';
import { apiFetch } from '@/lib/api';
import { Organization, OrgMembership, OrgRole } from '@/lib/types';

export default function OrganizationPage() {
  const [orgs, setOrgs] = useState<Organization[]>([]);
  const [selected, setSelected] = useState<Organization | null>(null);
  const [members, setMembers] = useState<OrgMembership[]>([]);
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [inviteEmail, setInviteEmail] = useState('');
  const [inviteRole, setInviteRole] = useState<OrgRole>('MEMBER');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  async function loadOrgs() {
    const data = await apiFetch<Organization[]>('/organizations');
    setOrgs(data);
    if (data.length > 0 && !selected) {
      setSelected(data[0]);
      loadMembers(data[0].id);
    }
  }

  async function loadMembers(orgId: string) {
    const data = await apiFetch<OrgMembership[]>(`/organizations/${orgId}/members`);
    setMembers(data);
  }

  async function createOrg() {
    if (!name.trim()) return;
    try {
      const org = await apiFetch<Organization>('/organizations', {
        method: 'POST',
        body: JSON.stringify({ name, description }),
      });
      setName(''); setDescription(''); setError('');
      setSuccess('Organization created');
      await loadOrgs();
      setSelected(org);
      loadMembers(org.id);
    } catch (e) { setError(e instanceof Error ? e.message : 'Failed'); }
  }

  async function invite() {
    if (!selected || !inviteEmail.trim()) return;
    try {
      await apiFetch<OrgMembership>(`/organizations/${selected.id}/members`, {
        method: 'POST',
        body: JSON.stringify({ email: inviteEmail, role: inviteRole }),
      });
      setInviteEmail(''); setError(''); setSuccess('Member invited');
      loadMembers(selected.id);
    } catch (e) { setError(e instanceof Error ? e.message : 'Failed'); }
  }

  async function removeMember(userId: string) {
    if (!selected || !confirm('Remove this member?')) return;
    await apiFetch(`/organizations/${selected.id}/members/${userId}`, { method: 'DELETE' });
    loadMembers(selected.id);
  }

  useEffect(() => { void loadOrgs(); }, []);

  const ROLE_COLOR: Record<OrgRole, string> = {
    OWNER: 'text-yellow-400',
    ADMIN: 'text-blue-400',
    MEMBER: 'text-[var(--muted)]',
  };

  return (
    <div className="space-y-6">
      <h1 className="text-3xl font-semibold">Organizations</h1>
      {error && <p className="text-red-600 text-sm">{error}</p>}
      {success && <p className="text-green-400 text-sm">{success}</p>}

      <div className="grid gap-6 lg:grid-cols-[280px_1fr]">
        {/* Sidebar */}
        <div className="space-y-4">
          <section className="rounded-3xl border border-[var(--border)] bg-[var(--panel)] p-5 space-y-3">
            <h2 className="text-lg font-semibold">Create Organization</h2>
            <input className="w-full rounded-2xl border border-slate-300 px-4 py-2.5 text-sm" value={name} onChange={e => setName(e.target.value)} placeholder="Name" />
            <input className="w-full rounded-2xl border border-slate-300 px-4 py-2.5 text-sm" value={description} onChange={e => setDescription(e.target.value)} placeholder="Description (optional)" />
            <button onClick={createOrg} className="w-full rounded-2xl bg-[var(--brand)] py-2.5 text-sm font-semibold text-white">Create</button>
          </section>

          <section className="rounded-3xl border border-[var(--border)] bg-[var(--panel)] p-5 space-y-2">
            <h2 className="text-lg font-semibold mb-3">Your Organizations</h2>
            {orgs.length === 0 && <p className="text-[var(--muted)] text-sm">No organizations yet.</p>}
            {orgs.map(org => (
              <button key={org.id} onClick={() => { setSelected(org); loadMembers(org.id); }}
                className={`w-full text-left rounded-2xl border px-4 py-3 transition ${selected?.id === org.id ? 'border-[var(--brand)]' : 'border-[var(--border)]'} bg-[var(--panel-soft)]`}>
                <div className="font-semibold text-sm">{org.name}</div>
                <div className="text-xs text-[var(--muted)]">{org.plan} plan</div>
              </button>
            ))}
          </section>
        </div>

        {/* Main panel */}
        {selected && (
          <div className="space-y-6">
            <section className="rounded-3xl border border-[var(--border)] bg-[var(--panel)] p-6">
              <div className="flex items-start justify-between mb-4">
                <div>
                  <h2 className="text-2xl font-semibold">{selected.name}</h2>
                  {selected.description && <p className="text-sm text-[var(--muted)] mt-1">{selected.description}</p>}
                </div>
                <span className={`rounded-full px-3 py-1 text-xs font-semibold ${selected.plan === 'ENTERPRISE' ? 'bg-purple-500/20 text-purple-300' : selected.plan === 'PRO' ? 'bg-blue-500/20 text-blue-300' : 'bg-slate-500/20 text-slate-300'}`}>
                  {selected.plan}
                </span>
              </div>

              <h3 className="text-lg font-semibold mb-3">Invite Member</h3>
              <div className="flex gap-3">
                <input className="flex-1 rounded-2xl border border-slate-300 px-4 py-2.5 text-sm" value={inviteEmail} onChange={e => setInviteEmail(e.target.value)} placeholder="Email address" />
                <select className="rounded-2xl border border-slate-300 px-4 py-2.5 text-sm bg-transparent" value={inviteRole} onChange={e => setInviteRole(e.target.value as OrgRole)}>
                  <option value="MEMBER">Member</option>
                  <option value="ADMIN">Admin</option>
                </select>
                <button onClick={invite} className="rounded-2xl bg-[var(--brand)] px-5 py-2.5 text-sm font-semibold text-white">Invite</button>
              </div>
            </section>

            <section className="rounded-3xl border border-[var(--border)] bg-[var(--panel)] p-6">
              <h3 className="text-lg font-semibold mb-4">Members ({members.length})</h3>
              <div className="space-y-2">
                {members.map(m => (
                  <div key={m.id} className="flex items-center justify-between rounded-2xl border border-[var(--border)] bg-[var(--panel-soft)] px-4 py-3">
                    <div>
                      <div className="font-medium text-sm">{m.userName}</div>
                      <div className="text-xs text-[var(--muted)]">{m.userEmail}</div>
                    </div>
                    <div className="flex items-center gap-3">
                      <span className={`text-xs font-semibold ${ROLE_COLOR[m.role]}`}>{m.role}</span>
                      {m.role !== 'OWNER' && (
                        <button onClick={() => removeMember(m.userId)} className="text-xs text-red-400 hover:text-red-600">Remove</button>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            </section>
          </div>
        )}
      </div>
    </div>
  );
}
