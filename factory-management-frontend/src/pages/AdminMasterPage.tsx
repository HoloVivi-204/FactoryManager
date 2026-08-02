import { useCallback, useEffect, useMemo, useState } from 'react'
import {
  adminApi,
  type DataScopeType,
  type DepartmentTypeOption,
  type UserDataScope,
} from '../api/adminApi'
import { DataTable, Panel, StatusBadge, type TableColumn } from '../components/ui'
import type { TableRow } from '../types'

type Field = {
  key: string
  label: string
  type?: string
  required?: boolean
  source?: string
}

type MasterConfig = {
  label: string
  fields: Field[]
  columns?: Field[]
}

const configs: Record<string, MasterConfig> = {
  factories: {
    label: 'Nhà máy',
    fields: [
      { key: 'code', label: 'Mã nhà máy', required: true },
      { key: 'name', label: 'Tên nhà máy', required: true },
      { key: 'address', label: 'Địa chỉ', required: true },
    ],
  },
  departments: {
    label: 'Phòng ban',
    fields: [
      { key: 'factoryId', label: 'Nhà máy', required: true, source: 'factories' },
      { key: 'departmentType', label: 'Loại phòng ban', type: 'department-type', required: true },
      { key: 'description', label: 'Mô tả', type: 'textarea' },
    ],
    columns: [
      { key: 'factoryId', label: 'Nhà máy', source: 'factories' },
      { key: 'code', label: 'Mã phòng ban' },
      { key: 'name', label: 'Tên phòng ban' },
      { key: 'departmentType', label: 'Loại phòng ban' },
      { key: 'description', label: 'Mô tả' },
    ],
  },
  'production-lines': {
    label: 'Dây chuyền',
    fields: [
      { key: 'departmentId', label: 'Phòng ban', required: true, source: 'departments' },
      { key: 'code', label: 'Mã dây chuyền', required: true },
      { key: 'name', label: 'Tên dây chuyền', required: true },
      { key: 'description', label: 'Mô tả' },
    ],
  },
  teams: {
    label: 'Tổ sản xuất',
    fields: [
      { key: 'productionLineId', label: 'Dây chuyền', required: true, source: 'production-lines' },
      { key: 'code', label: 'Mã tổ', required: true },
      { key: 'name', label: 'Tên tổ', required: true },
      { key: 'description', label: 'Mô tả' },
    ],
  },
  'machine-types': {
    label: 'Loại máy',
    fields: [
      { key: 'code', label: 'Mã loại máy', required: true },
      { key: 'name', label: 'Tên loại máy', required: true },
      { key: 'description', label: 'Mô tả' },
    ],
  },
  machines: {
    label: 'Máy',
    fields: [
      { key: 'code', label: 'Mã máy', required: true },
      { key: 'name', label: 'Tên máy', required: true },
      { key: 'serialNumber', label: 'Số serial' },
      { key: 'machineTypeId', label: 'Loại máy', required: true, source: 'machine-types' },
      { key: 'teamId', label: 'Tổ sản xuất', required: true, source: 'teams' },
      { key: 'installationDate', label: 'Ngày lắp đặt', type: 'date' },
      { key: 'status', label: 'Trạng thái', type: 'machine-status' },
      { key: 'description', label: 'Mô tả' },
    ],
  },
  employees: {
    label: 'Nhân viên',
    fields: [
      { key: 'teamId', label: 'Tổ sản xuất', source: 'teams' },
      { key: 'code', label: 'Mã nhân viên', required: true },
      { key: 'fullName', label: 'Họ và tên', required: true },
      { key: 'position', label: 'Chức vụ', required: true },
      { key: 'hireDate', label: 'Ngày vào làm', type: 'date', required: true },
    ],
  },
  shifts: {
    label: 'Ca làm việc',
    fields: [
      { key: 'code', label: 'Mã ca', required: true },
      { key: 'name', label: 'Tên ca', required: true },
      { key: 'startTime', label: 'Giờ bắt đầu', type: 'time', required: true },
      { key: 'endTime', label: 'Giờ kết thúc', type: 'time', required: true },
    ],
  },
  'downtime-reasons': {
    label: 'Lý do dừng máy',
    fields: [
      { key: 'code', label: 'Mã lý do', required: true },
      { key: 'name', label: 'Tên lý do', required: true },
      { key: 'reasonType', label: 'Loại dừng', type: 'reason-type', required: true },
      { key: 'description', label: 'Mô tả' },
    ],
  },
  'quality-error-types': {
    label: 'Loại lỗi chất lượng',
    fields: [
      { key: 'code', label: 'Mã lỗi', required: true },
      { key: 'name', label: 'Tên lỗi', required: true },
      { key: 'severity', label: 'Mức độ', type: 'severity', required: true },
      { key: 'description', label: 'Mô tả' },
    ],
  },
  materials: {
    label: 'Vật tư',
    fields: [
      { key: 'code', label: 'Mã vật tư', required: true },
      { key: 'name', label: 'Tên vật tư', required: true },
      { key: 'unit', label: 'Đơn vị tính', required: true },
      { key: 'description', label: 'Mô tả' },
    ],
  },
  users: {
    label: 'Tài khoản & phân quyền',
    fields: [
      { key: 'employeeId', label: 'Nhân viên', source: 'employees', required: true },
      { key: 'username', label: 'Tên đăng nhập', required: true },
      { key: 'password', label: 'Mật khẩu ban đầu', type: 'password', required: true },
      { key: 'roles', label: 'Vai trò', type: 'roles', required: true },
    ],
  },
}

const scopeDefinitions: Record<DataScopeType, { label: string; path: string }> = {
  FACTORY: { label: 'Nhà máy', path: 'factories' },
  DEPARTMENT: { label: 'Phòng ban', path: 'departments' },
  PRODUCTION_LINE: { label: 'Dây chuyền', path: 'production-lines' },
  TEAM: { label: 'Tổ sản xuất', path: 'teams' },
}

const roles = [
  'ADMIN',
  'DIRECTOR',
  'FACTORY_MANAGER',
  'DEPARTMENT_MANAGER',
  'FINANCE',
  'PRODUCTION_MANAGER',
  'TEAM_LEADER',
  'EMPLOYEE',
]

const enumValues: Record<string, string[]> = {
  'machine-status': ['IDLE', 'RUNNING', 'STOPPED', 'MAINTENANCE', 'BREAKDOWN'],
  'reason-type': ['PLANNED', 'UNPLANNED'],
  severity: ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'],
}

const text = (value: unknown, fallback = '—') => (value == null ? fallback : String(value))
const idOf = (row: TableRow) => Number(row.id)
const rolesOf = (row: TableRow | null) =>
  Array.isArray(row?.roles)
    ? row.roles.filter((role): role is string => typeof role === 'string')
    : []

export default function AdminMasterPage({ section }: { section?: string }) {
  const [path, setPath] = useState(section ?? 'factories')
  const [rows, setRows] = useState<TableRow[]>([])
  const [options, setOptions] = useState<Record<string, TableRow[]>>({})
  const [form, setForm] = useState<Record<string, string>>({})
  const [departmentTypes, setDepartmentTypes] = useState<DepartmentTypeOption[]>([])
  const [leaderTeam, setLeaderTeam] = useState<TableRow | null>(null)
  const [leaderCandidates, setLeaderCandidates] = useState<TableRow[]>([])
  const [leaderEmployeeId, setLeaderEmployeeId] = useState('')
  const [leaderError, setLeaderError] = useState('')
  const [leaderBusy, setLeaderBusy] = useState(false)
  const [scopeUser, setScopeUser] = useState<TableRow | null>(null)
  const [userScopes, setUserScopes] = useState<UserDataScope[]>([])
  const [scopeType, setScopeType] = useState<DataScopeType>('FACTORY')
  const [scopeTargetId, setScopeTargetId] = useState('')
  const [scopeTargets, setScopeTargets] = useState<TableRow[]>([])
  const [scopeError, setScopeError] = useState('')
  const [scopeBusy, setScopeBusy] = useState(false)
  const [message, setMessage] = useState<{ text: string; error: boolean } | null>(null)
  const [busy, setBusy] = useState(false)
  const [editingId, setEditingId] = useState<number | null>(null)
  const [modalOpen, setModalOpen] = useState(false)
  const [resetUser, setResetUser] = useState<TableRow | null>(null)
  const [resetValue, setResetValue] = useState('')
  const config = configs[path]

  const sources = useMemo(
    () => [...new Set(config.fields.flatMap((field) => (field.source ? [field.source] : [])))],
    [config],
  )

  const load = useCallback(async () => {
    try {
      setRows(await adminApi.list(path))
    } catch (error) {
      setMessage({ text: (error as Error).message, error: true })
    }
  }, [path])

  useEffect(() => {
    if (section && configs[section]) setPath(section)
  }, [section])

  useEffect(() => {
    setForm({})
    setEditingId(null)
    setModalOpen(false)
    setLeaderTeam(null)
    setScopeUser(null)
    setMessage(null)
    void load()

    const optionRequest = Promise.all(
      sources.map(async (source) => [source, await adminApi.list(source)] as const),
    )
    const departmentTypeRequest =
      path === 'departments' ? adminApi.departmentTypes() : Promise.resolve([])
    Promise.all([optionRequest, departmentTypeRequest])
      .then(([optionEntries, typeEntries]) => {
        setOptions(Object.fromEntries(optionEntries))
        setDepartmentTypes(typeEntries)
      })
      .catch((error) => setMessage({ text: (error as Error).message, error: true }))
  }, [load, path, sources])

  function openCreate() {
    setForm({})
    setEditingId(null)
    setMessage(null)
    setModalOpen(true)
  }

  function edit(row: TableRow) {
    const next: Record<string, string> = {}
    config.fields.forEach((field) => {
      let value = row[field.key]
      if (value == null && field.key.endsWith('Id')) {
        const related = row[field.key.slice(0, -2)] as TableRow | undefined
        value = related?.id
      }
      if (Array.isArray(value)) value = value.join(',')
      if (value != null) next[field.key] = String(value)
    })
    setForm(next)
    setEditingId(idOf(row))
    setMessage(null)
    setModalOpen(true)
  }

  function closeModal() {
    if (busy) return
    setModalOpen(false)
    setEditingId(null)
    setForm({})
    setMessage(null)
  }

  async function save() {
    const missing =
      !editingId && config.fields.find((field) => field.required && !form[field.key]?.trim())
    if (missing) {
      setMessage({ text: `Vui lòng nhập/chọn ${missing.label}.`, error: true })
      return
    }
    if (path === 'users' && !editingId && (form.password ?? '').length < 8) {
      setMessage({ text: 'Mật khẩu phải có ít nhất 8 ký tự.', error: true })
      return
    }

    const data = Object.fromEntries(
      Object.entries(form)
        .filter(([, value]) => value !== '')
        .map(([key, value]) => (key.endsWith('Id') ? [key, Number(value)] : [key, value.trim()])),
    )

    setBusy(true)
    try {
      if (path === 'users') {
        const selectedRoles = (form.roles ?? '').split(',').filter(Boolean)
        if (editingId) {
          await adminApi.updateRoles(editingId, selectedRoles)
        } else {
          const account = await adminApi.register({
            employeeId: Number(form.employeeId),
            username: form.username,
            password: form.password,
          })
          await adminApi.updateRoles(account.userId, selectedRoles)
        }
      } else if (editingId) {
        await adminApi.update(path, editingId, data)
      } else {
        await adminApi.create(path, {
          ...data,
          active: true,
          ...(path === 'machines' && !data.status ? { status: 'IDLE' } : {}),
        })
      }
      setModalOpen(false)
      setForm({})
      setEditingId(null)
      setMessage({ text: `Đã lưu ${config.label} thành công.`, error: false })
      await load()
    } catch (error) {
      setMessage({ text: (error as Error).message, error: true })
    } finally {
      setBusy(false)
    }
  }

  async function remove(id: number) {
    if (!confirm(`Bạn có chắc muốn xóa mềm ${config.label} #${id}?`)) return
    setBusy(true)
    try {
      await adminApi.remove(path, id)
      setMessage({ text: `Đã xóa ${config.label} thành công.`, error: false })
      await load()
    } catch (error) {
      setMessage({ text: (error as Error).message, error: true })
    } finally {
      setBusy(false)
    }
  }

  async function resetPassword() {
    if (!resetUser) return
    if (resetValue.length < 8) {
      setMessage({ text: 'Mật khẩu mới phải có ít nhất 8 ký tự.', error: true })
      return
    }
    setBusy(true)
    try {
      await adminApi.resetPassword(idOf(resetUser), resetValue)
      const username = text(resetUser.username)
      setResetUser(null)
      setResetValue('')
      setMessage({
        text: `Đã đặt lại mật khẩu cho ${username}. Các phiên đăng nhập cũ đã bị vô hiệu hóa.`,
        error: false,
      })
    } catch (error) {
      setMessage({ text: (error as Error).message, error: true })
    } finally {
      setBusy(false)
    }
  }

  async function openLeaderModal(team: TableRow) {
    setLeaderTeam(team)
    setLeaderCandidates([])
    setLeaderEmployeeId(team.leaderEmployeeId ? String(team.leaderEmployeeId) : '')
    setLeaderError('')
    setLeaderBusy(true)
    try {
      setLeaderCandidates(await adminApi.employeesByTeam(idOf(team)))
    } catch (error) {
      setLeaderError((error as Error).message)
    } finally {
      setLeaderBusy(false)
    }
  }

  function closeLeaderModal() {
    if (!leaderBusy) {
      setLeaderTeam(null)
      setLeaderCandidates([])
      setLeaderEmployeeId('')
      setLeaderError('')
    }
  }

  async function assignLeader() {
    if (!leaderTeam || !leaderEmployeeId) {
      setLeaderError('Vui lòng chọn một nhân viên thuộc tổ.')
      return
    }
    setLeaderBusy(true)
    setLeaderError('')
    try {
      await adminApi.assignTeamLeader(idOf(leaderTeam), Number(leaderEmployeeId))
      const teamName = text(leaderTeam.name)
      setLeaderTeam(null)
      setMessage({ text: `Đã gán tổ trưởng cho ${teamName}.`, error: false })
      await load()
    } catch (error) {
      setLeaderError((error as Error).message)
    } finally {
      setLeaderBusy(false)
    }
  }

  async function removeLeader() {
    if (!leaderTeam) return
    setLeaderBusy(true)
    setLeaderError('')
    try {
      await adminApi.removeTeamLeader(idOf(leaderTeam))
      const teamName = text(leaderTeam.name)
      setLeaderTeam(null)
      setMessage({ text: `Đã gỡ tổ trưởng của ${teamName}.`, error: false })
      await load()
    } catch (error) {
      setLeaderError((error as Error).message)
    } finally {
      setLeaderBusy(false)
    }
  }

  async function openScopeModal(user: TableRow) {
    setScopeUser(user)
    setUserScopes([])
    setScopeType('FACTORY')
    setScopeTargetId('')
    setScopeTargets([])
    setScopeError('')
    setScopeBusy(true)
    try {
      const [scopes, targets] = await Promise.all([
        adminApi.dataScopes(idOf(user)),
        adminApi.list(scopeDefinitions.FACTORY.path),
      ])
      setUserScopes(scopes)
      setScopeTargets(targets)
    } catch (error) {
      setScopeError((error as Error).message)
    } finally {
      setScopeBusy(false)
    }
  }

  function closeScopeModal() {
    if (!scopeBusy) {
      setScopeUser(null)
      setUserScopes([])
      setScopeTargets([])
      setScopeTargetId('')
      setScopeError('')
    }
  }

  async function changeScopeType(next: DataScopeType) {
    setScopeType(next)
    setScopeTargetId('')
    setScopeError('')
    setScopeBusy(true)
    try {
      setScopeTargets(await adminApi.list(scopeDefinitions[next].path))
    } catch (error) {
      setScopeTargets([])
      setScopeError((error as Error).message)
    } finally {
      setScopeBusy(false)
    }
  }

  async function addScope() {
    if (!scopeUser || !scopeTargetId) {
      setScopeError(`Vui lòng chọn ${scopeDefinitions[scopeType].label.toLowerCase()}.`)
      return
    }
    setScopeBusy(true)
    setScopeError('')
    try {
      await adminApi.addDataScope(idOf(scopeUser), scopeType, Number(scopeTargetId))
      setUserScopes(await adminApi.dataScopes(idOf(scopeUser)))
      setScopeTargetId('')
    } catch (error) {
      setScopeError((error as Error).message)
    } finally {
      setScopeBusy(false)
    }
  }

  async function removeScope(dataScopeId: number) {
    if (!scopeUser || !confirm('Bạn có chắc muốn thu hồi phạm vi dữ liệu này?')) return
    setScopeBusy(true)
    setScopeError('')
    try {
      await adminApi.removeDataScope(idOf(scopeUser), dataScopeId)
      setUserScopes(await adminApi.dataScopes(idOf(scopeUser)))
    } catch (error) {
      setScopeError((error as Error).message)
    } finally {
      setScopeBusy(false)
    }
  }

  const selectedFactory = (options.factories ?? []).find(
    (item) => String(item.id) === form.factoryId,
  )
  const selectedDepartmentType = departmentTypes.find((item) => item.type === form.departmentType)
  const visibleFields = config.fields.filter(
    (field) =>
      !(path === 'users' && editingId && (field.key === 'employeeId' || field.key === 'password')),
  )

  const fields = (
    <>
      <div className="form-grid">
        {visibleFields.map((field) =>
          field.type === 'roles' ? (
            <div className="field role-field" key={field.key}>
              <span>Vai trò *</span>
              <div className="role-options">
                {roles.map((role) => {
                  const checked = (form.roles ?? '').split(',').includes(role)
                  return (
                    <label key={role}>
                      <input
                        type="checkbox"
                        checked={checked}
                        onChange={() => {
                          const current = (form.roles ?? '').split(',').filter(Boolean)
                          setForm({
                            ...form,
                            roles: (checked
                              ? current.filter((item) => item !== role)
                              : [...current, role]
                            ).join(','),
                          })
                        }}
                      />
                      {role}
                    </label>
                  )
                })}
              </div>
            </div>
          ) : (
            <label className="field" key={field.key}>
              <span>
                {field.label}
                {field.required && !editingId ? ' *' : ''}
              </span>
              {field.source ? (
                <select
                  disabled={path === 'users' && !!editingId}
                  value={form[field.key] ?? ''}
                  onChange={(event) => setForm({ ...form, [field.key]: event.target.value })}
                >
                  <option value="">-- Chọn {field.label.toLowerCase()} --</option>
                  {(options[field.source] ?? []).map((item) => (
                    <option key={String(item.id)} value={String(item.id ?? '')}>
                      {item.code ? `${text(item.code)} - ` : ''}
                      {text(item.name ?? item.fullName)}
                    </option>
                  ))}
                </select>
              ) : field.type === 'department-type' ? (
                <select
                  value={form[field.key] ?? ''}
                  onChange={(event) => {
                    const selected = departmentTypes.find(
                      (item) => item.type === event.target.value,
                    )
                    setForm({
                      ...form,
                      [field.key]: event.target.value,
                      ...(selected ? { description: selected.description } : { description: '' }),
                    })
                  }}
                >
                  <option value="">-- Chọn loại phòng ban --</option>
                  {departmentTypes.map((option) => (
                    <option key={option.type} value={option.type}>
                      {option.name} ({option.codeSuffix})
                    </option>
                  ))}
                </select>
              ) : field.type && enumValues[field.type] ? (
                <select
                  value={form[field.key] ?? ''}
                  onChange={(event) => setForm({ ...form, [field.key]: event.target.value })}
                >
                  <option value="">-- Chọn {field.label.toLowerCase()} --</option>
                  {enumValues[field.type].map((value) => (
                    <option key={value}>{value}</option>
                  ))}
                </select>
              ) : field.type === 'textarea' ? (
                <textarea
                  value={form[field.key] ?? ''}
                  onChange={(event) => setForm({ ...form, [field.key]: event.target.value })}
                />
              ) : (
                <input
                  disabled={path === 'users' && !!editingId}
                  type={field.type ?? 'text'}
                  value={form[field.key] ?? ''}
                  onChange={(event) => setForm({ ...form, [field.key]: event.target.value })}
                />
              )}
            </label>
          ),
        )}
      </div>
      {path === 'departments' && selectedDepartmentType && (
        <p className="hint">
          Tên cố định: <strong>{selectedDepartmentType.name}</strong>. Mã tự sinh:{' '}
          <strong>
            {selectedFactory
              ? `${text(selectedFactory.code)}-${selectedDepartmentType.codeSuffix}`
              : `{MÃ_NHÀ_MÁY}-${selectedDepartmentType.codeSuffix}`}
          </strong>
          . Bạn có thể chỉnh lại phần mô tả trước khi lưu.
        </p>
      )}
    </>
  )

  const normalColumns: TableColumn<TableRow>[] = (config.columns ?? config.fields).map((field) => ({
    key: field.key,
    label: field.label,
    render: (row) => {
      if (field.source) {
        const base = field.key.slice(0, -2)
        return text(row[`${base}Name`] ?? row[`${base}Code`] ?? row[field.key])
      }
      const value = row[field.key]
      return Array.isArray(value) ? value.join(', ') : text(value)
    },
  }))

  const teamLeaderColumns: TableColumn<TableRow>[] =
    path === 'teams'
      ? [
          {
            key: 'leaderEmployeeName',
            label: 'Tổ trưởng',
            render: (row) =>
              row.leaderEmployeeId
                ? `${text(row.leaderEmployeeCode)} - ${text(row.leaderEmployeeName)}`
                : 'Chưa gán',
          },
        ]
      : []

  const userColumns: TableColumn<TableRow>[] = [
    { key: 'employeeCode', label: 'Mã nhân viên' },
    { key: 'employeeName', label: 'Nhân viên' },
    { key: 'username', label: 'Tên đăng nhập' },
    { key: 'roles', label: 'Vai trò', render: (row) => rolesOf(row).join(', ') || '—' },
    { key: 'enabled', label: 'Sử dụng', render: (row) => <StatusBadge value={row.enabled} /> },
    {
      key: 'accountNonLocked',
      label: 'Khóa',
      render: (row) => <StatusBadge value={row.accountNonLocked ? 'Không khóa' : 'Đã khóa'} />,
    },
  ]

  const tableColumns: TableColumn<TableRow>[] = [
    { key: 'id', label: 'ID' },
    ...(path === 'users' ? userColumns : [...normalColumns, ...teamLeaderColumns]),
    ...(path === 'users'
      ? []
      : [
          {
            key: 'active',
            label: 'Trạng thái',
            render: (row: TableRow) => <StatusBadge value={row.active} />,
          },
        ]),
    {
      key: 'action',
      label: 'Thao tác',
      render: (row) => (
        <div className="admin-actions">
          <button disabled={busy} onClick={() => edit(row)}>
            {path === 'users' ? 'Phân quyền' : 'Sửa'}
          </button>
          {path === 'teams' && (
            <button disabled={busy} onClick={() => void openLeaderModal(row)}>
              {row.leaderEmployeeId ? 'Đổi tổ trưởng' : 'Gán tổ trưởng'}
            </button>
          )}
          {path === 'users' && (
            <button
              disabled={busy}
              onClick={() => {
                setMessage(null)
                setResetValue('')
                setResetUser(row)
              }}
            >
              Đặt lại mật khẩu
            </button>
          )}
          {path === 'users' && (
            <button disabled={busy} onClick={() => void openScopeModal(row)}>
              Phạm vi dữ liệu
            </button>
          )}
          {path !== 'users' && (
            <button disabled={busy} className="danger-link" onClick={() => void remove(idOf(row))}>
              Xóa
            </button>
          )}
          {resetUser?.id === row.id && (
            <div
              className="modal-backdrop"
              onMouseDown={(event) => {
                if (event.target === event.currentTarget && !busy) setResetUser(null)
              }}
            >
              <div className="modal reset-password-modal">
                <div className="admin-modal-header">
                  <div>
                    <h2>Đặt lại mật khẩu</h2>
                    <p>
                      {text(row.employeeName)} — {text(row.username)}
                    </p>
                  </div>
                  <button
                    className="modal-close"
                    disabled={busy}
                    onClick={() => setResetUser(null)}
                  >
                    ×
                  </button>
                </div>
                <div className="reset-password-body">
                  <label className="field">
                    <span>Mật khẩu mới *</span>
                    <input
                      autoFocus
                      type="password"
                      value={resetValue}
                      onChange={(event) => setResetValue(event.target.value)}
                      placeholder="Tối thiểu 8 ký tự"
                    />
                  </label>
                  {message && (
                    <p className={`form-message${message.error ? ' error' : ''}`}>{message.text}</p>
                  )}
                </div>
                <div className="form-actions">
                  <button disabled={busy} onClick={() => setResetUser(null)}>
                    Hủy
                  </button>
                  <button className="primary" disabled={busy} onClick={() => void resetPassword()}>
                    {busy ? 'Đang xử lý...' : 'Xác nhận đặt lại'}
                  </button>
                </div>
              </div>
            </div>
          )}
        </div>
      ),
    },
  ]

  const leaderModal = leaderTeam && (
    <div
      className="modal-backdrop"
      onMouseDown={(event) => {
        if (event.target === event.currentTarget) closeLeaderModal()
      }}
    >
      <div className="modal admin-modal">
        <div className="admin-modal-header">
          <div>
            <h2>{leaderTeam.leaderEmployeeId ? 'Đổi tổ trưởng' : 'Gán tổ trưởng'}</h2>
            <p>
              {text(leaderTeam.code)} — {text(leaderTeam.name)}
            </p>
          </div>
          <button
            aria-label="Đóng"
            className="modal-close"
            disabled={leaderBusy}
            onClick={closeLeaderModal}
          >
            ×
          </button>
        </div>
        <div className="form-grid">
          <label className="field">
            <span>Nhân viên thuộc tổ *</span>
            <select
              autoFocus
              disabled={leaderBusy}
              value={leaderEmployeeId}
              onChange={(event) => setLeaderEmployeeId(event.target.value)}
            >
              <option value="">
                {leaderBusy ? 'Đang tải nhân viên...' : '-- Chọn nhân viên --'}
              </option>
              {leaderCandidates.map((employee) => (
                <option key={String(employee.id)} value={String(employee.id ?? '')}>
                  {text(employee.code)} - {text(employee.fullName)}
                  {employee.position ? ` (${text(employee.position)})` : ''}
                </option>
              ))}
            </select>
          </label>
        </div>
        {!leaderBusy && leaderCandidates.length === 0 && (
          <p className="hint">
            Tổ này chưa có nhân viên hoạt động. Hãy phân nhân viên vào tổ trước khi gán tổ trưởng.
          </p>
        )}
        <p className="hint">
          Danh sách chỉ hiển thị nhân viên đang hoạt động và thuộc đúng tổ này.
        </p>
        {leaderError && <p className="form-message error">{leaderError}</p>}
        <div className="form-actions">
          {Boolean(leaderTeam.leaderEmployeeId) && (
            <button className="danger" disabled={leaderBusy} onClick={() => void removeLeader()}>
              Gỡ tổ trưởng
            </button>
          )}
          <button disabled={leaderBusy} onClick={closeLeaderModal}>
            Hủy
          </button>
          <button
            className="primary"
            disabled={leaderBusy || !leaderEmployeeId}
            onClick={() => void assignLeader()}
          >
            {leaderBusy ? 'Đang xử lý...' : 'Xác nhận gán'}
          </button>
        </div>
      </div>
    </div>
  )

  const scopeModal = scopeUser && (
    <div
      className="modal-backdrop"
      onMouseDown={(event) => {
        if (event.target === event.currentTarget) closeScopeModal()
      }}
    >
      <div className="modal admin-modal">
        <div className="admin-modal-header">
          <div>
            <h2>Phạm vi dữ liệu</h2>
            <p>
              {text(scopeUser.employeeName)} — {text(scopeUser.username)}
            </p>
          </div>
          <button
            aria-label="Đóng"
            className="modal-close"
            disabled={scopeBusy}
            onClick={closeScopeModal}
          >
            ×
          </button>
        </div>
        {rolesOf(scopeUser).some((role) => role === 'ADMIN' || role === 'DIRECTOR') && (
          <p className="hint">
            ADMIN và DIRECTOR được xem toàn hệ thống nên không bắt buộc cấp phạm vi.
          </p>
        )}
        <div className="form-grid">
          <label className="field">
            <span>Cấp phạm vi *</span>
            <select
              disabled={scopeBusy}
              value={scopeType}
              onChange={(event) => void changeScopeType(event.target.value as DataScopeType)}
            >
              {(Object.keys(scopeDefinitions) as DataScopeType[]).map((type) => (
                <option key={type} value={type}>
                  {scopeDefinitions[type].label}
                </option>
              ))}
            </select>
          </label>
          <label className="field">
            <span>{scopeDefinitions[scopeType].label} *</span>
            <select
              disabled={scopeBusy}
              value={scopeTargetId}
              onChange={(event) => setScopeTargetId(event.target.value)}
            >
              <option value="">
                {scopeBusy
                  ? 'Đang tải dữ liệu...'
                  : `-- Chọn ${scopeDefinitions[scopeType].label.toLowerCase()} --`}
              </option>
              {scopeTargets.map((target) => (
                <option key={String(target.id)} value={String(target.id ?? '')}>
                  {target.code ? `${text(target.code)} - ` : ''}
                  {text(target.name)}
                </option>
              ))}
            </select>
          </label>
        </div>
        <div className="form-actions">
          <button
            className="primary"
            disabled={scopeBusy || !scopeTargetId}
            onClick={() => void addScope()}
          >
            {scopeBusy ? 'Đang xử lý...' : 'Thêm phạm vi'}
          </button>
        </div>
        <p className="hint">
          <strong>Phạm vi đã cấp</strong> — một tài khoản có thể được cấp nhiều khu vực.
        </p>
        <DataTable
          rows={userScopes}
          columns={[
            {
              key: 'scopeType',
              label: 'Cấp',
              render: (scope) => scopeDefinitions[scope.scopeType].label,
            },
            { key: 'scopeCode', label: 'Mã' },
            { key: 'scopeName', label: 'Tên phạm vi' },
            {
              key: 'action',
              label: 'Thao tác',
              render: (scope) => (
                <button
                  className="danger-link"
                  disabled={scopeBusy}
                  onClick={() => void removeScope(scope.id)}
                >
                  Thu hồi
                </button>
              ),
            },
          ]}
        />
        {scopeError && <p className="form-message error">{scopeError}</p>}
        <div className="form-actions">
          <button disabled={scopeBusy} onClick={closeScopeModal}>
            Đóng
          </button>
        </div>
      </div>
    </div>
  )

  return (
    <>
      <div className="page-title">
        <div>
          <h2>Quản trị dữ liệu nhà máy</h2>
          <p>Quản lý đầy đủ danh mục tổ chức, nhân sự, ca làm, máy móc, chất lượng và vật tư</p>
        </div>
        <button className="admin-add-button" onClick={openCreate}>
          + Thêm {config.label}
        </button>
      </div>
      <div className="step-tabs">
        {Object.entries(configs).map(([key, value]) => (
          <button key={key} className={path === key ? 'active' : ''} onClick={() => setPath(key)}>
            {value.label}
          </button>
        ))}
      </div>
      {message && !modalOpen && (
        <p className={`form-message${message.error ? ' error' : ''}`}>{message.text}</p>
      )}
      <Panel title={`Danh sách ${config.label}`}>
        <DataTable rows={rows} columns={tableColumns} />
      </Panel>
      {modalOpen && (
        <div
          className="modal-backdrop"
          onMouseDown={(event) => {
            if (event.target === event.currentTarget) closeModal()
          }}
        >
          <div className="modal admin-modal">
            <div className="admin-modal-header">
              <div>
                <h2>
                  {editingId ? 'Sửa' : 'Thêm'} {config.label}
                </h2>
                <p>
                  {editingId
                    ? `Cập nhật bản ghi #${editingId}`
                    : 'Nhập đầy đủ thông tin bản ghi mới'}
                </p>
              </div>
              <button aria-label="Đóng" className="modal-close" onClick={closeModal}>
                ×
              </button>
            </div>
            {fields}
            {message && (
              <p className={`form-message${message.error ? ' error' : ''}`}>{message.text}</p>
            )}
            <div className="form-actions">
              <button disabled={busy} onClick={closeModal}>
                Hủy
              </button>
              <button className="primary" disabled={busy} onClick={() => void save()}>
                {busy ? 'Đang xử lý...' : editingId ? 'Lưu thay đổi' : 'Thêm mới'}
              </button>
            </div>
          </div>
        </div>
      )}
      {leaderModal}
      {scopeModal}
    </>
  )
}
