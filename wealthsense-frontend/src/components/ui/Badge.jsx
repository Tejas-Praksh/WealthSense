import { cn } from '@/lib/utils';
import { BADGE_COLORS } from '@/utils/constants';

const Badge = ({ type, children, className, pulse = false, ...props }) => {
  const colors = BADGE_COLORS[type] || BADGE_COLORS.PENDING;

  return (
    <span
      className={cn(
        'inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-medium border',
        colors.bg,
        colors.text,
        colors.border,
        pulse && 'animate-pulse-slow',
        className
      )}
      {...props}
    >
      {type === 'FLAGGED' && (
        <span className="relative flex h-1.5 w-1.5">
          <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-accent-red opacity-75" />
          <span className="relative inline-flex rounded-full h-1.5 w-1.5 bg-accent-red" />
        </span>
      )}
      {children}
    </span>
  );
};

export { Badge };
