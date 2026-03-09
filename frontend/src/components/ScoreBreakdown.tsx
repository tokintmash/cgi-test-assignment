import type { ScoreBreakdown as ScoreBreakdownModel } from '../types'
import '../styles/ScoreBreakdown.css'

interface ScoreBreakdownProps {
  breakdown: ScoreBreakdownModel
}

function toPercent(score: number): number {
  return Math.round(score * 100)
}

export function ScoreBreakdown({ breakdown }: ScoreBreakdownProps) {
  const items = [
    { label: 'Efficiency', value: breakdown.efficiency },
    { label: 'Preferences', value: breakdown.preferenceMatch },
    { label: 'Zone', value: breakdown.zoneMatch },
    { label: 'Base', value: breakdown.base },
  ]

  return (
    <ul className="score-breakdown" aria-label="Score breakdown">
      {items.map((item) => (
        <li key={item.label}>
          <span>{item.label}</span>
          <div className="score-track" aria-hidden="true">
            <div className="score-fill" style={{ width: `${toPercent(item.value)}%` }} />
          </div>
          <strong>{toPercent(item.value)}%</strong>
        </li>
      ))}
    </ul>
  )
}
