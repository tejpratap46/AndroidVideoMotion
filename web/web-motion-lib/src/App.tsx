import { useEffect, useId, useState } from 'react';

const featureCards = [
  {
    title: 'MotionConfig class + usage',
    description: 'Core Kotlin config model from modules/core and how it is instantiated for a render pipeline.',
    snippet: `// modules/core/src/main/java/.../MotionConfig.kt
package com.tejpratapsingh.motionlib.core

data class MotionConfig(
    val aspectRatio: Pair<Int, Int> = Pair(9, 16),
    val fps: Int = 24,
    val outputQuality: Int = 100,
)

// usage
val config = MotionConfig(
    aspectRatio = Pair(9, 16),
    fps = 30,
    outputQuality = 90,
)`
  },
  {
    title: 'MotionAudio class + usage',
    description: 'Audio timeline model from modules/core and how to attach audio clips to a project.',
    snippet: `// modules/core/src/main/java/.../MotionAudio.kt
package com.tejpratapsingh.motionlib.core

data class MotionAudio(
    val audioUri: Uri,
    var startFrame: Int = 0,
    var endFrame: Int = 0,
    var delayFrame: Int = 0,
)

// usage
val narration = MotionAudio(
    audioUri = Uri.parse("file:///storage/emulated/0/Download/narration.mp3"),
    startFrame = 0,
    endFrame = 360,
)
project.audios.add(narration)`
  },
  {
    title: 'MotionVideoProducer usage',
    description: 'Concrete Kotlin pipeline assembly from modules/motionlib and app module sample usage.',
    snippet: `val motionVideo = MotionVideoProducer
  .with(
    context = applicationContext,
    videoProducerAdapter = FfmpegVideoProducerAdapter(),
  )
  .addMotionViewToSequence(rootContainer)

// later in PreviewActivity/Worker
override fun getMotionVideo(): MotionVideoProducer = motionVideo`
  },
  {
    title: 'SDUIMotionVideoProducerFactory class',
    description: 'Kotlin class from modules/sdui for turning MotionProject/JSON into a MotionVideoProducer.',
    snippet: `class SDUIMotionVideoProducerFactory(
  private val context: Context,
  private val motionSduiAdapter: MotionSduiAdapter,
) {
  fun createFromProject(
    project: MotionProject,
    onViewsCreated: (List<MotionView>) -> Unit = {},
  ): MotionVideoProducer = createFromSdui(project.sdui, onViewsCreated)
}

// usage
val factory = SDUIMotionVideoProducerFactory(applicationContext, adapter)
val producer = factory.createFromProject(motionProject)`
  },
  {
    title: 'Full video producer flow (MultiLyrics)',
    description: 'Important bits to build a full renderable video producer from project metadata, timeline views and SDUI persistence.',
    snippet: `fun getMultiLyricsVideoProducer(context: Context, project: MotionProject): MotionVideoProducer {
  // 1) Parse lyrics metadata to frame-timed entries
  val lyrics = project.metadata["lyrics"].asJsonArray.map { ... }

  // 2) Define output profile and make it current
  setCurrentConfig(MotionConfig(aspectRatio = VideoAspectRatio.Ratio9x16_480, fps = 24))

  // 3) Build base producer with FFmpeg adapter + container
  val producer = MotionVideoProducer
    .with(context = context, videoProducerAdapter = FfmpegVideoProducerAdapter())
    .addMotionViewToSequence(MultiLyricsContainer(...))

  // 4) Add each lyric segment as timed PopUpTextView
  lyrics.zipWithNext().forEach { (current, next) ->
    producer.addMotionViewToSequence(PopUpTextView(
      context = context,
      text = current.text,
      startFrame = current.frame,
      endFrame = next.frame,
    ))
  }

  // 5) Serialize timeline -> SDUI and persist MotionProject
  val sdui = createMotionSDUIJson(
    views = collectMotionViews(producer),
    audios = producer.motionAudio,
    plugins = producer.motionComposerView.plugins,
    config = provideCurrentConfig(),
  )
  app.motionStoreDao.upsert(project.copy(sdui = sdui))

  // 6) Return producer ready for preview/render/export
  return producer
}`
  }
];

const kotlinKeywords = new Set([
  'package', 'import', 'class', 'data', 'val', 'var', 'fun', 'private', 'override', 'return', 'object'
]);

function KotlinCodeBlock({ code }: { code: string }) {
  const lines = code.split('\n');

  return (
    <pre className="mt-4 overflow-x-auto rounded-lg border border-cyan-400/20 bg-slate-900 p-4 text-xs leading-6 text-slate-100">
      <code>
        {lines.map((line, index) => {
          const parts = line.split(/(\/\/.*$|\"[^\"]*\"|\b[\w.]+\b)/g).filter(Boolean);
          return (
            <div key={`${line}-${index}`}>
              {parts.map((part, partIndex) => {
                if (part.startsWith('//')) return <span key={partIndex} className="text-slate-500">{part}</span>;
                if (part.startsWith('"') && part.endsWith('"')) return <span key={partIndex} className="text-emerald-300">{part}</span>;
                if (kotlinKeywords.has(part)) return <span key={partIndex} className="text-fuchsia-300">{part}</span>;
                if (/^[A-Z][A-Za-z0-9_]*$/.test(part)) return <span key={partIndex} className="text-cyan-300">{part}</span>;
                if (/^\d+$/.test(part)) return <span key={partIndex} className="text-amber-300">{part}</span>;
                return <span key={partIndex}>{part}</span>;
              })}
            </div>
          );
        })}
      </code>
    </pre>
  );
}

const sequenceDiagram = `sequenceDiagram
  participant Client as LyricsMotionWorker/App
  participant Store as MotionStoreDao
  participant Builder as MultiLyricsVideoProducer
  participant Producer as MotionVideoProducer
  participant Renderer as FFmpeg Adapter

  Client->>Store: load MotionProject(projectId)
  Store-->>Client: MotionProject(metadata + sdui)
  Client->>Builder: getMultiLyricsVideoProducer(context, project)
  Builder->>Producer: with(FfmpegVideoProducerAdapter)
  Builder->>Producer: add MultiLyricsContainer + PopUpTextView timeline
  Builder->>Store: upsert(project.copy(sdui = createMotionSDUIJson(...)))
  Builder-->>Client: MotionVideoProducer
  Client->>Renderer: render/export frames
  Renderer-->>Client: mp4 output + progress callbacks`;

function MermaidDiagram({ chart }: { chart: string }) {
  const id = useId().replace(/:/g, '-');
  const [svg, setSvg] = useState('');

  useEffect(() => {
    let active = true;

    async function render() {
      const dynamicImport = new Function('u', 'return import(/* -ignore */ u)') as (u: string) => Promise<any>;
      const mermaidModule = await dynamicImport('https://cdn.jsdelivr.net/npm/mermaid/dist/mermaid.esm.min.mjs');
      const mermaid = mermaidModule.default;
      mermaid.initialize({ startOnLoad: false, securityLevel: 'loose', theme: 'dark' });
      const { svg: renderedSvg } = await mermaid.render(`mermaid-${id}`, chart);
      if (active) setSvg(renderedSvg);
    }

    render().catch(() => {
      if (active) setSvg('');
    });

    return () => {
      active = false;
    };
  }, [chart, id]);

  if (!svg) return <KotlinCodeBlock code={chart} />;

  return <div className="mt-4 overflow-x-auto rounded-lg border border-violet-400/20 bg-slate-950 p-4" dangerouslySetInnerHTML={{ __html: svg }} />;
}

export default function App() {
  return (
    <main className="min-h-screen bg-slate-950 text-slate-100">
      <section className="mx-auto max-w-6xl px-6 py-14">
        <h1 className="text-4xl font-bold leading-tight md:text-5xl">MotionLib Kotlin API Showcase</h1>
        <p className="mt-4 max-w-3xl text-slate-300">Kotlin-first examples sourced from /modules with render-flow documentation.</p>
      </section>

      <section className="mx-auto grid max-w-6xl gap-5 px-6 pb-16 md:grid-cols-2">
        {featureCards.map((feature) => (
          <article key={feature.title} className="rounded-xl border border-slate-800 bg-slate-900/70 p-6">
            <h2 className="text-xl font-semibold text-cyan-200">{feature.title}</h2>
            <p className="mt-2 text-sm text-slate-300">{feature.description}</p>
            <KotlinCodeBlock code={feature.snippet} />
          </article>
        ))}
      </section>

      <section className="mx-auto max-w-6xl px-6 pb-16">
        <article className="rounded-xl border border-violet-400/40 bg-violet-500/10 p-6">
          <h3 className="text-lg font-semibold text-violet-100">Video pipeline sequence diagram (Mermaid.js)</h3>
          <MermaidDiagram chart={sequenceDiagram} />
        </article>
      </section>
    </main>
  );
}
