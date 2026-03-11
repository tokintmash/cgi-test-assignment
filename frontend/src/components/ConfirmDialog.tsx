import '../styles/BookingDialog.css'

interface ConfirmDialogProps {
  isOpen: boolean
  title: string
  message: string
  confirmLabel: string
  isLoading: boolean
  result: string | null
  onConfirm: () => void
  onClose: () => void
}

export function ConfirmDialog({
  isOpen,
  title,
  message,
  confirmLabel,
  isLoading,
  result,
  onConfirm,
  onClose,
}: ConfirmDialogProps) {
  if (!isOpen) return null

  return (
    <div className="dialog-backdrop" onClick={onClose}>
      <section
        className="booking-dialog"
        role="dialog"
        aria-modal="true"
        aria-label={title}
        onClick={(event) => event.stopPropagation()}
      >
        <h2>{title}</h2>
        {result ? (
          <>
            <p className="dialog-success">{result}</p>
            <div className="dialog-actions">
              <button type="button" className="primary-button" onClick={onClose}>
                Close
              </button>
            </div>
          </>
        ) : (
          <>
            <p>{message}</p>
            <div className="dialog-actions">
              <button type="button" className="ghost-button" onClick={onClose} disabled={isLoading}>
                Cancel
              </button>
              <button type="button" className="primary-button btn-danger" onClick={onConfirm} disabled={isLoading}>
                {isLoading ? 'Processing...' : confirmLabel}
              </button>
            </div>
          </>
        )}
      </section>
    </div>
  )
}
