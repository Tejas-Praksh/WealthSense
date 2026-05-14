import { cn } from '@/lib/utils';
import { getInitials } from '@/utils/formatters';

const sizeClasses = {
  sm: 'h-8 w-8 text-xs',
  md: 'h-10 w-10 text-sm',
  lg: 'h-12 w-12 text-base',
  xl: 'h-16 w-16 text-lg',
};

const Avatar = ({ src, name, size = 'md', className, ...props }) => {
  const initials = getInitials(name);

  return (
    <div
      className={cn(
        'relative flex items-center justify-center rounded-full bg-bg-secondary border border-color-border font-semibold text-text-secondary overflow-hidden flex-shrink-0',
        sizeClasses[size],
        className
      )}
      {...props}
    >
      {src ? (
        <img
          src={src}
          alt={name || 'Avatar'}
          className="h-full w-full object-cover"
          onError={(e) => {
            e.target.style.display = 'none';
          }}
        />
      ) : (
        <span>{initials}</span>
      )}
    </div>
  );
};

export { Avatar };
