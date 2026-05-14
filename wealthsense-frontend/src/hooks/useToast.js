import { useToastContext } from '@/components/ui/Toast';

export function useToast() {
  const { toast, addToast, removeToast } = useToastContext();
  return { toast, addToast, removeToast };
}
