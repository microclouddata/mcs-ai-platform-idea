import Link from 'next/link';

function NetworkGraph() {
  const nodes = [
    { x: 80, y: 60 }, { x: 160, y: 25 }, { x: 250, y: 75 }, { x: 330, y: 15 },
    { x: 40, y: 145 }, { x: 130, y: 160 }, { x: 210, y: 125 }, { x: 295, y: 155 },
    { x: 60, y: 230 }, { x: 155, y: 248 }, { x: 245, y: 205 }, { x: 350, y: 115 },
    { x: 20, y: 310 }, { x: 105, y: 308 }, { x: 190, y: 328 }, { x: 310, y: 265 },
  ];
  const edges = [
    [0,1],[1,2],[2,3],[0,4],[1,5],[2,6],[3,7],[4,5],[5,6],[6,7],
    [4,8],[5,9],[6,10],[7,11],[8,9],[9,10],[10,11],[8,12],[9,13],
    [10,14],[11,15],[12,13],[13,14],[14,15],[3,11],[2,10],[1,6],
  ];
  return (
    <svg width="380" height="350" viewBox="0 0 380 350" fill="none" xmlns="http://www.w3.org/2000/svg">
      {edges.map(([a, b], i) => (
        <line
          key={i}
          x1={nodes[a].x} y1={nodes[a].y}
          x2={nodes[b].x} y2={nodes[b].y}
          stroke="#3b82f6" strokeWidth="0.9" strokeOpacity="0.25"
        />
      ))}
      {nodes.map((node, i) => (
        <circle key={i} cx={node.x} cy={node.y} r="3.5" fill="#3b82f6" fillOpacity="0.55" />
      ))}
    </svg>
  );
}

export default function HomePage() {
  return (
    <div className="relative min-h-[calc(100vh-73px)] overflow-hidden">
      {/* Network graph decoration */}
      <div className="absolute -top-10 -left-10 pointer-events-none select-none">
        <NetworkGraph />
      </div>

      {/* Hero content */}
      <div className="relative z-10 flex flex-col items-start justify-center px-4 pt-20 pb-16 max-w-2xl">
        {/* Badge */}
        <div className="mb-8 flex items-center gap-2 rounded-full border border-blue-200 bg-blue-50 px-4 py-2 text-xs font-semibold tracking-widest text-blue-700 uppercase">
          <span className="h-1.5 w-1.5 rounded-full bg-green-500 shrink-0"></span>
          Next-Gen AI Platform — Now Available
        </div>

        {/* Heading */}
        <h1 className="mb-6 text-5xl font-extrabold leading-tight text-[var(--text)] md:text-6xl">
          Building Intelligent<br />Systems Powered by<br />
          <span className="text-[var(--brand)]">AI</span>
        </h1>

        {/* Description */}
        <p className="mb-8 text-lg text-[var(--muted)] leading-relaxed max-w-xl">
          MCS AI Platform delivers enterprise-grade AI solutions that transform data into decisions,
          automate complexity, and unlock new dimensions of business performance.
        </p>

        {/* CTAs */}
        <div className="mb-16 flex items-center gap-6">
          <Link
            href="/register"
            className="rounded-full bg-[var(--brand)] px-7 py-3.5 font-semibold text-white hover:bg-blue-700 transition-colors shadow-sm"
          >
            Explore Services
          </Link>
          <Link
            href="/dashboard"
            className="text-sm font-semibold text-[var(--text)] hover:text-[var(--brand)] transition-colors"
          >
            View Case Studies →
          </Link>
        </div>

        {/* Stats */}
        <div className="flex flex-wrap gap-12">
          <div>
            <p className="text-2xl font-bold text-[var(--text)]">500+</p>
            <p className="mt-0.5 text-xs font-medium uppercase tracking-widest text-[var(--muted)]">Models Deployed</p>
          </div>
          <div>
            <p className="text-2xl font-bold text-[var(--text)]">99.7%</p>
            <p className="mt-0.5 text-xs font-medium uppercase tracking-widest text-[var(--muted)]">Uptime SLA</p>
          </div>
          <div>
            <p className="text-2xl font-bold text-[var(--text)]">120+</p>
            <p className="mt-0.5 text-xs font-medium uppercase tracking-widest text-[var(--muted)]">Enterprise Clients</p>
          </div>
          <div>
            <p className="text-2xl font-bold text-[var(--text)]">12</p>
            <p className="mt-0.5 text-xs font-medium uppercase tracking-widest text-[var(--muted)]">Countries Served</p>
          </div>
        </div>
      </div>
    </div>
  );
}
