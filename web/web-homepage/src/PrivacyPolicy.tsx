import React from 'react';

const PrivacyPolicy = () => {
  const navigateHome = (e: React.MouseEvent) => {
    e.preventDefault();
    window.history.pushState({}, '', '/');
    window.dispatchEvent(new PopStateEvent('popstate'));
    window.scrollTo(0, 0);
  };

  return (
    <div className="min-h-screen bg-[#050505] text-white font-sans selection:bg-emerald-500/30">
      {/* Navigation */}
      <nav className="fixed top-0 w-full z-50 border-b border-white/5 bg-black/50 backdrop-blur-xl">
        <div className="max-w-7xl mx-auto px-6 h-16 flex items-center justify-between">
          <a href="/" onClick={navigateHome} className="flex items-center gap-2">
            <div className="w-8 h-8 bg-gradient-to-tr from-emerald-400 to-blue-500 rounded-lg flex items-center justify-center font-bold text-black shadow-lg shadow-emerald-500/20">
              L
            </div>
            <span className="text-xl font-bold tracking-tight">LyricsMaker</span>
          </a>
          <a href="/" onClick={navigateHome} className="text-sm font-medium text-white/60 hover:text-white transition-colors">
            Back to Home
          </a>
        </div>
      </nav>

      <main className="max-w-4xl mx-auto px-6 pt-32 pb-20">
        <h1 className="text-4xl md:text-5xl font-black mb-8">Privacy Policy</h1>
        <p className="text-white/50 mb-12 italic">Last Updated: May 2026</p>

        <div className="prose prose-invert max-w-none space-y-12">
          {/* Introduction */}
          <section>
            <p className="text-white/70 leading-relaxed">
              This Privacy Policy describes Our policies and procedures on the collection, use and disclosure of Your information when You use the Service and tells You about Your privacy rights and how the law protects You. We use Your Personal data to provide and improve the Service. By using the Service, You agree to the collection and use of information in accordance with this Privacy Policy.
            </p>
          </section>

          {/* Interpretation and Definitions */}
          <section>
            <h2 className="text-2xl font-bold mb-4 text-emerald-400">1. Interpretation and Definitions</h2>
            <div className="space-y-4 text-white/70">
              <p><span className="text-white font-semibold">Account:</span> means a unique account created for You to access our Service or parts of our Service.</p>
              <p><span className="text-white font-semibold">Company:</span> (referred to as either "the Company", "We", "Us" or "Our" in this Agreement) refers to LyricsMaker.</p>
              <p><span className="text-white font-semibold">Service:</span> refers to the Application or Website.</p>
              <p><span className="text-white font-semibold">Personal Data:</span> is any information that relates to an identified or identifiable individual.</p>
            </div>
          </section>

          {/* Collecting and Using Your Personal Data */}
          <section>
            <h2 className="text-2xl font-bold mb-4 text-emerald-400">2. Collecting and Using Your Personal Data</h2>

            <div className="ml-4 space-y-8">
              <div>
                <h3 className="text-xl font-bold mb-3 text-white">Types of Data Collected</h3>

                <div className="space-y-6">
                  <div>
                    <h4 className="font-semibold text-emerald-300/80 mb-2">Personal Data</h4>
                    <p className="mb-2">While using Our Service, We may ask You to provide Us with certain personally identifiable information that can be used to contact or identify You. Personally identifiable information may include, but is not limited to:</p>
                    <ul className="list-disc pl-6 space-y-1">
                      <li>Email address</li>
                      <li>First name and last name</li>
                      <li>Usage Data</li>
                    </ul>
                  </div>

                  <div>
                    <h4 className="font-semibold text-emerald-300/80 mb-2">Usage Data</h4>
                    <p className="mb-2">Usage Data is collected automatically when using the Service. It may include information such as:</p>
                    <ul className="list-disc pl-6 space-y-1">
                      <li>Your Device's Internet Protocol address (e.g. IP address)</li>
                      <li>Browser type, browser version</li>
                      <li>The pages of our Service that You visit, the time and date of Your visit</li>
                      <li>Time spent on those pages, unique device identifiers and other diagnostic data</li>
                    </ul>
                  </div>

                  <div>
                    <h4 className="font-semibold text-emerald-300/80 mb-2">Tracking Technologies and Cookies</h4>
                    <p className="mb-2">We use Cookies and similar tracking technologies to track the activity on Our Service and store certain information. Tracking technologies used are beacons, tags, and scripts to collect and track information and to improve and analyze Our Service. These may include:</p>
                    <ul className="list-disc pl-6 space-y-1">
                      <li><span className="text-white font-semibold">Cookies or Browser Cookies:</span> A cookie is a small file placed on Your Device. You can instruct Your browser to refuse all Cookies or to indicate when a Cookie is being sent.</li>
                      <li><span className="text-white font-semibold">Web Beacons:</span> Certain sections of our Service and our emails may contain small electronic files known as web beacons.</li>
                    </ul>
                  </div>

                  <div className="bg-emerald-500/10 border border-emerald-500/20 p-4 rounded-xl">
                    <h4 className="font-semibold text-emerald-400 mb-2">User Generated Content</h4>
                    <p className="text-emerald-100/80">
                      We collect and store the content you create, including videos, synchronized lyrics, and audio waveforms. By using the Service, you acknowledge that this content may be used for promotional purposes or as training data for our motion synchronization systems.
                    </p>
                  </div>
                </div>
              </div>

              <div>
                <h3 className="text-xl font-bold mb-3 text-white">Use of Your Personal Data</h3>
                <p className="mb-4">The Company may use Personal Data for the following purposes:</p>
                <ul className="list-disc pl-6 space-y-2 text-white/70">
                  <li><span className="text-white font-semibold">To provide and maintain our Service:</span> including to monitor the usage of our Service.</li>
                  <li><span className="text-white font-semibold">To manage Your Account:</span> to manage Your registration as a user of the Service.</li>
                  <li><span className="text-white font-semibold">To contact You:</span> by email or other equivalent forms of electronic communication regarding updates or informative communications.</li>
                  <li><span className="text-white font-semibold">For promotional and training purposes:</span> To display your generated content in our marketing materials or to train our AI models to improve lyric synchronization and motion effects.</li>
                  <li><span className="text-white font-semibold">To manage Your requests:</span> To attend and manage Your requests to Us.</li>
                </ul>
              </div>
            </div>
          </section>

          {/* Retention of Your Personal Data */}
          <section>
            <h2 className="text-2xl font-bold mb-4 text-emerald-400">3. Retention of Your Personal Data</h2>
            <p className="text-white/70 leading-relaxed">
              The Company will retain Your Personal Data only for as long as is necessary for the purposes set out in this Privacy Policy. We will retain and use Your Personal Data to the extent necessary to comply with our legal obligations (for example, if we are required to retain your data to comply with applicable laws), resolve disputes, and enforce our legal agreements and policies.
            </p>
          </section>

          {/* Transfer of Your Personal Data */}
          <section>
            <h2 className="text-2xl font-bold mb-4 text-emerald-400">4. Transfer of Your Personal Data</h2>
            <p className="text-white/70 leading-relaxed">
              Your information, including Personal Data, is processed at the Company's operating offices and in any other places where the parties involved in the processing are located. It means that this information may be transferred to — and maintained on — computers located outside of Your state, province, country or other governmental jurisdiction where the data protection laws may differ than those from Your jurisdiction.
            </p>
          </section>

          {/* Disclosure of Your Personal Data */}
          <section>
            <h2 className="text-2xl font-bold mb-4 text-emerald-400">5. Disclosure of Your Personal Data</h2>
            <div className="space-y-6 text-white/70">
              <div>
                <h4 className="font-semibold text-white mb-2">Business Transactions</h4>
                <p>If the Company is involved in a merger, acquisition or asset sale, Your Personal Data may be transferred. We will provide notice before Your Personal Data is transferred and becomes subject to a different Privacy Policy.</p>
              </div>
              <div>
                <h4 className="font-semibold text-white mb-2">Law enforcement</h4>
                <p>Under certain circumstances, the Company may be required to disclose Your Personal Data if required to do so by law or in response to valid requests by public authorities (e.g. a court or a government agency).</p>
              </div>
            </div>
          </section>

          {/* Security of Your Personal Data */}
          <section>
            <h2 className="text-2xl font-bold mb-4 text-emerald-400">6. Security of Your Personal Data</h2>
            <p className="text-white/70 leading-relaxed">
              The security of Your Personal Data is important to Us, but remember that no method of transmission over the Internet, or method of electronic storage is 100% secure. While We strive to use commercially acceptable means to protect Your Personal Data, We cannot guarantee its absolute security.
            </p>
          </section>

          {/* Contact Us */}
          <section>
            <h2 className="text-2xl font-bold mb-4 text-emerald-400">7. Contact Us</h2>
            <p className="text-white/70">
              If you have any questions about this Privacy Policy, You can contact us:
            </p>
            <ul className="list-disc pl-6 mt-2 text-emerald-400">
              <li>By email: <a href="mailto:email@tejpratapsingh.com">email@tejpratapsingh.com</a></li>
            </ul>
          </section>
        </div>
      </main>

      <footer className="py-20 px-6 border-t border-white/5">
        <div className="max-w-7xl mx-auto flex flex-col md:flex-row justify-between items-center gap-10">
          <div className="flex flex-col items-center md:items-start gap-4">
            <div className="flex items-center gap-2">
              <div className="w-6 h-6 bg-white rounded flex items-center justify-center font-bold text-black text-xs">L</div>
              <span className="font-bold tracking-tight text-white/80">LyricsMaker</span>
            </div>
            <p className="text-sm text-white/30">© {new Date().getFullYear()} Built with Passion for Creators.</p>
          </div>
        </div>
      </footer>
    </div>
  );
};

export default PrivacyPolicy;
