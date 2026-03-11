import type { TableRecommendation, TableCombination } from '../types'
import { featureLabel } from '../utils/featureLabels'
import '../styles/RecommendationPanel.css'

interface RecommendationPanelProps {
  recommendations: TableRecommendation[]
  combinations: TableCombination[]
  isLoading: boolean
  hasSearched: boolean
  selectedTableId: number | null
  selectedCombination: TableCombination | null
  onSelect: (tableId: number) => void
  onBook: (tableId: number) => void
  onBookCombination: (combination: TableCombination) => void
  onSelectCombination: (combination: TableCombination | null) => void
}

export function RecommendationPanel({
  recommendations,
  combinations,
  isLoading,
  hasSearched,
  selectedTableId,
  selectedCombination,
  onSelect,
  onBook,
  onBookCombination,
  onSelectCombination,
}: RecommendationPanelProps) {
  return (
    <section className="card-panel recommendation-panel" aria-label="Recommended tables">
      <h2 className="panel-title">Recommendations</h2>

      {!hasSearched && <p className="panel-state">Submit your search to see ranked suggestions.</p>}
      {isLoading && <p className="panel-state">Calculating best tables...</p>}

      {hasSearched && !isLoading && recommendations.length === 0 && combinations.length === 0 && (
        <p className="panel-state">No matches :(</p>
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

      {hasSearched && !isLoading && combinations.length > 0 && (
        <div className="combinations-section">
          <h3 className="combinations-title">Combined tables</h3>
          <ol className="recommendation-list">
            {combinations.map((combo) => {
              const isSelected = selectedCombination?.tableId1 === combo.tableId1 && selectedCombination?.tableId2 === combo.tableId2
              return (
                <li key={`${combo.tableId1}-${combo.tableId2}`} className={isSelected ? 'recommendation-item combination-item selected' : 'recommendation-item combination-item'}>
                  <button
                    type="button"
                    className="recommendation-summary"
                    onClick={() => onSelectCombination(isSelected ? null : combo)}
                  >
                    <div>
                      <h3>{combo.tableName1} + {combo.tableName2}</h3>
                      <p className="recommendation-meta">
                        {combo.zone} · Combined seats: {combo.combinedCapacity}
                      </p>
                    </div>
                  </button>
                  <button type="button" className="secondary-button" onClick={() => onBookCombination(combo)}>
                    Book combined tables
                  </button>
                </li>
              )
            })}
          </ol>
        </div>
      )}
    </section>
  )
}
