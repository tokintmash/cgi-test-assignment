import { useCallback, useEffect, useRef } from 'react'
import type { TableRecommendation, TableCombination } from '../types'
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
  onVisibleIdsChange: (ids: Set<number>) => void
  onHover: (ids: Set<number>) => void
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
  onVisibleIdsChange,
  onHover,
}: RecommendationPanelProps) {
  const scrollRef = useRef<HTMLDivElement>(null)
  const visibleRef = useRef<Set<number>>(new Set())
  const emptySet = useRef(new Set<number>()).current

  const observerCallback = useCallback((entries: IntersectionObserverEntry[]) => {
    let changed = false
    for (const entry of entries) {
      const tableId = Number(entry.target.getAttribute('data-table-id'))
      if (Number.isNaN(tableId)) continue
      if (entry.isIntersecting && !visibleRef.current.has(tableId)) {
        visibleRef.current.add(tableId)
        changed = true
      } else if (!entry.isIntersecting && visibleRef.current.has(tableId)) {
        visibleRef.current.delete(tableId)
        changed = true
      }
    }
    if (changed) {
      onVisibleIdsChange(new Set(visibleRef.current))
    }
  }, [onVisibleIdsChange])

  useEffect(() => {
    const container = scrollRef.current
    if (!container) return

    const observer = new IntersectionObserver(observerCallback, {
      root: container,
      threshold: 0.1,
    })

    const items = container.querySelectorAll('[data-table-id]')
    items.forEach((item) => observer.observe(item))

    return () => observer.disconnect()
  }, [recommendations, combinations, observerCallback])

  // Reset visible IDs when recommendations change
  useEffect(() => {
    visibleRef.current = new Set()
  }, [recommendations])

  return (
    <section className="card-panel recommendation-panel" aria-label="Recommended tables">
      <h2 className="panel-title">Recommendations</h2>

      <div className="recommendation-scroll" ref={scrollRef}>
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
                <li
                  key={recommendation.tableId}
                  data-table-id={recommendation.tableId}
                  className={isSelected ? 'recommendation-item selected' : 'recommendation-item'}
                  onMouseEnter={() => onHover(new Set([recommendation.tableId]))}
                  onMouseLeave={() => onHover(emptySet)}
                >
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
                  <li
                    key={`${combo.tableId1}-${combo.tableId2}`}
                    className={isSelected ? 'recommendation-item combination-item selected' : 'recommendation-item combination-item'}
                    onMouseEnter={() => onHover(new Set([combo.tableId1, combo.tableId2]))}
                    onMouseLeave={() => onHover(emptySet)}
                  >
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
      </div>
    </section>
  )
}
