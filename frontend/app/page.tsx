import Link from 'next/link';

export default function HomePage() {
  return (
    <div className="grid gap-6 md:grid-cols-2">
      <section className="rounded-3xl border border-[var(--border)] bg-[var(--panel)] p-8 shadow-2xl">
        <p className="mb-3 text-sm uppercase tracking-[0.3em] text-[var(--brand)]">MVP</p>
        <h1 className="mb-4 text-4xl font-bold">Spring Boot + Next.js in one IntelliJ project</h1>
        <p className="mb-6 text-[var(--muted)]">
          Register, create an agent, upload a PDF/TXT/Markdown file, and chat against your own document knowledge base.
        </p>
        <div className="flex gap-3">
          <Link href="/register" className="rounded-2xl bg-[var(--brand)] px-5 py-3 font-semibold text-slate-950">Start now</Link>
          <Link href="/dashboard" className="rounded-2xl border border-[var(--border)] px-5 py-3 font-semibold">Open dashboard</Link>
        </div>
      </section>
      <section className="rounded-3xl border border-[var(--border)] bg-[var(--panel-soft)] p-8">
        <h2 className="mb-4 text-2xl font-semibold">Included in this scaffold</h2>
        <ul className="space-y-3 text-[var(--muted)]">
          <li>• JWT auth</li>
          <li>• Agent CRUD for MVP</li>
          <li>• Chat session + message persistence</li>
          <li>• PDF/TXT/Markdown upload and chunking</li>
          <li>• Basic knowledge search tool</li>
          <li>• OpenAI adapter with mock mode</li>
        </ul>
      </section>
    </div>
  );
}
