export type TimeRangeKey = 'all' | 'today' | '3d' | '7d'

export const TIME_RANGE_OPTIONS: Array<{ value: TimeRangeKey; label: string }> = [
  { value: 'all', label: '全部' },
  { value: 'today', label: '今天' },
  { value: '3d', label: '三天内' },
  { value: '7d', label: '七天内' },
]

export function normalizeTimeRange(value: unknown): TimeRangeKey {
  if (typeof value !== 'string') {
    return 'all'
  }
  return TIME_RANGE_OPTIONS.some((option) => option.value === value) ? (value as TimeRangeKey) : 'all'
}

export function buildTimeRangeQuery(timeRange: TimeRangeKey) {
  return timeRange === 'all' ? {} : { timeRange }
}
