import { useState, useEffect } from 'react';
import PrivacyPolicy from './PrivacyPolicy';

function App() {
  const [currentPage, setCurrentPage] = useState('home');

  useEffect(() => {
    // Basic routing support
    const handlePopState = () => {
      const path = window.location.pathname;
      if (path === '/privacy') {
        setCurrentPage('privacy');
      } else {
        setCurrentPage('home');
      }
    };

    window.addEventListener('popstate', handlePopState);
    handlePopState(); // Check initial path

    return () => window.removeEventListener('popstate', handlePopState);
  }, []);

  const navigateToPrivacy = (e: React.MouseEvent) => {
    e.preventDefault();
    window.history.pushState({}, '', '/privacy');
    setCurrentPage('privacy');
    window.scrollTo(0, 0);
  };

  if (currentPage === 'privacy') {
    return <PrivacyPolicy />;
  }

  return (
    <div className="min-h-screen bg-[#050505] text-white font-sans selection:bg-emerald-500/30">
      {/* Navigation */}
      <nav className="fixed top-0 w-full z-50 border-b border-white/5 bg-black/50 backdrop-blur-xl">
        <div className="max-w-7xl mx-auto px-6 h-16 flex items-center justify-between">
          <div className="flex items-center gap-2">
            <div className="w-8 h-8 bg-gradient-to-tr from-emerald-400 to-blue-500 rounded-lg flex items-center justify-center font-bold text-black shadow-lg shadow-emerald-500/20">
              L
            </div>
            <span className="text-xl font-bold tracking-tight">LyricsMaker</span>
          </div>
          <div className="hidden md:flex items-center gap-8 text-sm font-medium text-white/60">
            <a href="#features" className="hover:text-white transition-colors">Features</a>
            <a href="#samples" className="hover:text-white transition-colors">Samples</a>
            <button className="bg-white text-black px-5 py-2 rounded-full font-bold hover:bg-emerald-400 transition-all active:scale-95">
              Launch App
            </button>
          </div>
        </div>
      </nav>

      {/* Hero Section */}
      <header className="relative pt-32 pb-20 md:pt-48 md:pb-32 px-6 overflow-hidden">
        {/* Background Gradients */}
        <div className="absolute top-0 left-1/2 -translate-x-1/2 w-full max-w-6xl h-full -z-10 pointer-events-none">
          <div className="absolute top-0 left-1/4 w-96 h-96 bg-emerald-500/20 blur-[120px] rounded-full animate-pulse" />
          <div className="absolute bottom-0 right-1/4 w-96 h-96 bg-blue-600/20 blur-[120px] rounded-full" />
        </div>

        <div className="max-w-5xl mx-auto text-center">
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full border border-emerald-500/20 bg-emerald-500/5 text-emerald-400 text-xs font-bold uppercase tracking-widest mb-8 animate-fade-in">
            <span className="relative flex h-2 w-2">
              <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75"></span>
              <span className="relative inline-flex rounded-full h-2 w-2 bg-emerald-500"></span>
            </span>
            New: Motion Effects v2.0
          </div>
          <h1 className="text-5xl md:text-8xl font-black tracking-tighter mb-8 leading-[0.9]">
            MAKE YOUR <br />
            <span className="bg-gradient-to-r from-emerald-400 via-emerald-200 to-blue-500 bg-clip-text text-transparent italic">
              LYRICS MOVE.
            </span>
          </h1>
          <p className="text-lg md:text-xl text-white/50 max-w-2xl mx-auto mb-12 leading-relaxed">
            Create professional-grade synchronized lyric videos with dynamic waveforms and cinematic animations. All powered by SDUI.
          </p>
          <div className="flex flex-col sm:flex-row items-center justify-center gap-4">
            <button className="w-full sm:w-auto bg-emerald-500 hover:bg-emerald-400 text-black px-10 py-4 rounded-2xl font-black text-lg transition-all shadow-xl shadow-emerald-500/20 active:scale-95">
              Get Started for Free
            </button>
            <a href="#samples" className="w-full sm:w-auto bg-white/5 hover:bg-white/10 border border-white/10 px-10 py-4 rounded-2xl font-bold text-lg transition-all backdrop-blur-sm text-center">
              View Samples
            </a>
          </div>
        </div>
      </header>

      {/* Features Grid */}
      <section id="features" className="py-24 px-6 relative">
        <div className="max-w-7xl mx-auto">
          <div className="mb-16">
            <h2 className="text-3xl font-bold mb-4">Powerful Features</h2>
            <div className="h-1 w-20 bg-emerald-500" />
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            <FeatureCard
              title="Real-time Waveforms"
              description="Our advanced audio engine generates beautiful, synchronized waveforms that react to every beat of your music."
              color="emerald"
            />
            <FeatureCard
              title="SDUI Driven"
              description="Total control over layout and animations via our Server-Driven UI architecture. Change once, update everywhere."
              color="blue"
            />
            <FeatureCard
              title="Export Anywhere"
              description="High-fidelity rendering in 9:16 or 16:9 formats, optimized for TikTok, Reels, and YouTube Shorts."
              color="purple"
            />
            <div className="md:col-span-2 relative group overflow-hidden rounded-3xl border border-white/5 bg-white/[0.02] p-8 hover:bg-white/[0.04] transition-all">
               <div className="relative z-10">
                 <h3 className="text-2xl font-bold mb-4">Precision Timing Editor</h3>
                 <p className="text-white/50 max-w-md">Fine-tune lyric appearances down to the millisecond with our visual timeline editor. Perfect sync, every time.</p>
               </div>
               <div className="absolute right-0 bottom-0 top-0 w-1/3 bg-gradient-to-l from-emerald-500/10 to-transparent pointer-events-none" />
            </div>
            <div className="relative group overflow-hidden rounded-3xl border border-white/5 bg-white/[0.02] p-8 hover:bg-white/[0.04] transition-all">
               <h3 className="text-2xl font-bold mb-4">4K Rendering</h3>
               <p className="text-white/50">Crystal clear quality for premium content creators.</p>
            </div>
          </div>
        </div>
      </section>

      {/* Samples Section */}
      <SamplesSection />

      {/* Footer */}
      <footer className="py-20 px-6 border-t border-white/5">
        <div className="max-w-7xl mx-auto flex flex-col md:flex-row justify-between items-center gap-10">
          <div className="flex flex-col items-center md:items-start gap-4">
            <div className="flex items-center gap-2">
              <div className="w-6 h-6 bg-white rounded flex items-center justify-center font-bold text-black text-xs">L</div>
              <span className="font-bold tracking-tight text-white/80">LyricsMaker</span>
            </div>
            <p className="text-sm text-white/30">© {new Date().getFullYear()} Built with Passion for Creators.</p>
          </div>
          <div className="flex gap-8 text-sm text-white/50">
            <a href="/privacy" onClick={navigateToPrivacy} className="hover:text-white transition-colors">Privacy Policy</a>
            <a href="#" className="hover:text-white transition-colors">Twitter</a>
            <a href="#" className="hover:text-white transition-colors">GitHub</a>
            <a href="#" className="hover:text-white transition-colors">Discord</a>
          </div>
        </div>
      </footer>
    </div>
  );
}

function SamplesSection() {
  const videos = [
    { id: 1, src: "/videos/sample_1.mp4", title: "Cinematic Zoom" },
    { id: 2, src: "/videos/sample_2.mp4", title: "Accent Beat" },
    { id: 3, src: "/videos/sample_3.mp4", title: "TypeWriter Mode" },
    { id: 4, src: "/videos/sample_4.mp4", title: "Accent Beat" },
  ];

  return (
    <section id="samples" className="py-24 bg-black/20 overflow-hidden">
      <div className="max-w-7xl mx-auto px-6">
        <div className="mb-16 text-center">
          <h2 className="text-3xl md:text-5xl font-bold mb-4">See it in Action</h2>
          <p className="text-white/50 max-w-2xl mx-auto">
            Discover the possibilities with our pre-built templates and custom motion graphics.
          </p>
        </div>

        <div className="flex overflow-x-auto gap-8 pb-8 snap-x snap-mandatory no-scrollbar scroll-smooth">
          {videos.map((video) => (
            <div key={video.id} className="flex-none w-[280px] md:w-[320px] snap-center group relative aspect-[9/16] rounded-3xl overflow-hidden border border-white/10 bg-white/5 hover:border-emerald-500/50 transition-all duration-500 shadow-2xl">
              <video
                src={video.src}
                className="w-full h-full object-cover opacity-80 group-hover:opacity-100 transition-opacity duration-500"
                muted
                loop
                onMouseOver={(e) => (e.target as HTMLVideoElement).play()}
                onMouseOut={(e) => {
                  const v = e.target as HTMLVideoElement;
                  v.pause();
                  v.currentTime = 0;
                }}
              />
              <div className="absolute inset-0 bg-gradient-to-t from-black/80 via-transparent to-transparent opacity-60 group-hover:opacity-40 transition-opacity pointer-events-none" />
              <div className="absolute bottom-6 left-6 right-6 pointer-events-none">
                <p className="text-xs font-bold text-emerald-400 uppercase tracking-widest mb-1">{`Sample 0${video.id}`}</p>
                <h3 className="text-xl font-bold">{video.title}</h3>
              </div>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}

function FeatureCard({ title, description, color }: { title: string, description: string, color: string }) {
  const colors = {
    emerald: "from-emerald-400 to-emerald-600 shadow-emerald-500/10",
    blue: "from-blue-400 to-blue-600 shadow-blue-500/10",
    purple: "from-purple-400 to-purple-600 shadow-purple-500/10"
  };

  return (
    <div className="group relative rounded-3xl border border-white/5 bg-white/[0.02] p-8 hover:bg-white/[0.04] transition-all hover:-translate-y-1">
      <div className={`w-12 h-12 rounded-2xl bg-gradient-to-tr ${colors[color as keyof typeof colors]} mb-6 shadow-2xl`} />
      <h3 className="text-xl font-bold mb-4 group-hover:text-emerald-400 transition-colors">{title}</h3>
      <p className="text-white/50 leading-relaxed">{description}</p>
    </div>
  );
}

export default App;
