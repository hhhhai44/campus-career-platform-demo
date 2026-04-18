type RawPageLike<T> = {
  records?: T[] | null
  total?: number | string | null
}

export function normalizePageResult<T>(result: RawPageLike<T>) {
  const records = Array.isArray(result.records) ? result.records : []
  const totalNum = Number(result.total)
  const total = Number.isFinite(totalNum) && totalNum >= 0
    ? totalNum
    : records.length
  return {
    records,
    total,
  }
}
