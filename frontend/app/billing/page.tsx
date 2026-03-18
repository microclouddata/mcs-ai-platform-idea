'use client';

import { useEffect, useState } from 'react';
import { apiFetch } from '@/lib/api';
import { Subscription, PlanType } from '@/lib/types';

const PLANS: { type: PlanType; label: string; price: string; color: string; features: string[] }[] = [
  {
    type: 'FREE',
    label: 'Free',
    price: '$0/mo',
    color: 'border-slate-600',
    features: ['5 agents', '100 requests/day', '50 documents', 'Community support'],
  },
  {
    type: 'PRO',
    label: 'Pro',
    price: '$29/mo',
    color: 'border-blue-500',
    features: ['50 agents', '5,000 requests/day', '1,000 documents', 'Priority support', 'API access'],
  },
  {
    type: 'ENTERPRISE',
    label: 'Enterprise',
    price: '$149/mo',
    color: 'border-purple-500',
    features: ['500 agents', '100,000 requests/day', '10,000 documents', 'Dedicated support', 'SSO / SAML', 'SLA guarantee'],
  },
];

export default function BillingPage() {
  const [subscription, setSubscription] = useState<Subscription | null>(null);
  const [upgrading, setUpgrading] = useState(false);
  const [message, setMessage] = useState('');

  async function load() {
    const data = await apiFetch<Subscription>('/billing/subscription');
    setSubscription(data);
  }

  async function upgrade(plan: PlanType) {
    if (!confirm(`Upgrade to ${plan} plan?`)) return;
    setUpgrading(true);
    try {
      const data = await apiFetch<Subscription>('/billing/subscription/upgrade', {
        method: 'POST',
        body: JSON.stringify({ plan }),
      });
      setSubscription(data);
      setMessage(`Successfully upgraded to ${plan}`);
    } catch (e) {
      setMessage(e instanceof Error ? e.message : 'Failed');
    } finally {
      setUpgrading(false);
    }
  }

  useEffect(() => { void load(); }, []);

  return (
    <div className="space-y-6">
      <h1 className="text-3xl font-semibold">Billing & Subscription</h1>
      {message && <p className="text-green-400 text-sm">{message}</p>}

      {subscription && (
        <section className="rounded-3xl border border-[var(--border)] bg-[var(--panel)] p-6">
          <div className="flex items-center justify-between">
            <div>
              <div className="text-lg font-semibold">Current Plan</div>
              <div className="mt-1 flex items-center gap-3">
                <span className={`rounded-full px-4 py-1 text-sm font-bold ${subscription.plan === 'ENTERPRISE' ? 'bg-purple-500/20 text-purple-300' : subscription.plan === 'PRO' ? 'bg-blue-500/20 text-blue-300' : 'bg-slate-500/20 text-slate-300'}`}>
                  {subscription.plan}
                </span>
                <span className={`text-sm ${subscription.status === 'ACTIVE' ? 'text-green-400' : 'text-red-400'}`}>{subscription.status}</span>
              </div>
            </div>
            {subscription.periodEnd && (
              <div className="text-right text-sm text-[var(--muted)]">
                <div>Renews</div>
                <div>{new Date(subscription.periodEnd).toLocaleDateString()}</div>
              </div>
            )}
          </div>
        </section>
      )}

      <div className="grid gap-6 md:grid-cols-3">
        {PLANS.map(plan => {
          const isCurrent = subscription?.plan === plan.type;
          return (
            <section key={plan.type} className={`rounded-3xl border-2 ${isCurrent ? plan.color : 'border-[var(--border)]'} bg-[var(--panel)] p-6 flex flex-col`}>
              <div className="flex items-start justify-between">
                <div>
                  <h2 className="text-xl font-bold">{plan.label}</h2>
                  <div className="text-2xl font-semibold mt-1">{plan.price}</div>
                </div>
                {isCurrent && <span className="rounded-full bg-[var(--brand)]/20 px-3 py-1 text-xs text-[var(--brand)] font-semibold">Current</span>}
              </div>
              <ul className="mt-4 space-y-2 flex-1">
                {plan.features.map(f => (
                  <li key={f} className="flex items-center gap-2 text-sm text-[var(--muted)]">
                    <span className="text-green-400">✓</span> {f}
                  </li>
                ))}
              </ul>
              <button
                disabled={isCurrent || upgrading}
                onClick={() => upgrade(plan.type)}
                className={`mt-6 w-full rounded-2xl py-3 text-sm font-semibold transition ${isCurrent ? 'bg-[var(--panel-soft)] text-[var(--muted)] cursor-default' : 'bg-[var(--brand)] text-slate-950 hover:opacity-90'} disabled:opacity-50`}
              >
                {isCurrent ? 'Current Plan' : `Upgrade to ${plan.label}`}
              </button>
            </section>
          );
        })}
      </div>
    </div>
  );
}
