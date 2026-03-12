import { useCallback, useEffect, useMemo, useState } from 'react'
import { reservationApi } from './api/reservationApi'
import { BookingDialog } from './components/BookingDialog'
import { ConfirmDialog } from './components/ConfirmDialog'
import { ReservationDetailDialog } from './components/ReservationDetailDialog'
import { FloorPlan } from './components/FloorPlan'
import { RecommendationPanel } from './components/RecommendationPanel'
import { SearchForm } from './components/SearchForm'
import type {
  SearchRequest,
  TableAvailability,
  TableStatus as TableStatusType,
  TableRecommendation,
  TableCombination,
  RestaurantTable,
  WeatherData,
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

function toStatusMap(allTables: TableStatusType[]): Record<number, TableAvailability> {
  return allTables.reduce<Record<number, TableAvailability>>((acc, tableStatus) => {
    acc[tableStatus.tableId] = tableStatus.status
    return acc
  }, {})
}

function toTableStatusMap(allTables: TableStatusType[]): Record<number, TableStatusType> {
  return allTables.reduce<Record<number, TableStatusType>>((acc, tableStatus) => {
    acc[tableStatus.tableId] = tableStatus
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
  const [combinations, setCombinations] = useState<TableCombination[]>([])
  const [selectedCombination, setSelectedCombination] = useState<TableCombination | null>(null)
  const [statusByTableId, setStatusByTableId] = useState<Record<number, TableAvailability>>({})

  const [selectedTableId, setSelectedTableId] = useState<number | null>(null)
  const [bookingOpen, setBookingOpen] = useState(false)
  const [bookingSession, setBookingSession] = useState(0)
  const [bookingLoading, setBookingLoading] = useState(false)
  const [bookingError, setBookingError] = useState<string | null>(null)
  const [bookingSuccess, setBookingSuccess] = useState<string | null>(null)

  const [tableStatusMap, setTableStatusMap] = useState<Record<number, TableStatusType>>({})

  const [detailOpen, setDetailOpen] = useState(false)
  const [detailTableId, setDetailTableId] = useState<number | null>(null)
  const [cancelLoading, setCancelLoading] = useState(false)
  const [cancelError, setCancelError] = useState<string | null>(null)
  const [cancelSuccess, setCancelSuccess] = useState<string | null>(null)

  const [resetDialogOpen, setResetDialogOpen] = useState(false)
  const [resetting, setResetting] = useState(false)
  const [resetResult, setResetResult] = useState<string | null>(null)

  const [visibleRecommendedIds, setVisibleRecommendedIds] = useState<Set<number>>(new Set())
  const [hoveredTableIds, setHoveredTableIds] = useState<Set<number>>(new Set())

  const [weather, setWeather] = useState<WeatherData | null>(null)
  const [weatherWarning, setWeatherWarning] = useState<string | null>(null)

  const zones = useMemo(() => Array.from(new Set(tables.map((table) => table.zone))).filter((zone) => zone !== 'Window').sort(), [tables])

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

  // Floor plan only highlights tables visible in the scrollable recommendation list
  const floorPlanHighlightIds = hasSearched ? visibleRecommendedIds : recommendedIds

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

    const fetchWeather = async () => {
      try {
        const data = await reservationApi.getWeather()
        if (mounted) {
          setWeather(data)
        }
      } catch {
        // Weather is optional — hide badge on failure
      }
    }

    void fetchWeather()

    return () => {
      mounted = false
    }
  }, [])

  const runSearch = useCallback(async (criteria: SearchRequest) => {
    setSearchLoading(true)
    setSearchError(null)

    try {
      const response = await reservationApi.searchTables(criteria)
      setRecommendations(response.recommendations)
      setCombinations(response.combinations ?? [])
      setStatusByTableId(toStatusMap(response.allTables))
      setTableStatusMap(toTableStatusMap(response.allTables))
      setHasSearched(true)
      if (response.weather) {
        setWeather(response.weather)
      }
      setWeatherWarning(response.weatherWarning ?? null)

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
  }, [])

  useEffect(() => {
    if (tables.length === 0 || autoSearchAttempted) {
      return
    }

    setAutoSearchAttempted(true)
    void runSearch(searchRequest)
  }, [autoSearchAttempted, searchRequest, tables.length])

  const handleSearchSubmit = (criteria: SearchRequest) => {
    const searchDateTime = new Date(`${criteria.date}T${criteria.startTime}`)
    if (searchDateTime < new Date()) {
      setSearchError('Cannot search for past dates and times.')
      return
    }

    setSearchRequest(criteria)
    setSearchError(null)
    setSelectedTableId(null)
    setSelectedCombination(null)
    setBookingOpen(false)
    setBookingError(null)
    setBookingSuccess(null)
    void runSearch(criteria)
  }

  const openCombinationBooking = (combination: TableCombination) => {
    setSelectedCombination(combination)
    setSelectedTableId(null)
    setBookingSession((current) => current + 1)
    setBookingError(null)
    setBookingSuccess(null)
    setBookingOpen(true)
  }

  const openBooking = (tableId: number) => {
    if (statusByTableId[tableId] === 'reserved') {
      setDetailTableId(tableId)
      setCancelError(null)
      setCancelSuccess(null)
      setDetailOpen(true)
      return
    }
    if (statusByTableId[tableId] !== 'available') {
      return
    }
    setSelectedCombination(null)
    setBookingSession((current) => current + 1)
    setSelectedTableId(tableId)
    setBookingError(null)
    setBookingSuccess(null)
    setBookingOpen(true)
  }

  const handleResetClick = () => {
    setResetResult(null)
    setResetDialogOpen(true)
  }

  const handleResetConfirm = async () => {
    setResetting(true)
    try {
      await reservationApi.resetReservations()
      setResetResult('Reservations have been reset.')
      setSelectedTableId(null)
      setSelectedCombination(null)
      setBookingOpen(false)
      await runSearch(searchRequest)
    } catch (error: unknown) {
      setResetResult(errorMessage(error, 'Failed to reset reservations.'))
    } finally {
      setResetting(false)
    }
  }

  const closeDetail = () => {
    setDetailOpen(false)
    setCancelError(null)
    setCancelSuccess(null)
  }

  const handleCancelReservation = async () => {
    if (detailTableId === null) return
    const status = tableStatusMap[detailTableId]
    if (!status?.reservationId) return

    setCancelLoading(true)
    setCancelError(null)

    try {
      await reservationApi.cancelReservation(status.reservationId)
      setCancelSuccess('Reservation has been cancelled.')
      await runSearch(searchRequest)
    } catch (error: unknown) {
      setCancelError(errorMessage(error, 'Failed to cancel reservation.'))
    } finally {
      setCancelLoading(false)
    }
  }

  const closeResetDialog = () => {
    setResetDialogOpen(false)
    setResetResult(null)
  }

  const closeBooking = () => {
    setBookingOpen(false)
    setBookingError(null)
    setBookingSuccess(null)
    setSelectedCombination(null)
  }

  const handleBookingConfirm = async (payload: { guestName: string; duration: number }) => {
    if (selectedTableId === null && selectedCombination === null) {
      return
    }

    if (payload.guestName.trim().length < 2) {
      setBookingError('Guest name must be at least 2 characters.')
      return
    }

    setBookingLoading(true)
    setBookingError(null)

    try {
      const baseRequest = {
        date: searchRequest.date,
        startTime: searchRequest.startTime,
        duration: payload.duration,
        partySize: searchRequest.partySize,
        guestName: payload.guestName.trim(),
      }

      if (selectedCombination) {
        await reservationApi.createReservation({
          ...baseRequest,
          tableIds: [selectedCombination.tableId1, selectedCombination.tableId2],
        })
        setBookingSuccess(`Reservation confirmed for ${payload.guestName.trim()} (${selectedCombination.tableName1} + ${selectedCombination.tableName2}).`)
      } else {
        await reservationApi.createReservation({
          ...baseRequest,
          tableId: selectedTableId!,
        })
        setBookingSuccess(`Reservation confirmed for ${payload.guestName.trim()}.`)
      }

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
        <div className="header-top-row">
          <div>
            <p className="eyebrow">Smart reservation</p>
            <h1>Restaurant table recommender</h1>
          </div>
          <div className="header-actions">
            {weather && (
              <span className="weather-badge">
                {Math.round(weather.temperatureC)}°C · {Math.round(weather.windSpeedKmh)} km/h
              </span>
            )}
            <button
              className="btn-reset"
              disabled={resetting}
              onClick={handleResetClick}
            >
              Reset reservations
            </button>
          </div>
        </div>
        <p>Search by party, time, and preferences, then click a table on the map or in the ranking to book.</p>
      </header>

      <SearchForm
        value={searchRequest}
        zones={zones}
        isLoading={searchLoading}
        onChange={setSearchRequest}
        onSubmit={handleSearchSubmit}
      />

      {tablesError && <p className="status-banner error">{tablesError}</p>}
      {searchError && <p className="status-banner error">{searchError}</p>}
      {weatherWarning && <p className="status-banner weather-warning">{weatherWarning}</p>}

      <main className="layout-grid">
        <div className="left-column">
          <FloorPlan
            tables={tables}
            statusByTableId={statusByTableId}
            recommendedIds={floorPlanHighlightIds}
            hoveredTableIds={hoveredTableIds}
            selectedTableId={selectedTableId}
            selectedCombination={selectedCombination}
            isLoading={searchLoading || tablesLoading}
            onSelectTable={openBooking}
          />
        </div>

        <div className="right-column">
          <RecommendationPanel
            recommendations={recommendations}
            combinations={combinations}
            isLoading={searchLoading}
            hasSearched={hasSearched}
            selectedTableId={selectedTableId}
            selectedCombination={selectedCombination}
            onSelect={setSelectedTableId}
            onBook={openBooking}
            onBookCombination={openCombinationBooking}
            onSelectCombination={setSelectedCombination}
            onVisibleIdsChange={setVisibleRecommendedIds}
            onHover={setHoveredTableIds}
          />
        </div>
      </main>

      <BookingDialog
        key={bookingSession}
        isOpen={bookingOpen}
        table={selectedTable}
        combination={selectedCombination}
        criteria={searchRequest}
        isSubmitting={bookingLoading}
        errorMessage={bookingError}
        successMessage={bookingSuccess}
        onClose={closeBooking}
        onConfirm={handleBookingConfirm}
      />

      <ReservationDetailDialog
        isOpen={detailOpen}
        table={detailTableId !== null ? tableById.get(detailTableId) ?? null : null}
        tableStatus={detailTableId !== null ? tableStatusMap[detailTableId] ?? null : null}
        isCancelling={cancelLoading}
        errorMessage={cancelError}
        successMessage={cancelSuccess}
        onClose={closeDetail}
        onCancel={() => void handleCancelReservation()}
      />

      <ConfirmDialog
        isOpen={resetDialogOpen}
        title="Reset reservations"
        message="This will delete all current reservations and generate new random ones. Continue?"
        confirmLabel="Yes, reset"
        isLoading={resetting}
        result={resetResult}
        onConfirm={() => void handleResetConfirm()}
        onClose={closeResetDialog}
      />
    </div>
  )
}

export default App
