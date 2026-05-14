import { useState, useCallback, memo } from 'react';
import { motion } from 'framer-motion';
import { Upload, FileSpreadsheet, Check, AlertCircle, X } from 'lucide-react';
import { Modal } from '@/components/ui/Modal';
import { Button } from '@/components/ui/Button';
import { ProgressBar } from '@/components/ui/ProgressBar';

const CSVUploadModal = memo(({ isOpen, onClose }) => {
  const [file, setFile] = useState(null);
  const [preview, setPreview] = useState(null);
  const [uploading, setUploading] = useState(false);
  const [progress, setProgress] = useState(0);
  const [result, setResult] = useState(null);
  const [dragOver, setDragOver] = useState(false);

  const handleFile = useCallback((f) => {
    if (!f) return;
    setFile(f);
    // Mock preview
    setPreview({
      headers: ['Date', 'Description', 'Amount', 'Type'],
      rows: [
        ['10 May 2026', 'Zomato Order', '₹340', 'Debit'],
        ['10 May 2026', 'Ola Auto', '₹85', 'Debit'],
        ['9 May 2026', 'Salary Credit', '₹26,000', 'Credit'],
        ['8 May 2026', 'Amazon.in', '₹1,299', 'Debit'],
        ['7 May 2026', 'Netflix India', '₹649', 'Debit'],
      ],
      totalRows: 47,
    });
  }, []);

  const handleDrop = useCallback((e) => {
    e.preventDefault();
    setDragOver(false);
    const f = e.dataTransfer.files[0];
    if (f && (f.name.endsWith('.csv') || f.name.endsWith('.xlsx'))) {
      handleFile(f);
    }
  }, [handleFile]);

  const handleImport = useCallback(() => {
    setUploading(true);
    let p = 0;
    const interval = setInterval(() => {
      p += Math.random() * 15;
      if (p >= 100) {
        p = 100;
        clearInterval(interval);
        setUploading(false);
        setResult({ count: 47, status: 'success' });
      }
      setProgress(Math.min(p, 100));
    }, 200);
  }, []);

  const handleReset = useCallback(() => {
    setFile(null);
    setPreview(null);
    setProgress(0);
    setResult(null);
  }, []);

  const handleClose = useCallback(() => {
    handleReset();
    onClose();
  }, [onClose, handleReset]);

  return (
    <Modal isOpen={isOpen} onClose={handleClose} title="Import Transactions" size="lg">
      {result ? (
        <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="text-center py-8">
          <div className="w-16 h-16 rounded-full bg-accent-green/10 flex items-center justify-center mx-auto mb-4">
            <Check className="h-8 w-8 text-accent-green" />
          </div>
          <h3 className="text-lg font-semibold text-text-primary mb-1">Import complete!</h3>
          <p className="text-sm text-text-secondary">{result.count} transactions imported successfully</p>
          <Button variant="primary" size="sm" className="mt-4" onClick={handleClose}>Done</Button>
        </motion.div>
      ) : !preview ? (
        <div
          onDragOver={(e) => { e.preventDefault(); setDragOver(true); }}
          onDragLeave={() => setDragOver(false)}
          onDrop={handleDrop}
          className={`border-2 border-dashed rounded-card p-10 text-center transition-colors ${
            dragOver
              ? 'border-accent-green bg-accent-green/5'
              : 'border-color-border hover:border-text-secondary'
          }`}
        >
          <Upload className="h-10 w-10 text-text-secondary mx-auto mb-3" />
          <p className="text-sm font-medium text-text-primary mb-1">Drop your bank statement here</p>
          <p className="text-xs text-text-secondary mb-4">Supports CSV and XLSX files</p>
          <label>
            <input
              type="file"
              accept=".csv,.xlsx"
              className="hidden"
              onChange={(e) => handleFile(e.target.files[0])}
            />
            <span className="inline-flex items-center gap-1.5 px-4 py-2 bg-accent-green/10 text-accent-green text-sm font-medium rounded-btn cursor-pointer hover:bg-accent-green/20 transition-colors">
              <FileSpreadsheet className="h-4 w-4" />
              Browse files
            </span>
          </label>
        </div>
      ) : (
        <div className="space-y-4">
          {/* File info */}
          <div className="flex items-center justify-between p-3 bg-bg-primary rounded-btn">
            <div className="flex items-center gap-2">
              <FileSpreadsheet className="h-4 w-4 text-accent-green" />
              <span className="text-sm text-text-primary">{file?.name || 'statement.csv'}</span>
            </div>
            <button onClick={handleReset} className="text-text-secondary hover:text-text-primary">
              <X className="h-3.5 w-3.5" />
            </button>
          </div>

          {/* Preview table */}
          <div className="overflow-x-auto rounded-card border border-color-border">
            <table className="w-full text-xs">
              <thead>
                <tr className="bg-bg-primary">
                  {preview.headers.map((h) => (
                    <th key={h} className="px-3 py-2 text-left text-text-secondary font-medium">{h}</th>
                  ))}
                </tr>
              </thead>
              <tbody className="divide-y divide-color-border/40">
                {preview.rows.map((row, i) => (
                  <tr key={i}>
                    {row.map((cell, j) => (
                      <td key={j} className="px-3 py-2 text-text-primary tabular-nums">{cell}</td>
                    ))}
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <p className="text-xs text-text-secondary">
            Showing 5 of {preview.totalRows} rows
          </p>

          {/* Progress */}
          {uploading && (
            <div className="space-y-1">
              <ProgressBar value={progress} max={100} size="sm" />
              <p className="text-[10px] text-text-secondary text-center">Importing... {Math.round(progress)}%</p>
            </div>
          )}

          {/* Import button */}
          <Button
            variant="primary"
            className="w-full"
            onClick={handleImport}
            isLoading={uploading}
          >
            Import {preview.totalRows} transactions
          </Button>
        </div>
      )}
    </Modal>
  );
});

CSVUploadModal.displayName = 'CSVUploadModal';
export { CSVUploadModal };
