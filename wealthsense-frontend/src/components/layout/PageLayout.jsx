import { Navbar } from './Navbar';
import { Sidebar } from './Sidebar';
import { OfflineIndicator } from '@/components/shared/OfflineIndicator';
import { cn } from '@/lib/utils';

const PageLayout = ({ children, noPadding }) => {
  return (
    <div className="min-h-screen bg-bg-primary">
      <OfflineIndicator />
      <Navbar />
      <Sidebar />
      {/* Main content: offset for navbar (top-16), sidebar on desktop, bottom nav on mobile */}
      <main id="main-content" className="pt-16 pb-20 md:pb-0 md:pl-[240px] transition-all duration-200">
        <div className={cn(noPadding ? '' : 'p-4 md:p-6 max-w-7xl mx-auto')}>
          {children}
        </div>
      </main>
    </div>
  );
};

export { PageLayout };


