import type {
  ReservationRequest,
  ReservationResponse,
  ResetReservationsResponse,
  RestaurantTable,
  SearchRequest,
  SearchResponse,
} from '../types'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080'

class ApiError extends Error {
  status: number

  constructor(message: string, status: number) {
    super(message)
    this.name = 'ApiError'
    this.status = status
  }
}

async function fetchJson<T>(path: string, options?: RequestInit): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    headers: {
      'Content-Type': 'application/json',
      ...(options?.headers ?? {}),
    },
    ...options,
  })

  const raw = await response.text()
  const payload = raw ? (JSON.parse(raw) as unknown) : undefined

  if (!response.ok) {
    const message = extractErrorMessage(payload) ?? `Request failed with status ${response.status}`
    throw new ApiError(message, response.status)
  }

  return payload as T
}

function extractErrorMessage(payload: unknown): string | null {
  if (typeof payload !== 'object' || payload === null) {
    return null
  }

  const maybeMap = payload as Record<string, unknown>
  if (typeof maybeMap.error === 'string') {
    return maybeMap.error
  }

  if (typeof maybeMap.message === 'string') {
    return maybeMap.message
  }

  return null
}

export const reservationApi = {
  ApiError,

  getAllTables: () => fetchJson<RestaurantTable[]>('/api/tables', { method: 'GET' }),

  searchTables: (request: SearchRequest) =>
    fetchJson<SearchResponse>('/api/tables/search', {
      method: 'POST',
      body: JSON.stringify(request),
    }),

  createReservation: (request: ReservationRequest) =>
    fetchJson<ReservationResponse>('/api/reservations', {
      method: 'POST',
      body: JSON.stringify(request),
    }),

  cancelReservation: (reservationId: number) =>
    fetchJson<{ message: string }>(`/api/reservations/${reservationId}`, {
      method: 'DELETE',
    }),

  resetReservations: () =>
    fetchJson<ResetReservationsResponse>('/api/reservations/reset', {
      method: 'POST',
    }),
}
