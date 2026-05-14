import { forwardRef, useState } from 'react';
import { Eye, EyeOff } from 'lucide-react';
import { cn } from '@/lib/utils';

const Input = forwardRef(
  (
    {
      className,
      type = 'text',
      label,
      error,
      id,
      icon: Icon,
      ...props
    },
    ref
  ) => {
    const [showPassword, setShowPassword] = useState(false);
    const isPassword = type === 'password';
    const inputType = isPassword ? (showPassword ? 'text' : 'password') : type;

    return (
      <div className="w-full space-y-1.5">
        {label && (
          <label
            htmlFor={id}
            className="block text-sm font-medium text-text-secondary"
          >
            {label}
          </label>
        )}
        <div className="relative">
          {Icon && (
            <div className="absolute left-3 top-1/2 -translate-y-1/2 text-text-secondary">
              <Icon className="h-4 w-4" />
            </div>
          )}
          <input
            ref={ref}
            id={id}
            type={inputType}
            className={cn(
              'w-full h-11 rounded-btn bg-bg-primary border border-color-border px-4 text-sm text-text-primary placeholder:text-text-secondary/60 transition-all duration-200',
              'focus:outline-none focus:ring-2 focus:ring-accent-green/50 focus:border-accent-green',
              'disabled:opacity-50 disabled:cursor-not-allowed',
              Icon && 'pl-10',
              isPassword && 'pr-10',
              error && 'border-accent-red focus:ring-accent-red/50 focus:border-accent-red',
              className
            )}
            {...props}
          />
          {isPassword && (
            <button
              type="button"
              tabIndex={-1}
              onClick={() => setShowPassword(!showPassword)}
              className="absolute right-3 top-1/2 -translate-y-1/2 text-text-secondary hover:text-text-primary transition-colors"
            >
              {showPassword ? (
                <EyeOff className="h-4 w-4" />
              ) : (
                <Eye className="h-4 w-4" />
              )}
            </button>
          )}
        </div>
        {error && (
          <p className="text-xs text-accent-red mt-1">{error}</p>
        )}
      </div>
    );
  }
);

Input.displayName = 'Input';

export { Input };
