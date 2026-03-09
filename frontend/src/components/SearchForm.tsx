import type { ChangeEvent, FormEvent } from 'react'
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

  return (
    <section className="card-panel search-panel" aria-label="Search tables">
      <h2 className="panel-title">Search tables</h2>
      <form className="search-form" onSubmit={handleSubmit}>
        <div className="search-row search-row-main">
          <label>
            Date
            <input type="date" name="date" value={value.date} onChange={handleInputChange} required />
          </label>

          <label>
            Start time
            <input
              type="time"
              name="startTime"
              value={value.startTime}
              onChange={handleInputChange}
              required
            />
          </label>

          <label>
            Party size
            <input
              type="number"
              name="partySize"
              min={1}
              value={value.partySize}
              onChange={handleInputChange}
              required
            />
          </label>

          <label>
            Duration (minutes)
            <input
              type="number"
              name="duration"
              min={30}
              step={30}
              value={value.duration}
              onChange={handleInputChange}
              required
            />
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

        <fieldset className="search-preferences">
          <legend>Preferences</legend>
          {featureOptions.map((option) => (
            <label key={option.value} className="checkbox-label">
              <input
                type="checkbox"
                checked={value.preferences.includes(option.value)}
                onChange={() => handlePreferenceChange(option.value)}
              />
              <span>{option.label}</span>
            </label>
          ))}
        </fieldset>
      </form>
    </section>
  )
}
