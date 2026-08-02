import { useCallback, useEffect, useRef, useState } from 'react'
import { CheckCircle2, CircleX, X } from 'lucide-react'
import { TOAST_EVENT, type ToastDetail } from '../utils/toast'

type ToastItem = ToastDetail & { id: number }

export default function ToastViewport() {
  const [items, setItems] = useState<ToastItem[]>([])
  const nextId = useRef(0)
  const timers = useRef(new Map<number, number>())

  const remove = useCallback((id: number) => {
    const timer = timers.current.get(id)
    if (timer !== undefined) window.clearTimeout(timer)
    timers.current.delete(id)
    setItems((current) => current.filter((item) => item.id !== id))
  }, [])

  useEffect(() => {
    const receive = (event: Event) => {
      const detail = (event as CustomEvent<ToastDetail>).detail
      const id = ++nextId.current
      setItems((current) => [...current, { ...detail, id }].slice(-5))
      const timer = window.setTimeout(() => remove(id), detail.duration ?? 4200)
      timers.current.set(id, timer)
    }

    window.addEventListener(TOAST_EVENT, receive)
    const activeTimers = timers.current
    return () => {
      window.removeEventListener(TOAST_EVENT, receive)
      activeTimers.forEach((timer) => window.clearTimeout(timer))
      activeTimers.clear()
    }
  }, [remove])

  return (
    <div className="toast-viewport" aria-live="polite" aria-atomic="false">
      {items.map((item) => {
        const Icon = item.type === 'success' ? CheckCircle2 : CircleX
        return (
          <article
            key={item.id}
            className={`app-toast toast-${item.type}`}
            role={item.type === 'error' ? 'alert' : 'status'}
          >
            <Icon className="toast-icon" size={22} aria-hidden="true" />
            <div className="toast-copy">
              <strong>{item.title}</strong>
              <p>{item.message}</p>
            </div>
            <button type="button" onClick={() => remove(item.id)} aria-label="Đóng thông báo">
              <X size={17} aria-hidden="true" />
            </button>
          </article>
        )
      })}
    </div>
  )
}
