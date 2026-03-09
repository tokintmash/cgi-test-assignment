export type TableFeature = 'WINDOW' | 'PRIVATE' | 'ACCESSIBLE' | 'NEAR_PLAY_AREA'

export type TableAvailability = 'available' | 'reserved'

export interface RestaurantTable {
  id: number
  name: string
  capacity: number
  zone: string
  posX: number
  posY: number
  width: number
  height: number
  shape: 'round' | 'rectangle' | string
  features: TableFeature[]
}

export interface ScoreBreakdown {
  efficiency: number
  preferenceMatch: number
  zoneMatch: number
  base: number
}

export interface TableRecommendation {
  tableId: number
  tableName: string
  zone: string
  capacity: number
  features: TableFeature[]
  score: number
  scoreBreakdown: ScoreBreakdown
  posX: number
  posY: number
  width: number
  height: number
  shape: string
}

export interface TableStatus {
  tableId: number
  tableName: string
  zone: string
  capacity: number
  status: TableAvailability
  features: TableFeature[]
}

export interface SearchRequest {
  date: string
  startTime: string
  partySize: number
  duration: number
  zone?: string
  preferences: TableFeature[]
}

export interface SearchResponse {
  recommendations: TableRecommendation[]
  allTables: TableStatus[]
}

export interface ReservationRequest {
  tableId: number
  date: string
  startTime: string
  duration: number
  partySize: number
  guestName: string
}

export interface ReservationResponse {
  id: number
  tableId: number
  date: string
  startTime: string
  endTime: string
  partySize: number
  guestName: string
}

export interface ResetReservationsResponse {
  message: string
}
