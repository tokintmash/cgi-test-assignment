import { useRef, type ChangeEvent, type FormEvent } from 'react'
import type { SearchRequest, TableFeature } from '../types'
import { featureOptions } from '../utils/featureLabels'
import '../styles/SearchForm.css'

interface SearchFormProps {
  value: SearchRequest
  zones: string[]
  isLoading: boolean
  onChange: (next: SearchRequest) => void
  onSubmit: (criteria: SearchRequest) => void
}

export function SearchForm({ value, zones, isLoading, onChange, onSubmit }: SearchFormProps) {
  const dateRef = useRef<HTMLInputElement>(null)

  const handleDateClick = () => {
    dateRef.current?.showPicker()
  }

  const handleInputChange = (event: ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value: nextValue } = event.target

    if (name === 'partySize' || name === 'duration') {
      const parsed = Number.parseInt(nextValue, 10)
      onChange({ ...value, [name]: Number.isNaN(parsed) ? 0 : parsed })
      return
    }

    onChange({ ...value, [name]: nextValue })
  }

  const handlePreferenceChange = (preference: TableFeature) => {
    const exists = value.preferences.includes(preference)
    const nextPreferences = exists
      ? value.preferences.filter((item) => item !== preference)
      : [...value.preferences, preference]

    onChange({
      ...value,
      preferences: nextPreferences,
    })
  }

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    onSubmit(value)
  }

  const timeSlots = (() => {
    const slots: string[] = []
    for (let h = 10; h <= 22; h++) {
      for (const m of [0, 30]) {
        if (h === 22 && m === 30) break
        slots.push(`${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}`)
      }
    }
    return slots
  })()

  const durationOptions = [30, 60, 90, 120, 150, 180]

  const partySizeOptions = Array.from({ length: 20 }, (_, i) => i + 1)

  return (
    <section className="card-panel search-panel" aria-label="Search tables">
      <h2 className="panel-title">Search tables</h2>
      <form className="search-form" onSubmit={handleSubmit}>
        <div className="search-row search-row-main">
          <label>
            Date
            <input
              ref={dateRef}
              type="date"
              name="date"
              value={value.date}
              onChange={handleInputChange}
              onClick={handleDateClick}
              required
            />
          </label>

          <label>
            Start time
            <select name="startTime" value={value.startTime} onChange={handleInputChange} required>
              {timeSlots.map((slot) => (
                <option key={slot} value={slot}>
                  {slot}
                </option>
              ))}
            </select>
          </label>

          <label>
            Party size
            <select name="partySize" value={value.partySize} onChange={handleInputChange} required>
              {partySizeOptions.map((size) => (
                <option key={size} value={size}>
                  {size} {size === 1 ? 'guest' : 'guests'}
                </option>
              ))}
            </select>
          </label>

          <label>
            Duration
            <select name="duration" value={value.duration} onChange={handleInputChange} required>
              {durationOptions.map((mins) => (
                <option key={mins} value={mins}>
                  {mins >= 60 ? `${mins / 60 >= 1 && mins % 60 ? `${Math.floor(mins / 60)}.5h` : `${mins / 60}h`}` : `${mins}m`}
                </option>
              ))}
            </select>
          </label>

          <label>
            Zone
            <select name="zone" value={value.zone ?? ''} onChange={handleInputChange}>
              <option value="">Any zone</option>
              {zones.map((zone) => (
                <option key={zone} value={zone}>
                  {zone}
                </option>
              ))}
            </select>
          </label>

          <button type="submit" className="primary-button search-submit" disabled={isLoading}>
            Search
          </button>
        </div>

        <div className="search-preferences" role="group" aria-label="Preferences">
          <span className="preferences-label">Preferences</span>
          {featureOptions.map((option) => {
            const isActive = value.preferences.includes(option.value)
            return (
              <button
                key={option.value}
                type="button"
                className={`preference-tag${isActive ? ' active' : ''}`}
                aria-pressed={isActive}
                onClick={() => handlePreferenceChange(option.value)}
              >
                {option.label}
              </button>
            )
          })}
        </div>
      </form>
    </section>
  )
}
