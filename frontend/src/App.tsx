import { useEffect, useMemo, useState } from 'react'
import { reservationApi } from './api/reservationApi'
import { BookingDialog } from './components/BookingDialog'
import { FloorPlan } from './components/FloorPlan'
import { RecommendationPanel } from './components/RecommendationPanel'
import { SearchForm } from './components/SearchForm'
import type {
  SearchRequest,
  TableAvailability,
  TableStatus,
  TableRecommendation,
  RestaurantTable,
} from './types'
import './styles/app.css'

function padTwo(value: number): string {
  return value.toString().padStart(2, '0')
}

function formatDate(date: Date): string {
  const year = date.getFullYear()
  const month = padTwo(date.getMonth() + 1)
  const day = padTwo(date.getDate())
  return `${year}-${month}-${day}`
}

function formatTime(date: Date): string {
  return `${padTwo(date.getHours())}:${padTwo(date.getMinutes())}`
}

function nextHalfHourTime(): string {
  const now = new Date()
  now.setMinutes(now.getMinutes() + 30)

  const minutes = now.getMinutes()
  if (minutes === 0 || minutes === 30) {
    now.setSeconds(0, 0)
    return formatTime(now)
  }

  if (minutes < 30) {
    now.setMinutes(30, 0, 0)
    return formatTime(now)
  }

  now.setHours(now.getHours() + 1)
  now.setMinutes(0, 0, 0)
  return formatTime(now)
}

function createDefaultSearchRequest(): SearchRequest {
  return {
    date: formatDate(new Date()),
    startTime: nextHalfHourTime(),
    partySize: 2,
    duration: 120,
    zone: '',
    preferences: [],
  }
}

function toStatusMap(allTables: TableStatus[]): Record<number, TableAvailability> {
  return allTables.reduce<Record<number, TableAvailability>>((acc, tableStatus) => {
    acc[tableStatus.tableId] = tableStatus.status
    return acc
  }, {})
}

function errorMessage(error: unknown, fallback: string): string {
  if (error instanceof Error && error.message) {
    return error.message
  }
  return fallback
}

function App() {
  const [tables, setTables] = useState<RestaurantTable[]>([])
  const [tablesLoading, setTablesLoading] = useState(false)
  const [tablesError, setTablesError] = useState<string | null>(null)

  const [searchRequest, setSearchRequest] = useState<SearchRequest>(() => createDefaultSearchRequest())
  const [searchLoading, setSearchLoading] = useState(false)
  const [searchError, setSearchError] = useState<string | null>(null)
  const [hasSearched, setHasSearched] = useState(false)
  const [autoSearchAttempted, setAutoSearchAttempted] = useState(false)

  const [recommendations, setRecommendations] = useState<TableRecommendation[]>([])
  const [statusByTableId, setStatusByTableId] = useState<Record<number, TableAvailability>>({})

  const [selectedTableId, setSelectedTableId] = useState<number | null>(null)
  const [bookingOpen, setBookingOpen] = useState(false)
  const [bookingSession, setBookingSession] = useState(0)
  const [bookingLoading, setBookingLoading] = useState(false)
  const [bookingError, setBookingError] = useState<string | null>(null)

  const [successMessage, setSuccessMessage] = useState<string | null>(null)

  const zones = useMemo(() => Array.from(new Set(tables.map((table) => table.zone))).sort(), [tables])

  const recommendedIds = useMemo(
    () => new Set(recommendations.map((recommendation) => recommendation.tableId)),
    [recommendations],
  )

  const tableById = useMemo(
    () =>
      tables.reduce<Map<number, RestaurantTable>>((acc, table) => {
        acc.set(table.id, table)
        return acc
      }, new Map()),
    [tables],
  )

  const selectedTable = selectedTableId !== null ? tableById.get(selectedTableId) ?? null : null

  useEffect(() => {
    const timeout = successMessage
      ? window.setTimeout(() => {
          setSuccessMessage(null)
        }, 3500)
      : undefined

    return () => {
      if (timeout !== undefined) {
        window.clearTimeout(timeout)
      }
    }
  }, [successMessage])

  useEffect(() => {
    let mounted = true

    const fetchTables = async () => {
      setTablesLoading(true)
      setTablesError(null)

      try {
        const allTables = await reservationApi.getAllTables()
        if (!mounted) {
          return
        }
        setTables(allTables)
      } catch (error: unknown) {
        if (!mounted) {
          return
        }
        setTablesError(errorMessage(error, 'Failed to load tables.'))
      } finally {
        if (mounted) {
          setTablesLoading(false)
        }
      }
    }

    void fetchTables()

    return () => {
      mounted = false
    }
  }, [])

  const runSearch = async (criteria: SearchRequest) => {
    setSearchLoading(true)
    setSearchError(null)

    try {
      const response = await reservationApi.searchTables(criteria)
      setRecommendations(response.recommendations.slice(0, 5))
      setStatusByTableId(toStatusMap(response.allTables))
      setHasSearched(true)

      setSelectedTableId((current) => {
        if (current === null) {
          return null
        }
        const refreshedStatus = response.allTables.find((table) => table.tableId === current)?.status
        return refreshedStatus === 'reserved' || refreshedStatus === undefined ? null : current
      })
    } catch (error: unknown) {
      setSearchError(errorMessage(error, 'Search failed. Please try again.'))
    } finally {
      setSearchLoading(false)
    }
  }

  useEffect(() => {
    if (tables.length === 0 || autoSearchAttempted) {
      return
    }

    setAutoSearchAttempted(true)
    void runSearch(searchRequest)
  }, [autoSearchAttempted, searchRequest, tables.length])

  const handleSearchSubmit = (criteria: SearchRequest) => {
    setSearchRequest(criteria)
    setSelectedTableId(null)
    setBookingOpen(false)
    setBookingError(null)
    void runSearch(criteria)
  }

  const openBooking = (tableId: number) => {
    if (statusByTableId[tableId] !== 'available') {
      return
    }
    setBookingSession((current) => current + 1)
    setSelectedTableId(tableId)
    setBookingError(null)
    setBookingOpen(true)
  }

  const closeBooking = () => {
    setBookingOpen(false)
    setBookingError(null)
  }

  const handleBookingConfirm = async (payload: { guestName: string; duration: number }) => {
    if (selectedTableId === null) {
      return
    }

    if (payload.guestName.trim().length < 2) {
      setBookingError('Guest name must be at least 2 characters.')
      return
    }

    setBookingLoading(true)
    setBookingError(null)

    try {
      await reservationApi.createReservation({
        tableId: selectedTableId,
        date: searchRequest.date,
        startTime: searchRequest.startTime,
        duration: payload.duration,
        partySize: searchRequest.partySize,
        guestName: payload.guestName.trim(),
      })

      setSuccessMessage(`Reservation confirmed for ${payload.guestName.trim()}.`)
      setBookingOpen(false)
      setSelectedTableId(null)
      await runSearch(searchRequest)
    } catch (error: unknown) {
      setBookingError(errorMessage(error, 'Unable to create reservation.'))
    } finally {
      setBookingLoading(false)
    }
  }

  return (
    <div className="app-shell">
      <header className="app-header">
        <p className="eyebrow">Smart reservation</p>
        <h1>Restaurant table recommender</h1>
        <p>Search by party, time, and preferences, then click a table on the map or in the ranking to book.</p>
      </header>

      <SearchForm
        value={searchRequest}
        zones={zones}
        isLoading={searchLoading}
        onChange={setSearchRequest}
        onSubmit={handleSearchSubmit}
      />

      {successMessage && <p className="status-banner success">{successMessage}</p>}
      {tablesError && <p className="status-banner error">{tablesError}</p>}
      {searchError && <p className="status-banner error">{searchError}</p>}

      <main className="layout-grid">
        <div className="left-column">
          <FloorPlan
            tables={tables}
            statusByTableId={statusByTableId}
            recommendedIds={recommendedIds}
            selectedTableId={selectedTableId}
            isLoading={searchLoading || tablesLoading}
            onSelectTable={openBooking}
          />
        </div>

        <div className="right-column">
          <RecommendationPanel
            recommendations={recommendations}
            isLoading={searchLoading}
            hasSearched={hasSearched}
            selectedTableId={selectedTableId}
            onSelect={setSelectedTableId}
            onBook={openBooking}
          />
        </div>
      </main>

      <BookingDialog
        key={bookingSession}
        isOpen={bookingOpen}
        table={selectedTable}
        criteria={searchRequest}
        isSubmitting={bookingLoading}
        errorMessage={bookingError}
        onClose={closeBooking}
        onConfirm={handleBookingConfirm}
      />
    </div>
  )
}

export default App
