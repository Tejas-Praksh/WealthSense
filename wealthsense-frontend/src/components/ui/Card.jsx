import { forwardRef } from 'react';
import { motion } from 'framer-motion';
import { cn } from '@/lib/utils';

const cardVariants = {
  default: 'bg-bg-card border border-color-border',
  elevated: 'bg-bg-card border border-color-border shadow-lg shadow-black/20',
  clickable: 'bg-bg-card border border-color-border cursor-pointer hover:border-accent-green/40 hover:shadow-lg hover:shadow-accent-green/5 transition-all duration-200',
};

const Card = forwardRef(
  ({ className, variant = 'default', children, animate = true, ...props }, ref) => {
    const Component = animate ? motion.div : 'div';
    const animationProps = animate
      ? {
          initial: { opacity: 0, y: 12 },
          animate: { opacity: 1, y: 0 },
          transition: { duration: 0.35, ease: 'easeOut' },
        }
      : {};

    return (
      <Component
        ref={ref}
        className={cn('rounded-card p-6', cardVariants[variant], className)}
        {...animationProps}
        {...props}
      >
        {children}
      </Component>
    );
  }
);

Card.displayName = 'Card';

const CardHeader = forwardRef(({ className, ...props }, ref) => (
  <div ref={ref} className={cn('flex flex-col gap-1.5 pb-4', className)} {...props} />
));
CardHeader.displayName = 'CardHeader';

const CardTitle = forwardRef(({ className, ...props }, ref) => (
  <h3 ref={ref} className={cn('text-lg font-semibold text-text-primary', className)} {...props} />
));
CardTitle.displayName = 'CardTitle';

const CardDescription = forwardRef(({ className, ...props }, ref) => (
  <p ref={ref} className={cn('text-sm text-text-secondary', className)} {...props} />
));
CardDescription.displayName = 'CardDescription';

const CardContent = forwardRef(({ className, ...props }, ref) => (
  <div ref={ref} className={cn('', className)} {...props} />
));
CardContent.displayName = 'CardContent';

const CardFooter = forwardRef(({ className, ...props }, ref) => (
  <div ref={ref} className={cn('flex items-center pt-4', className)} {...props} />
));
CardFooter.displayName = 'CardFooter';

export { Card, CardHeader, CardTitle, CardDescription, CardContent, CardFooter };
