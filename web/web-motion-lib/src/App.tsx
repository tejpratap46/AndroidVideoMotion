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
    val file: File,
    var startFrame: Int = 0,
    var endFrame: Int = 0,
    var delayFrame: Int = 0,
)

// usage
val narration = MotionAudio(
    file = File("/storage/emulated/0/Download/narration.mp3"),
    startFrame = 0,
    endFrame = 360,
)
project.audios.add(narration)`
  },
  {
    title: 'MotionVideoProducer usage',
    description: 'Concrete Kotlin pipeline assembly from modules/motionlib and app module sample usage.',
    snippet: `// modules/app/src/main/java/.../SampleMotionVideo.kt (usage style)
val motionVideo = MotionVideoProducer
    .with(
        context = applicationContext,
        config = MotionConfig(fps = 30),
    )
    .addMotionViewToSequence(rootContainer)

// later in PreviewActivity/Worker
override fun getMotionVideo(): MotionVideoProducer = motionVideo`
  },
  {
    title: 'SDUIMotionVideoProducerFactory class',
    description: 'Kotlin class from modules/sdui for turning MotionProject/JSON into a MotionVideoProducer.',
    snippet: `// modules/sdui/src/main/java/.../SDUIMotionVideoProducerFactory.kt
class SDUIMotionVideoProducerFactory(
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
  }
];

const integrationFeatures = [
  'Encoders: Android Native Media implementation, FFMPEG, and JCodec',
  'AI capability with PyTorch and TensorFlow inference pipelines',
  'Project management and sync workflows with MotionStore',
  'SDUI JSON serialization to recreate projects deterministically',
  'Prebuild templates for rapid video generation (upcoming)'
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
                if (part.startsWith('//')) {
                  return <span key={partIndex} className="text-slate-500">{part}</span>;
                }

                if (part.startsWith('"') && part.endsWith('"')) {
                  return <span key={partIndex} className="text-emerald-300">{part}</span>;
                }

                if (kotlinKeywords.has(part)) {
                  return <span key={partIndex} className="text-fuchsia-300">{part}</span>;
                }

                if (/^[A-Z][A-Za-z0-9_]*$/.test(part)) {
                  return <span key={partIndex} className="text-cyan-300">{part}</span>;
                }

                if (/^\d+$/.test(part)) {
                  return <span key={partIndex} className="text-amber-300">{part}</span>;
                }

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
  participant Client as App/Worker
  participant Store as MotionStore(MotionProject)
  participant Factory as SDUIMotionVideoProducerFactory
  participant Parser as SDUI Parsers
  participant Producer as MotionVideoProducer
  participant Renderer as Encoder/Renderer

  Client->>Store: load MotionProject(id)
  Store-->>Client: MotionProject + SDUI JSON
  Client->>Factory: createFromProject(project)
  Factory->>Parser: toMotionConfig / toMotionView / toMotionAudio
  Parser-->>Factory: Kotlin domain objects
  Factory->>Producer: MotionVideoProducer.with(...)
  Factory-->>Client: producer
  Client->>Renderer: produce(producer)
  Renderer-->>Client: MP4/video output`;

export default function App() {
  return (
    <main className="min-h-screen bg-slate-950 text-slate-100">
      <section className="mx-auto max-w-6xl px-6 py-14">
        <p className="mb-3 inline-flex rounded-full border border-cyan-400/40 px-3 py-1 text-xs uppercase tracking-[0.2em] text-cyan-300">
          web-motion-lib
        </p>
        <h1 className="text-4xl font-bold leading-tight md:text-5xl">MotionLib Kotlin API Showcase</h1>
        <p className="mt-4 max-w-3xl text-slate-300">
          A React + Vite + Tailwind web app that now showcases Kotlin classes and usage patterns from the
          <span className="mx-1 rounded bg-slate-800 px-1 py-0.5 text-slate-100">/modules</span>
          source of MotionLib.
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
            <KotlinCodeBlock code={feature.snippet} />
          </article>
        ))}
      </section>

      <section className="mx-auto max-w-6xl px-6 pb-16">
        <article className="rounded-xl border border-violet-400/40 bg-violet-500/10 p-6">
          <h3 className="text-lg font-semibold text-violet-100">Video pipeline sequence diagram</h3>
          <p className="mt-2 text-sm text-violet-50/90">
            Mermaid sequence diagram for how MotionProject data becomes a rendered output video.
          </p>
          <KotlinCodeBlock code={sequenceDiagram} />
        </article>
      </section>
    </main>
  );
}
