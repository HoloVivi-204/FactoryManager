import { useCallback, useEffect, useRef, useState } from 'react'

export function useApi<T>(loader: () => Promise<T>, reloadKey?: unknown) {
  const loaderRef = useRef(loader)
  const [data, setData] = useState<T>()
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    loaderRef.current = loader
  }, [loader])

  const reload = useCallback(() => {
    setLoading(true)
    setError('')
    loaderRef
      .current()
      .then(setData)
      .catch((loadError: Error) => setError(loadError.message))
      .finally(() => setLoading(false))
  }, [])

  useEffect(() => {
    reload()
  }, [reload, reloadKey])

  return { data, loading, error, reload }
}
