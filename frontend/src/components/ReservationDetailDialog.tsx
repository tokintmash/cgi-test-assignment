import type { RestaurantTable, TableStatus } from '../types'
import { featureLabel } from '../utils/featureLabels'
import '../styles/BookingDialog.css'

interface ReservationDetailDialogProps {
  isOpen: boolean
  table: RestaurantTable | null
  tableStatus: TableStatus | null
  isCancelling: boolean
  errorMessage: string | null
  successMessage: string | null
  onClose: () => void
  onCancel: () => void
}

export function ReservationDetailDialog({
  isOpen,
  table,
  tableStatus,
  isCancelling,
  errorMessage,
  successMessage,
  onClose,
  onCancel,
}: ReservationDetailDialogProps) {
  if (!isOpen || !table || !tableStatus) {
    return null
  }

  return (
    <div className="dialog-backdrop" onClick={onClose}>
      <section
        className="booking-dialog"
        role="dialog"
        aria-modal="true"
        aria-label={`Reservation for ${table.name}`}
        onClick={(event) => event.stopPropagation()}
      >
        {successMessage ? (
          <>
            <h2>Reservation cancelled</h2>
            <p className="dialog-success">{successMessage}</p>
            <div className="dialog-actions">
              <button type="button" className="primary-button" onClick={onClose}>
                Close
              </button>
            </div>
          </>
        ) : (
          <>
            <h2>Reservation details</h2>
            <p>
              {table.name} in {table.zone} · Seats {table.capacity}
            </p>
            <p>
              Guest: <strong>{tableStatus.guestName}</strong>
            </p>
            <p>
              Time: {tableStatus.reservationStart?.slice(0, 5)} – {tableStatus.reservationEnd?.slice(0, 5)}
            </p>
            <p className="dialog-features">
              {table.features.length > 0
                ? table.features.map((feature) => featureLabel(feature)).join(' · ')
                : 'No special features'}
            </p>

            {errorMessage && <p className="dialog-error">{errorMessage}</p>}

            <div className="dialog-actions">
              <button type="button" className="ghost-button" onClick={onClose} disabled={isCancelling}>
                Close
              </button>
              <button type="button" className="btn-danger" onClick={onCancel} disabled={isCancelling}>
                {isCancelling ? 'Cancelling...' : 'Cancel reservation'}
              </button>
            </div>
          </>
        )}
      </section>
    </div>
  )
}
