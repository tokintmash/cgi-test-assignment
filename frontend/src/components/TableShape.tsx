import type { KeyboardEvent } from 'react'
import type { RestaurantTable } from '../types'

export type TableVisualState = 'default' | 'available' | 'reserved' | 'recommended' | 'selected'

interface TableShapeProps {
  table: RestaurantTable
  visualState: TableVisualState
  selectable: boolean
  statusLabel: string
  onSelect: (tableId: number) => void
}

export function TableShape({ table, visualState, selectable, statusLabel, onSelect }: TableShapeProps) {
  const classes = ['table-shape', `table-${visualState}`, selectable ? 'table-clickable' : 'table-disabled'].join(
    ' ',
  )

  const handleSelect = () => {
    if (selectable) {
      onSelect(table.id)
    }
  }

  const handleKeyDown = (event: KeyboardEvent<SVGGElement>) => {
    if (!selectable) {
      return
    }

    if (event.key === 'Enter' || event.key === ' ') {
      event.preventDefault()
      onSelect(table.id)
    }
  }

  return (
    <g
      className={classes}
      onClick={handleSelect}
      onKeyDown={handleKeyDown}
      tabIndex={selectable ? 0 : -1}
      role={selectable ? 'button' : 'img'}
      aria-label={`${table.name}, ${table.capacity} seats, ${statusLabel}`}
      aria-disabled={!selectable}
    >
      <title>{`${table.name} · ${table.capacity} seats · ${statusLabel}`}</title>
      <rect x={table.posX} y={table.posY} width={table.width} height={table.height} rx={12} />
      <text x={table.posX + table.width / 2} y={table.posY + table.height / 2 + 4} textAnchor="middle">
        {table.name}
      </text>
    </g>
  )
}
