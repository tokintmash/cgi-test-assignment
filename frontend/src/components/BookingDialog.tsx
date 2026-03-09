import { useState, type FormEvent } from 'react'
import type { RestaurantTable, SearchRequest } from '../types'
import { featureLabel } from '../utils/featureLabels'
import '../styles/BookingDialog.css'

interface BookingDialogProps {
  isOpen: boolean
  table: RestaurantTable | null
  criteria: SearchRequest
  isSubmitting: boolean
  errorMessage: string | null
  onClose: () => void
  onConfirm: (payload: { guestName: string; duration: number }) => void
}

export function BookingDialog({
  isOpen,
  table,
  criteria,
  isSubmitting,
  errorMessage,
  onClose,
  onConfirm,
}: BookingDialogProps) {
  const [guestName, setGuestName] = useState('')
  const [duration, setDuration] = useState(criteria.duration)

  if (!isOpen || !table) {
    return null
  }

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    onConfirm({
      guestName: guestName.trim(),
      duration,
    })
  }

  return (
    <div className="dialog-backdrop" onClick={onClose}>
      <section
        className="booking-dialog"
        role="dialog"
        aria-modal="true"
        aria-label={`Book ${table.name}`}
        onClick={(event) => event.stopPropagation()}
      >
        <h2>Confirm booking</h2>
        <p>
          {table.name} in {table.zone} · Capacity {table.capacity}
        </p>
        <p>
          {criteria.date} at {criteria.startTime} · Party of {criteria.partySize}
        </p>
        <p className="dialog-features">
          {table.features.length > 0
            ? table.features.map((feature) => featureLabel(feature)).join(' • ')
            : 'No special features'}
        </p>

        <form onSubmit={handleSubmit} className="booking-form">
          <label>
            Guest name
            <input
              type="text"
              value={guestName}
              onChange={(event) => setGuestName(event.target.value)}
              placeholder="Guest name"
              required
              minLength={2}
            />
          </label>

          <label>
            Duration (minutes)
            <input
              type="number"
              min={30}
              step={30}
              value={duration}
              onChange={(event) => setDuration(Number.parseInt(event.target.value, 10) || 30)}
              required
            />
          </label>

          {errorMessage && <p className="dialog-error">{errorMessage}</p>}

          <div className="dialog-actions">
            <button type="button" className="ghost-button" onClick={onClose} disabled={isSubmitting}>
              Cancel
            </button>
            <button type="submit" className="primary-button" disabled={isSubmitting}>
              {isSubmitting ? 'Booking...' : 'Confirm reservation'}
            </button>
          </div>
        </form>
      </section>
    </div>
  )
}
