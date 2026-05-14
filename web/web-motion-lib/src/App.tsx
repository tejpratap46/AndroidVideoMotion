const featureCards = [
  {
    title: 'MotionConfig',
    description: 'Define render presets, output ratio, duration and quality profiles once and reuse them for every project.',
    snippet: `import { MotionConfig } from 'motionlib';\n\nconst config = new MotionConfig({\n  width: 1080,\n  height: 1920,\n  fps: 30,\n  durationMs: 12000\n});`
  },
  {
    title: 'MotionView',
    description: 'Compose layered visuals, text and media with timeline-aware declarative views.',
    snippet: `import { MotionView } from 'motionlib/view';\n\n<MotionView\n  id="title-layer"\n  startMs={600}\n  endMs={6200}\n  style={{ x: 48, y: 140 }}\n/>`
  },
  {
    title: 'MotionEffect',
    description: 'Attach transition and visual effects such as blur, glitch, color grading and keyframe curves.',
    snippet: `import { MotionEffect } from 'motionlib/effects';\n\nMotionEffect.fadeIn('title-layer', {\n  durationMs: 450,\n  easing: 'easeOutCubic'\n});`
  },
  {
    title: 'MotionPlugin',
    description: 'Extend the runtime with custom render nodes, encoders and AI pipelines as reusable modules.',
    snippet: `import { MotionPlugin } from 'motionlib/plugin';\n\nMotionPlugin.register({\n  id: 'lower-third-v1',\n  setup(ctx) {\n    ctx.registerTemplate('lower-third');\n  }\n});`
  },
  {
    title: 'MotionAudio',
    description: 'Sync narration, music and sound effects with ducking, waveform analysis and beat-aware markers.',
    snippet: `import { MotionAudio } from 'motionlib/audio';\n\nconst audio = new MotionAudio('narration.mp3');\naudio.duck('bgm-track', { fromDb: -4, toDb: -14 });`
  }
];

const integrationFeatures = [
  'Encoders: Android Native Media implementation, FFMPEG, and JCodec',
  'AI capability with PyTorch and TensorFlow inference pipelines',
  'Project management and sync workflows with MotionStore',
  'SDUI JSON serialization to recreate projects deterministically',
  'Prebuild templates for rapid video generation (upcoming)'
];

function CodeBlock({ code }: { code: string }) {
  return (
    <pre className="mt-4 overflow-x-auto rounded-lg border border-cyan-400/20 bg-slate-900 p-4 text-xs text-cyan-200">
      <code>{code}</code>
    </pre>
  );
}

export default function App() {
  return (
    <main className="min-h-screen bg-slate-950 text-slate-100">
      <section className="mx-auto max-w-6xl px-6 py-14">
        <p className="mb-3 inline-flex rounded-full border border-cyan-400/40 px-3 py-1 text-xs uppercase tracking-[0.2em] text-cyan-300">
          web-motion-lib
        </p>
        <h1 className="text-4xl font-bold leading-tight md:text-5xl">MotionLib Feature Showcase</h1>
        <p className="mt-4 max-w-3xl text-slate-300">
          A React + Vite + Tailwind web app that demonstrates how to build advanced motion video workflows using
          MotionConfig, MotionView, MotionEffect, MotionPlugin and MotionAudio.
        </p>

        <div className="mt-10 grid gap-4 rounded-xl border border-slate-800 bg-slate-900/60 p-6 md:grid-cols-2">
          {integrationFeatures.map((item) => (
            <div key={item} className="rounded-lg border border-slate-800 bg-slate-950/60 p-4 text-sm text-slate-200">
              {item}
            </div>
          ))}
        </div>
      </section>

      <section className="mx-auto grid max-w-6xl gap-5 px-6 pb-16 md:grid-cols-2">
        {featureCards.map((feature) => (
          <article key={feature.title} className="rounded-xl border border-slate-800 bg-slate-900/70 p-6">
            <h2 className="text-xl font-semibold text-cyan-200">{feature.title}</h2>
            <p className="mt-2 text-sm text-slate-300">{feature.description}</p>
            <CodeBlock code={feature.snippet} />
          </article>
        ))}
      </section>

      <section className="mx-auto max-w-6xl px-6 pb-20">
        <div className="rounded-xl border border-amber-400/40 bg-amber-500/10 p-6 text-amber-100">
          <h3 className="text-lg font-semibold">Upcoming: Prebuild Templates</h3>
          <p className="mt-2 text-sm text-amber-50/90">
            Template packs will allow one-click generation for reels, shorts, lyric cards and social promos by combining
            SDUI JSON + MotionStore assets + preferred encoder target.
          </p>
        </div>
      </section>
    </main>
  );
}
