'use client';

import { useEffect, useState } from 'react';
import { apiFetch } from '@/lib/api';
import { Integration, IntegrationType } from '@/lib/types';

const INTEGRATION_INFO: Record<IntegrationType, { label: string; icon: string; fields: { key: string; label: string; placeholder: string }[] }> = {
  SLACK: { label: 'Slack', icon: '💬', fields: [{ key: 'webhookUrl', label: 'Webhook URL', placeholder: 'https://hooks.slack.com/services/...' }] },
  WHATSAPP: { label: 'WhatsApp', icon: '📱', fields: [{ key: 'phoneNumberId', label: 'Phone Number ID', placeholder: 'WhatsApp Business phone ID' }, { key: 'accessToken', label: 'Access Token', placeholder: 'Meta access token' }] },
  EMAIL: { label: 'Email', icon: '✉️', fields: [{ key: 'smtpHost', label: 'SMTP Host', placeholder: 'smtp.gmail.com' }, { key: 'smtpPort', label: 'Port', placeholder: '587' }, { key: 'username', label: 'Username', placeholder: 'you@gmail.com' }, { key: 'password', label: 'Password', placeholder: '••••••••' }] },
  WEBHOOK: { label: 'Webhook', icon: '🔗', fields: [{ key: 'webhookUrl', label: 'Webhook URL', placeholder: 'https://your-server.com/webhook' }, { key: 'secret', label: 'Secret (optional)', placeholder: 'hmac_secret' }] },
  GOOGLE_DRIVE: { label: 'Google Drive', icon: '📁', fields: [{ key: 'clientId', label: 'Client ID', placeholder: 'OAuth client ID' }, { key: 'clientSecret', label: 'Client Secret', placeholder: 'OAuth client secret' }] },
  NOTION: { label: 'Notion', icon: '📓', fields: [{ key: 'integrationToken', label: 'Integration Token', placeholder: 'secret_...' }] },
  CONFLUENCE: { label: 'Confluence', icon: '📚', fields: [{ key: 'baseUrl', label: 'Base URL', placeholder: 'https://yourorg.atlassian.net' }, { key: 'apiToken', label: 'API Token', placeholder: 'Atlassian API token' }, { key: 'email', label: 'Email', placeholder: 'you@company.com' }] },
  CRM: { label: 'CRM', icon: '👥', fields: [{ key: 'provider', label: 'Provider', placeholder: 'salesforce / hubspot' }, { key: 'apiKey', label: 'API Key', placeholder: 'CRM API key' }] },
};

export default function IntegrationsPage() {
  const [integrations, setIntegrations] = useState<Integration[]>([]);
  const [adding, setAdding] = useState<IntegrationType | null>(null);
  const [intName, setIntName] = useState('');
  const [config, setConfig] = useState<Record<string, string>>({});
  const [error, setError] = useState('');

  async function load() {
    const data = await apiFetch<Integration[]>('/integrations');
    setIntegrations(data);
  }

  function startAdd(type: IntegrationType) {
    setAdding(type);
    setIntName(INTEGRATION_INFO[type].label);
    setConfig({});
    setError('');
  }

  async function create() {
    if (!adding || !intName.trim()) return;
    try {
      await apiFetch<Integration>('/integrations', {
        method: 'POST',
        body: JSON.stringify({ type: adding, name: intName, config }),
      });
      setAdding(null); setIntName(''); setConfig({}); setError('');
      await load();
    } catch (e) { setError(e instanceof Error ? e.message : 'Failed'); }
  }

  async function toggle(id: string) {
    await apiFetch(`/integrations/${id}/enabled`, { method: 'PATCH' });
    await load();
  }

  async function remove(id: string) {
    if (!confirm('Delete this integration?')) return;
    await apiFetch(`/integrations/${id}`, { method: 'DELETE' });
    await load();
  }

  useEffect(() => { void load(); }, []);

  return (
    <div className="space-y-6">
      <h1 className="text-3xl font-semibold">Integrations</h1>
      <p className="text-sm text-[var(--muted)]">Connect your AI platform to external services and trigger automated workflows.</p>

      {/* Available integrations */}
      <section className="rounded-3xl border border-[var(--border)] bg-[var(--panel)] p-6">
        <h2 className="text-xl font-semibold mb-4">Available Integrations</h2>
        <div className="grid grid-cols-2 gap-3 md:grid-cols-4">
          {(Object.keys(INTEGRATION_INFO) as IntegrationType[]).map(type => {
            const info = INTEGRATION_INFO[type];
            const connected = integrations.filter(i => i.type === type);
            return (
              <button key={type} onClick={() => startAdd(type)}
                className="rounded-2xl border border-[var(--border)] bg-[var(--panel-soft)] p-4 text-left hover:border-[var(--brand)] transition">
                <div className="text-2xl mb-2">{info.icon}</div>
                <div className="font-semibold text-sm">{info.label}</div>
                {connected.length > 0 && <div className="mt-1 text-xs text-green-400">{connected.length} connected</div>}
              </button>
            );
          })}
        </div>
      </section>

      {/* Add form */}
      {adding && (
        <section className="rounded-3xl border border-[var(--brand)]/50 bg-[var(--panel)] p-6 space-y-4">
          <div className="flex items-center justify-between">
            <h2 className="text-xl font-semibold">{INTEGRATION_INFO[adding].icon} Configure {INTEGRATION_INFO[adding].label}</h2>
            <button onClick={() => setAdding(null)} className="text-[var(--muted)] hover:text-white">✕</button>
          </div>
          <input className="w-full rounded-2xl border border-slate-300 px-4 py-3" value={intName} onChange={e => setIntName(e.target.value)} placeholder="Integration name" />
          {INTEGRATION_INFO[adding].fields.map(field => (
            <div key={field.key}>
              <label className="mb-1 block text-sm text-[var(--muted)]">{field.label}</label>
              <input type={field.key.toLowerCase().includes('secret') || field.key.toLowerCase().includes('password') || field.key.toLowerCase().includes('token') ? 'password' : 'text'}
                className="w-full rounded-2xl border border-slate-300 px-4 py-3"
                value={config[field.key] ?? ''}
                onChange={e => setConfig({ ...config, [field.key]: e.target.value })}
                placeholder={field.placeholder} />
            </div>
          ))}
          {error && <p className="text-red-300 text-sm">{error}</p>}
          <button onClick={create} className="rounded-2xl bg-[var(--brand)] px-6 py-3 font-semibold text-slate-950">Connect</button>
        </section>
      )}

      {/* Connected integrations */}
      <section className="rounded-3xl border border-[var(--border)] bg-[var(--panel)] p-6">
        <h2 className="text-xl font-semibold mb-4">Connected ({integrations.length})</h2>
        {integrations.length === 0 && <p className="text-[var(--muted)]">No integrations connected yet.</p>}
        <div className="space-y-3">
          {integrations.map(int => {
            const info = INTEGRATION_INFO[int.type];
            return (
              <div key={int.id} className="flex items-center justify-between rounded-2xl border border-[var(--border)] bg-[var(--panel-soft)] px-5 py-4">
                <div className="flex items-center gap-3">
                  <span className="text-xl">{info?.icon}</span>
                  <div>
                    <div className="font-semibold">{int.name}</div>
                    <div className="text-xs text-[var(--muted)]">{info?.label}
                      {int.lastTriggeredAt && ` · Last triggered: ${new Date(int.lastTriggeredAt).toLocaleString()}`}
                    </div>
                  </div>
                </div>
                <div className="flex gap-3">
                  <button onClick={() => toggle(int.id)} className={`rounded-2xl border px-4 py-1.5 text-sm ${int.enabled ? 'border-yellow-600 text-yellow-400' : 'border-green-700 text-green-400'}`}>
                    {int.enabled ? 'Disable' : 'Enable'}
                  </button>
                  <button onClick={() => remove(int.id)} className="rounded-2xl border border-red-800 px-4 py-1.5 text-sm text-red-400">Delete</button>
                </div>
              </div>
            );
          })}
        </div>
      </section>
    </div>
  );
}
