import { cn } from '@/lib/utils';

const Skeleton = ({ className, ...props }) => (
  <div className={cn('skeleton rounded-btn', className)} {...props} />
);

const SkeletonText = ({ lines = 3, className }) => (
  <div className={cn('space-y-2', className)}>
    {Array.from({ length: lines }).map((_, i) => (
      <Skeleton
        key={i}
        className={cn('h-3', i === lines - 1 ? 'w-3/4' : 'w-full')}
      />
    ))}
  </div>
);

const SkeletonCard = ({ className }) => (
  <div className={cn('bg-bg-card rounded-card border border-color-border p-6', className)}>
    <div className="space-y-4">
      <Skeleton className="h-5 w-1/3" />
      <Skeleton className="h-10 w-2/3" />
      <div className="flex gap-4">
        <Skeleton className="h-8 w-24" />
        <Skeleton className="h-8 w-24" />
        <Skeleton className="h-8 w-24" />
      </div>
    </div>
  </div>
);

const SkeletonTransaction = () => (
  <div className="flex items-center gap-3 p-3">
    <Skeleton className="h-10 w-10 rounded-full flex-shrink-0" />
    <div className="flex-1 space-y-1.5">
      <Skeleton className="h-3.5 w-32" />
      <Skeleton className="h-3 w-20" />
    </div>
    <Skeleton className="h-4 w-16" />
  </div>
);

const SkeletonChart = ({ className }) => (
  <div className={cn('bg-bg-card rounded-card border border-color-border p-6', className)}>
    <Skeleton className="h-5 w-40 mb-4" />
    <div className="flex items-end gap-2 h-40">
      {[40, 65, 30, 80, 55, 70, 45].map((h, i) => (
        <Skeleton key={i} className="flex-1 rounded-t-sm" style={{ height: `${h}%` }} />
      ))}
    </div>
  </div>
);

export { Skeleton, SkeletonText, SkeletonCard, SkeletonTransaction, SkeletonChart };
