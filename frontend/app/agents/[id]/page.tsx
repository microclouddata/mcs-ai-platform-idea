'use client';

import { ChangeEvent, useEffect, useState } from 'react';
import { useParams } from 'next/navigation';
import Link from 'next/link';
import { apiFetch } from '@/lib/api';
import { Agent, ChatMessage, ChatResponse, DocumentFile } from '@/lib/types';

export default function AgentDetailPage() {
  const params = useParams();
  const agentId = params.id as string;

  const [agent, setAgent] = useState<Agent | null>(null);
  const [isOwner, setIsOwner] = useState(false);
  const [documents, setDocuments] = useState<DocumentFile[]>([]);
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [sessionId, setSessionId] = useState<string>('');
  const [message, setMessage] = useState('');
  const [file, setFile] = useState<File | null>(null);
  const [status, setStatus] = useState('');
  const [saving, setSaving] = useState(false);

  const API_BASE = process.env.NEXT_PUBLIC_API_BASE || 'http://localhost:8080/api';

  async function loadAgent() {
    const data = await apiFetch<Agent>(`/agents/${agentId}`);
    const currentUserId = typeof window !== 'undefined' ? localStorage.getItem('userId') : null;
    setAgent(data);
    setIsOwner(data.userId === currentUserId);
    return data.userId === currentUserId;
  }

  async function loadDocuments() {
    const data = await apiFetch<DocumentFile[]>(`/documents?agentId=${agentId}`);
    setDocuments(data);
  }

  function downloadDocument(doc: DocumentFile) {
    const token = localStorage.getItem('token');
    const url = `${API_BASE}/documents/${doc.id}/download`;
    const a = document.createElement('a');
    a.href = url;
    a.download = doc.fileName;
    fetch(url, { headers: { Authorization: `Bearer ${token}` } })
      .then((res) => res.blob())
      .then((blob) => {
        const objectUrl = URL.createObjectURL(blob);
        a.href = objectUrl;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        URL.revokeObjectURL(objectUrl);
      });
  }

  async function deleteDocument(id: string) {
    if (!confirm('Delete this document and its chunks?')) return;
    setSaving(true);
    try {
      await apiFetch(`/documents/${id}`, { method: 'DELETE' });
      await loadDocuments();
      setStatus('Document deleted');
    } catch (e) {
      setStatus(e instanceof Error ? e.message : 'Delete failed');
    } finally {
      setSaving(false);
    }
  }

  async function sendMessage() {
    if (!message.trim()) return;
    setSaving(true);
    setStatus('Thinking...');
    try {
      const response = await apiFetch<ChatResponse>('/chat', {
        method: 'POST',
        body: JSON.stringify({ agentId, sessionId, message }),
      });
      setSessionId(response.sessionId);
      const history = await apiFetch<ChatMessage[]>(`/chat/history/${response.sessionId}`);
      setMessages(history);
      setMessage('');
      setStatus('Done');
    } catch (e) {
      setStatus(e instanceof Error ? e.message : 'Send failed');
    } finally {
      setSaving(false);
    }
  }

  async function uploadFile() {
    if (!file) return;
    setSaving(true);
    setStatus('Uploading...');
    try {
      const formData = new FormData();
      formData.append('agentId', agentId);
      formData.append('file', file);
      await apiFetch('/documents/upload', {
        method: 'POST',
        body: formData,
      });
      setFile(null);
      (document.getElementById('file-input') as HTMLInputElement).value = '';
      setStatus('Upload success');
      await loadDocuments();
    } catch (e) {
      setStatus(e instanceof Error ? e.message : 'Upload failed');
    } finally {
      setSaving(false);
    }
  }

  useEffect(() => {
    async function init() {
      const owner = await loadAgent();
      if (owner) void loadDocuments();
    }
    void init();
  }, [agentId]);

  const chatSection = (
    <section className="rounded-3xl border border-[var(--border)] bg-[var(--panel)] p-6">
      <h2 className="mb-4 text-2xl font-semibold">Chat</h2>
      <div className="mb-4 h-[480px] overflow-y-auto rounded-3xl border border-[var(--border)] bg-black/20 p-4">
        {messages.length === 0 ? <p className="text-[var(--muted)]">Ask about your uploaded documents.</p> : null}
        <div className="space-y-4">
          {messages.map((item) => (
            <div key={item.id} className="rounded-2xl border border-[var(--border)] bg-[var(--panel-soft)] p-4">
              <div className="mb-2 text-xs font-bold tracking-[0.25em] text-[var(--brand)]">{item.role}</div>
              <div className="whitespace-pre-wrap text-sm leading-6 text-white">{item.content}</div>
            </div>
          ))}
        </div>
      </div>
      <textarea
        value={message}
        onChange={(e) => setMessage(e.target.value)}
        rows={5}
        className="mb-3 w-full rounded-2xl border border-slate-300 px-4 py-3"
        placeholder="Ask a question about your uploaded files"
      />
      <div className="flex items-center justify-between gap-3">
        <p className="text-sm text-[var(--muted)]">{status}</p>
        <button disabled={saving} className="rounded-2xl bg-[var(--brand)] px-5 py-3 font-semibold text-slate-950 disabled:opacity-50" onClick={sendMessage}>
          Send
        </button>
      </div>
    </section>
  );

  if (!agent) return <p className="text-[var(--muted)]">Loading...</p>;

  if (!isOwner) {
    return (
      <div className="mx-auto max-w-3xl space-y-4">
        <div>
          <h1 className="text-2xl font-semibold">{agent.name}</h1>
          <p className="text-sm text-[var(--muted)]">{agent.description}</p>
        </div>
        {chatSection}
      </div>
    );
  }

  return (
    <div className="grid gap-6 lg:grid-cols-[360px_1fr]">
      <aside className="space-y-6">
        <section className="rounded-3xl border border-[var(--border)] bg-[var(--panel)] p-6">
          <div className="mb-2 flex items-center justify-between">
            <h1 className="text-2xl font-semibold">{agent.name}</h1>
            <Link href={`/agents/${agentId}/settings`} className="text-xs text-[var(--brand)] hover:underline">
              Settings
            </Link>
          </div>
          <p className="text-sm text-[var(--muted)]">{agent.description}</p>
        </section>

        <section className="rounded-3xl border border-[var(--border)] bg-[var(--panel)] p-6">
          <h2 className="mb-4 text-xl font-semibold">Documents</h2>
          <input
            id="file-input"
            type="file"
            accept=".pdf,.txt,.md"
            className="hidden"
            onChange={(e: ChangeEvent<HTMLInputElement>) => setFile(e.target.files?.[0] ?? null)}
          />
          {!file ? (
            <button
              onClick={() => document.getElementById('file-input')?.click()}
              className="flex w-full items-center justify-center gap-2 rounded-2xl border-2 border-dashed border-[var(--border)] py-6 text-sm text-[var(--muted)] transition hover:border-[var(--brand)] hover:text-white"
            >
              <span className="text-lg">+</span> Click to select a file
            </button>
          ) : (
            <div className="flex items-center justify-between rounded-2xl border border-[var(--border)] bg-black/20 px-4 py-3 text-sm">
              <div>
                <div className="font-medium text-white">{file.name}</div>
                <div className="text-xs text-[var(--muted)]">{(file.size / 1024).toFixed(1)} KB</div>
              </div>
              <div className="flex gap-2">
                <button
                  onClick={() => setFile(null)}
                  className="rounded-xl px-3 py-1.5 text-xs text-[var(--muted)] hover:text-white"
                >
                  Remove
                </button>
                <button
                  disabled={saving}
                  onClick={uploadFile}
                  className="rounded-xl bg-[var(--brand)] px-3 py-1.5 text-xs font-semibold text-slate-950 disabled:opacity-50"
                >
                  {saving ? 'Uploading…' : 'Add'}
                </button>
              </div>
            </div>
          )}
          <div className="mt-4 space-y-2 text-sm text-[var(--muted)]">
            {documents.map((doc) => (
              <div key={doc.id} className="rounded-2xl border border-[var(--border)] px-3 py-2">
                <div className="font-medium text-white">{doc.fileName}</div>
                <div className="mb-1">{doc.status} · {doc.size ? `${(doc.size / 1024).toFixed(1)} KB` : ''}</div>
                <div className="flex gap-3">
                  <button onClick={() => downloadDocument(doc)} className="text-xs text-[var(--brand)] hover:underline">
                    Download
                  </button>
                  <button onClick={() => deleteDocument(doc.id)} disabled={saving} className="text-xs text-red-400 hover:text-red-300 disabled:opacity-50">
                    Delete
                  </button>
                </div>
              </div>
            ))}
            {documents.length === 0 ? <p>No documents yet.</p> : null}
          </div>
        </section>
      </aside>

      {chatSection}
    </div>
  );
}
