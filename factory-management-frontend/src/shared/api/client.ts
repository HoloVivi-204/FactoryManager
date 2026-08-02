import { TokenManager } from '../../features/auth/utils/TokenManager'
import { toast } from '../toast/toast'

export const API_BASE_URL =
  import.meta.env.VITE_API_URL ?? 'http://localhost:8080/factory-management/api/v1'

type Envelope<T> = {
  code: number
  message?: string
  result?: T
}

type Method = 'GET' | 'POST' | 'PUT' | 'DELETE'

function successMessage(path: string, method: Method, serverMessage?: string) {
  if (serverMessage?.trim()) return serverMessage
  if (path === '/auth/login') return 'Đăng nhập thành công.'
  if (path === '/auth/logout') return 'Đăng xuất thành công.'
  if (path === '/auth/register') return 'Tạo tài khoản thành công.'
  if (path.endsWith('/approve')) return 'Phê duyệt báo cáo thành công.'
  if (path.endsWith('/request-change')) return 'Đã gửi yêu cầu chỉnh sửa báo cáo.'
  if (path.endsWith('/submit')) return 'Đã gửi báo cáo chờ phê duyệt.'
  if (path.includes('/reset-password')) return 'Đặt lại mật khẩu thành công.'
  if (path.includes('/roles')) return 'Cập nhật vai trò thành công.'
  if (path.includes('/leave-requests/') && path.endsWith('/decision'))
    return 'Đã xử lý đơn nghỉ phép.'
  if (path.includes('/overtime-requests/') && path.endsWith('/decision'))
    return 'Đã xử lý đăng ký tăng ca.'
  if (path.includes('/employee-portal/leave-requests')) return 'Gửi đơn nghỉ phép thành công.'
  if (path.includes('/employee-portal/overtime-requests')) return 'Gửi đăng ký tăng ca thành công.'
  if (path.includes('/hr/schedules'))
    return method === 'DELETE' ? 'Đã ngừng lịch làm việc.' : 'Đã lưu lịch làm việc.'
  if (path.includes('/hr/attendance')) return 'Đã lưu chấm công.'
  if (path.includes('/hr/kpis')) return 'Đã lưu KPI nhân viên.'
  if (path.includes('/hr/assignments')) return 'Đã cập nhật phân công nhân sự.'
  if (path.includes('/hr/notifications')) return 'Đã gửi thông báo.'
  if (path.includes('/hr/overtime-requests')) return 'Đã lưu đăng ký tăng ca.'
  if (path.includes('/notifications/') && path.endsWith('/read'))
    return 'Đã đánh dấu thông báo là đã đọc.'
  if (path.includes('/maintenance/requests/') && path.endsWith('/status'))
    return 'Đã cập nhật trạng thái yêu cầu bảo trì.'
  if (path === '/maintenance/requests') return 'Đã gửi yêu cầu bảo trì.'
  if (path.includes('/maintenance/schedules'))
    return method === 'DELETE' ? 'Đã ngừng lịch bảo trì.' : 'Đã lưu lịch bảo trì.'
  if (path.includes('/maintenance/work-orders/') && path.endsWith('/status'))
    return 'Đã cập nhật trạng thái phiếu bảo trì.'
  if (path.includes('/maintenance/work-orders/') && path.includes('/parts'))
    return method === 'DELETE'
      ? 'Đã bỏ vật tư khỏi phiếu bảo trì.'
      : 'Đã thêm vật tư vào phiếu bảo trì.'
  if (path === '/maintenance/work-orders') return 'Đã lập phiếu công việc bảo trì.'
  if (method === 'DELETE') return 'Xóa dữ liệu thành công.'
  if (method === 'POST') return 'Thêm dữ liệu thành công.'
  return 'Cập nhật dữ liệu thành công.'
}

function fail(message: string, notify: boolean): never {
  if (notify) toast.error(message)
  throw new Error(message)
}

async function request<T>(
  path: string,
  method: Method = 'GET',
  data?: unknown,
  auth = true,
  notifyOverride?: boolean,
): Promise<T> {
  const token = TokenManager.token()
  const notify = notifyOverride ?? method !== 'GET'
  let response: Response

  try {
    response = await fetch(`${API_BASE_URL}${path}`, {
      method,
      headers: {
        'Content-Type': 'application/json',
        ...(auth && token ? { Authorization: `Bearer ${token}` } : {}),
      },
      body: data === undefined ? undefined : JSON.stringify(data),
    })
  } catch {
    return fail(
      'Không kết nối được backend. Hãy kiểm tra Spring Boot đang chạy ở cổng 8080.',
      notify,
    )
  }

  let body: Envelope<T>
  try {
    body = await response.json()
  } catch {
    return fail(`Máy chủ trả về dữ liệu không hợp lệ (HTTP ${response.status}).`, notify)
  }

  if (response.status === 401 && auth) {
    TokenManager.clear()
    window.dispatchEvent(new Event('factory:unauthorized'))
  }

  if (response.status === 403) {
    return fail('Tài khoản hiện tại không có quyền thực hiện thao tác này.', notify)
  }

  if (!response.ok || body.code !== 1000) {
    return fail(body.message ?? 'Không thể thực hiện yêu cầu.', notify)
  }

  if (notify) toast.success(successMessage(path, method, body.message))
  return body.result as T
}

export const get = <T>(path: string) => request<T>(path)

export const send = <T>(path: string, method: Exclude<Method, 'GET'>, data?: unknown) =>
  request<T>(path, method, data)

export const sendSilent = <T>(path: string, method: Exclude<Method, 'GET'>, data?: unknown) =>
  request<T>(path, method, data, true, false)

export const publicPost = <T>(path: string, data: unknown) => request<T>(path, 'POST', data, false)

export async function uploadFile<T>(path: string, file: File, notify = false): Promise<T> {
  const token = TokenManager.token()
  const data = new FormData()
  data.append('file', file)
  let response: Response
  try {
    response = await fetch(`${API_BASE_URL}${path}`, {
      method: 'POST',
      headers: token ? { Authorization: `Bearer ${token}` } : {},
      body: data,
    })
  } catch {
    return fail('Không kết nối được backend để tải file Excel lên.', notify)
  }

  let body: Envelope<T>
  try {
    body = await response.json()
  } catch {
    return fail(
      response.status === 413
        ? 'File Excel vượt quá giới hạn 10 MB.'
        : `Máy chủ không xử lý được file (HTTP ${response.status}).`,
      notify,
    )
  }
  if (response.status === 401) {
    TokenManager.clear()
    window.dispatchEvent(new Event('factory:unauthorized'))
  }
  if (!response.ok || body.code !== 1000)
    return fail(body.message ?? 'Không thể xử lý file Excel.', notify)
  if (notify) toast.success(body.message ?? 'Nhập dữ liệu Excel thành công.')
  return body.result as T
}

export async function downloadFile(path: string, fallbackName: string) {
  const token = TokenManager.token()
  const response = await fetch(`${API_BASE_URL}${path}`, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  })
  if (!response.ok) {
    let message = 'Không thể tải file mẫu.'
    try {
      const body = (await response.json()) as Envelope<unknown>
      message = body.message ?? message
    } catch {
      /* backend không trả JSON */
    }
    return fail(message, true)
  }
  const blob = await response.blob()
  const disposition = response.headers.get('content-disposition') ?? ''
  const encodedName = disposition.match(/filename\*=UTF-8''([^;]+)/i)?.[1]
  const plainName = disposition.match(/filename="?([^";]+)"?/i)?.[1]
  const fileName = encodedName ? decodeURIComponent(encodedName) : (plainName ?? fallbackName)
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = fileName
  document.body.appendChild(link)
  link.click()
  link.remove()
  URL.revokeObjectURL(url)
  toast.success('Đã tải file Excel mẫu.')
}
