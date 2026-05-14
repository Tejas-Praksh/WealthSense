import { useState, memo } from 'react';
import { FileDown, FileSpreadsheet, FileText } from 'lucide-react';
import { Modal } from '@/components/ui/Modal';
import { Button } from '@/components/ui/Button';
import { cn } from '@/lib/utils';

const FORMATS = [
  { value: 'csv', label: 'CSV', icon: FileSpreadsheet, desc: 'Comma-separated values' },
  { value: 'xlsx', label: 'Excel', icon: FileSpreadsheet, desc: 'Microsoft Excel format' },
  { value: 'pdf', label: 'PDF', icon: FileText, desc: 'Portable Document Format' },
];

const ExportModal = memo(({ isOpen, onClose }) => {
  const [format, setFormat] = useState('csv');
  const [exporting, setExporting] = useState(false);

  const handleExport = () => {
    setExporting(true);
    setTimeout(() => {
      setExporting(false);
      onClose();
    }, 1500);
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Export Transactions" size="sm">
      <div className="space-y-4">
        <div>
          <label className="text-[10px] uppercase tracking-wider text-text-secondary font-medium mb-2 block">Format</label>
          <div className="space-y-2">
            {FORMATS.map((f) => {
              const Icon = f.icon;
              return (
                <button
                  key={f.value}
                  onClick={() => setFormat(f.value)}
                  className={cn(
                    'w-full flex items-center gap-3 p-3 rounded-btn border transition-all text-left',
                    format === f.value
                      ? 'border-accent-green/30 bg-accent-green/5'
                      : 'border-color-border hover:border-text-secondary bg-bg-primary'
                  )}
                >
                  <Icon className={cn('h-4 w-4', format === f.value ? 'text-accent-green' : 'text-text-secondary')} />
                  <div>
                    <p className="text-sm font-medium text-text-primary">{f.label}</p>
                    <p className="text-[10px] text-text-secondary">{f.desc}</p>
                  </div>
                </button>
              );
            })}
          </div>
        </div>

        <Button variant="primary" className="w-full" onClick={handleExport} isLoading={exporting}>
          <FileDown className="h-4 w-4" />
          Export as {format.toUpperCase()}
        </Button>
      </div>
    </Modal>
  );
});

ExportModal.displayName = 'ExportModal';
export { ExportModal };
