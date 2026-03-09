import type { TableFeature } from '../types'

export const featureOptions: { label: string; value: TableFeature }[] = [
  { value: 'WINDOW', label: 'Window' },
  { value: 'PRIVATE', label: 'Private area' },
  { value: 'ACCESSIBLE', label: 'Accessible' },
  { value: 'NEAR_PLAY_AREA', label: 'Near play area' },
]

export function featureLabel(feature: TableFeature): string {
  const option = featureOptions.find((item) => item.value === feature)
  return option?.label ?? feature
}
