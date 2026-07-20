export type ToastType = 'success' | 'error'

export type ToastDetail = {
  type: ToastType
  title: string
  message: string
  duration?: number
}

export const TOAST_EVENT = 'factory:toast'

function emit(detail: ToastDetail) {
  window.dispatchEvent(new CustomEvent<ToastDetail>(TOAST_EVENT, { detail }))
}

export const toast = {
  success(message: string, title = 'Thành công') {
    emit({ type: 'success', title, message })
  },
  error(message: string, title = 'Thao tác thất bại') {
    emit({ type: 'error', title, message, duration: 5500 })
  },
}
