import type { TableRecommendation } from '../types'
import '../styles/RecommendationPanel.css'

interface RecommendationPanelProps {
  recommendations: TableRecommendation[]
  isLoading: boolean
  hasSearched: boolean
  selectedTableId: number | null
  onSelect: (tableId: number) => void
  onBook: (tableId: number) => void
}

export function RecommendationPanel({
  recommendations,
  isLoading,
  hasSearched,
  selectedTableId,
  onSelect,
  onBook,
}: RecommendationPanelProps) {
  return (
    <section className="card-panel recommendation-panel" aria-label="Recommended tables">
      <h2 className="panel-title">Recommendations</h2>

      {!hasSearched && <p className="panel-state">Submit your search to see ranked suggestions.</p>}
      {isLoading && <p className="panel-state">Calculating best tables...</p>}

      {hasSearched && !isLoading && recommendations.length === 0 && (
        <p className="panel-state">No available tables match this party size and time slot.</p>
      )}

      {hasSearched && !isLoading && recommendations.length > 0 && (
        <ol className="recommendation-list">
          {recommendations.map((recommendation) => {
            const isSelected = selectedTableId === recommendation.tableId
            return (
              <li key={recommendation.tableId} className={isSelected ? 'recommendation-item selected' : 'recommendation-item'}>
                <button
                  type="button"
                  className="recommendation-summary"
                  onClick={() => onSelect(recommendation.tableId)}
                >
                  <div>
                    <h3>{recommendation.tableName}</h3>
                    <p className="recommendation-meta">
                      {recommendation.zone} · Seats: {recommendation.capacity}
                    </p>
                  </div>
                </button>

                <button type="button" className="secondary-button" onClick={() => onBook(recommendation.tableId)}>
                  Book this table
                </button>
              </li>
            )
          })}
        </ol>
      )}
    </section>
  )
}
