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

export interface WeatherData {
  temperatureC: number
  windSpeedKmh: number
}

export interface ScoreBreakdown {
  efficiency: number
  preferenceMatch: number
  zoneMatch: number
  weatherPenalty: number
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

export interface TableCombination {
  tableId1: number
  tableName1: string
  posX1: number
  posY1: number
  width1: number
  height1: number
  shape1: string
  tableId2: number
  tableName2: string
  posX2: number
  posY2: number
  width2: number
  height2: number
  shape2: string
  zone: string
  combinedCapacity: number
  combinedFeatures: TableFeature[]
  score: number
  scoreBreakdown: ScoreBreakdown
}

export interface TableStatus {
  tableId: number
  tableName: string
  zone: string
  capacity: number
  status: TableAvailability
  features: TableFeature[]
  reservationId: number | null
  guestName: string | null
  reservationStart: string | null
  reservationEnd: string | null
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
  combinations: TableCombination[]
  allTables: TableStatus[]
  weather: WeatherData | null
  weatherWarning: string | null
}

export interface ReservationRequest {
  tableId?: number
  tableIds?: number[]
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
