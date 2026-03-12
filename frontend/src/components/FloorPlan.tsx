import { useMemo, useRef, useState } from 'react'
import type { TableAvailability, RestaurantTable, TableCombination } from '../types'
import { featureLabel } from '../utils/featureLabels'
import { TableShape, type TableVisualState } from './TableShape'
import '../styles/FloorPlan.css'

interface FloorPlanProps {
  tables: RestaurantTable[]
  statusByTableId: Record<number, TableAvailability>
  recommendedIds: Set<number>
  hoveredTableIds: Set<number>
  selectedTableId: number | null
  selectedCombination: TableCombination | null
  isLoading: boolean
  onSelectTable: (tableId: number) => void
}

interface TooltipInfo {
  tableId: number
  x: number
  y: number
  flipDown: boolean
}

function toVisualState(
  tableId: number,
  statusByTableId: Record<number, TableAvailability>,
  recommendedIds: Set<number>,
  hoveredTableIds: Set<number>,
  selectedTableId: number | null,
  selectedCombination: TableCombination | null,
): TableVisualState {
  if (selectedTableId === tableId) {
    return 'selected'
  }

  if (selectedCombination && (selectedCombination.tableId1 === tableId || selectedCombination.tableId2 === tableId)) {
    return 'selected'
  }

  if (hoveredTableIds.has(tableId)) {
    return 'hovered'
  }

  const status = statusByTableId[tableId]
  if (status === 'reserved') {
    return 'reserved'
  }

  if (recommendedIds.has(tableId)) {
    return 'recommended'
  }

  if (status === 'available') {
    return 'available'
  }

  return 'default'
}

function statusLabelForState(state: TableVisualState): string {
  switch (state) {
    case 'selected':
      return 'Selected'
    case 'hovered':
      return 'Recommended'
    case 'reserved':
      return 'Reserved'
    case 'recommended':
      return 'Recommended'
    case 'available':
      return 'Available'
    default:
      return 'Not searched yet'
  }
}

export function FloorPlan({
  tables,
  statusByTableId,
  recommendedIds,
  hoveredTableIds,
  selectedTableId,
  selectedCombination,
  isLoading,
  onSelectTable,
}: FloorPlanProps) {
  const [tooltip, setTooltip] = useState<TooltipInfo | null>(null)
  const svgRef = useRef<SVGSVGElement>(null)

  const tableById = useMemo(
    () => new Map(tables.map((t) => [t.id, t])),
    [tables],
  )

  const handleMouseEnter = (tableId: number) => {
    const table = tableById.get(tableId)
    if (!table || !svgRef.current) return
    const svg = svgRef.current
    const ctm = svg.getScreenCTM()
    if (!ctm) return
    const wrapper = svg.parentElement
    if (!wrapper) return
    const wrapperRect = wrapper.getBoundingClientRect()

    const topPoint = svg.createSVGPoint()
    topPoint.x = table.posX + table.width / 2
    topPoint.y = table.posY
    const screenTop = topPoint.matrixTransform(ctm)
    const relY = screenTop.y - wrapperRect.top
    const flipDown = relY < 70

    const anchorPoint = svg.createSVGPoint()
    anchorPoint.x = table.posX + table.width / 2
    anchorPoint.y = flipDown ? table.posY + table.height : table.posY
    const screenAnchor = anchorPoint.matrixTransform(ctm)

    setTooltip({
      tableId,
      x: screenAnchor.x - wrapperRect.left,
      y: screenAnchor.y - wrapperRect.top,
      flipDown,
    })
  }

  const handleMouseLeave = () => setTooltip(null)

  const tooltipTable = tooltip ? tableById.get(tooltip.tableId) : null
  const tooltipState = tooltipTable
    ? toVisualState(tooltipTable.id, statusByTableId, recommendedIds, hoveredTableIds, selectedTableId, selectedCombination)
    : null

  return (
    <section className="card-panel floor-plan-panel" aria-label="Interactive floor plan">
      <div className="panel-header-row">
        <h2 className="panel-title">Floor plan</h2>
        <div className="legend" aria-label="Table color legend">
          <span className="legend-item">
            <i className="swatch swatch-available" /> Available
          </span>
          <span className="legend-item">
            <i className="swatch swatch-reserved" /> Reserved
          </span>
          <span className="legend-item">
            <i className="swatch swatch-recommended" /> Recommended
          </span>
          <span className="legend-item">
            <i className="swatch swatch-selected" /> Selected
          </span>
        </div>
      </div>

      <div className="svg-wrapper" role="img" aria-label="Restaurant floor plan with table availability">
        {isLoading && <div className="plan-overlay">Refreshing availability...</div>}
        <svg ref={svgRef} viewBox="0 -25 700 585" className="floor-svg" preserveAspectRatio="xMidYMid meet">
          <g className="zone zone-main">
            <text x={185} y={15} className="zone-label">
              Main Hall
            </text>
            <text x={500} y={370} className="zone-label" textAnchor="middle">
              PLAY AREA
            </text>
            <text x={60} y={385} className="zone-label" textAnchor="middle">
              ENTRANCE
            </text>
          </g>
          <g className="zone zone-private">
            <text x={520} y={15} className="zone-label">
              Private
            </text>
          </g>
          <g className="zone zone-terrace">
            <text x={30} y={470} className="zone-label">
              Terrace
            </text>
          </g>

          {/* Walls */}
          <g className="walls">
            {/* Vertical wall between Main Hall and Private */}
            <line x1={500} y1={-30} x2={500} y2={310} stroke="#949494" strokeWidth={4} />
            {/* Horizontal walls separating Private tables */}
            <line x1={576} y1={103} x2={700} y2={103} stroke="#949494" strokeWidth={4} />
            <line x1={576} y1={201} x2={700} y2={201} stroke="#949494" strokeWidth={4} />
            <line x1={576} y1={310} x2={700} y2={310} stroke="#949494" strokeWidth={4} />
            {/* Horizontal walls separating Terrace and the rest HUMAN */}
            <line x1={0} y1={450} x2={150} y2={450} stroke="#949494" strokeWidth={4} />
            <line x1={240} y1={450} x2={700} y2={450} stroke="#949494" strokeWidth={4} />
            {/* Window (top wall, glass) HUMAN */}
            <line x1={250} y1={-20} x2={400} y2={-20} stroke="#7EC8E3" strokeWidth={3} />
            {/* Window (left wall, glass) */}
            <line x1={2} y1={10} x2={2} y2={290} stroke="#7EC8E3" strokeWidth={3} />
            {/* Entrance (gap between Window and Terrace zones) */}
            <line x1={2} y1={330} x2={2} y2={430} stroke="#D4915E" strokeWidth={3} />
          </g>

          {tables.map((table) => {
            const visualState = toVisualState(table.id, statusByTableId, recommendedIds, hoveredTableIds, selectedTableId, selectedCombination)
            const selectable = statusByTableId[table.id] !== 'reserved' && statusByTableId[table.id] !== undefined

            return (
              <TableShape
                key={table.id}
                table={table}
                visualState={visualState}
                selectable={selectable}
                statusLabel={statusLabelForState(visualState)}
                onSelect={onSelectTable}
                onMouseEnter={handleMouseEnter}
                onMouseLeave={handleMouseLeave}
              />
            )
          })}

        </svg>

        {tooltip && tooltipTable && tooltipState && (
          <div
            className={`table-tooltip${tooltip.flipDown ? ' tooltip-below' : ''}`}
            style={{ left: tooltip.x, top: tooltip.y }}
          >
            <strong>{tooltipTable.name}</strong>
            <span>{tooltipTable.capacity} seats · {tooltipTable.zone}</span>
            {tooltipTable.features.length > 0 && (
              <span className="tooltip-features">
                {tooltipTable.features.map(featureLabel).join(', ')}
              </span>
            )}
            <span className={`tooltip-status tooltip-status-${tooltipState}`}>
              {statusLabelForState(tooltipState)}
            </span>
          </div>
        )}
      </div>
    </section>
  )
}
