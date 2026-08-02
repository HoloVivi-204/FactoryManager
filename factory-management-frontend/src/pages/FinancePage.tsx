import { useEffect, useState } from 'react'
import { financeApi, type FinanceSummary } from '../api/financeApi'
import { DataTable, KpiCard, Panel, StatusBadge } from '../components/ui'
import type { TableRow } from '../types'

type FinanceView = 'finance' | 'warehouses' | 'inventory'
type FormState = Record<string, string>
type OptionGroups = {
  categories: TableRow[]
  factories: TableRow[]
  departments: TableRow[]
  lines: TableRow[]
  materials: TableRow[]
  warehouses: TableRow[]
}

const emptyOptions: OptionGroups = {
  categories: [],
  factories: [],
  departments: [],
  lines: [],
  materials: [],
  warehouses: [],
}

const money = (value: unknown) =>
  new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(Number(value ?? 0))

export default function FinancePage({ view }: { view: FinanceView }) {
  const [rows, setRows] = useState<TableRow[]>([])
  const [summary, setSummary] = useState<FinanceSummary>({})
  const [options, setOptions] = useState<OptionGroups>(emptyOptions)
  const [open, setOpen] = useState(false)
  const [form, setForm] = useState<FormState>({})
  const [message, setMessage] = useState('')

  async function load() {
    try {
      const [categories, factories, departments, lines, materials, warehouses] = await Promise.all([
        financeApi.categories(),
        financeApi.factories(),
        financeApi.departments(),
        financeApi.lines(),
        financeApi.materials(),
        financeApi.warehouses(),
      ])
      setOptions({ categories, factories, departments, lines, materials, warehouses })

      if (view === 'finance') {
        setRows(await financeApi.records())
        setSummary(await financeApi.summary())
      } else if (view === 'warehouses') {
        setRows(warehouses)
      } else {
        setRows(await financeApi.transactions())
      }
    } catch (error) {
      setMessage((error as Error).message)
    }
  }

  useEffect(() => {
    setOpen(false)
    setForm({})
    void load()
  }, [view])

  async function save() {
    try {
      if (view === 'finance') {
        await financeApi.createRecord({
          ...form,
          categoryId: Number(form.categoryId),
          factoryId: Number(form.factoryId),
          departmentId: form.departmentId ? Number(form.departmentId) : null,
          productionLineId: form.productionLineId ? Number(form.productionLineId) : null,
          amount: Number(form.amount),
          paidAmount: Number(form.paidAmount || 0),
          active: true,
        })
      } else if (view === 'warehouses') {
        await financeApi.createWarehouse({
          ...form,
          factoryId: Number(form.factoryId),
          active: true,
        })
      } else {
        await financeApi.createTransaction({
          ...form,
          warehouseId: Number(form.warehouseId),
          materialId: Number(form.materialId),
          quantity: Number(form.quantity),
          unitCost: form.unitCost ? Number(form.unitCost) : null,
        })
      }

      setOpen(false)
      setForm({})
      setMessage('Đã lưu dữ liệu thành công.')
      await load()
    } catch (error) {
      setMessage((error as Error).message)
    }
  }

  const select = (key: string, label: string, list: TableRow[]) => (
    <label className="field">
      <span>{label}</span>
      <select value={form[key] ?? ''} onChange={(event) => setForm({ ...form, [key]: event.target.value })}>
        <option value="">-- Chọn --</option>
        {list.map((item) => (
          <option key={String(item.id)} value={String(item.id ?? '')}>
            {String(item.code ?? '')} - {String(item.name ?? '')}
          </option>
        ))}
      </select>
    </label>
  )

  return (
    <>
      <div className="page-title">
        <div>
          <h2>{view === 'finance' ? 'Tài chính quản trị' : view === 'warehouses' ? 'Quản lý kho' : 'Nhập - xuất - tồn'}</h2>
          <p>Dữ liệu trong phạm vi nhà máy được phân quyền</p>
        </div>
        <button className="admin-add-button" onClick={() => setOpen(true)}>+ Nhập dữ liệu</button>
      </div>

      {view === 'finance' && (
        <div className="kpi-grid">
          <KpiCard label="DOANH THU" value={money(summary.totalRevenue)} hint="Trong kỳ" tone="green" />
          <KpiCard label="CHI PHÍ" value={money(summary.totalExpense)} hint="Trong kỳ" tone="orange" />
          <KpiCard label="LỢI NHUẬN" value={money(summary.profit)} hint="Doanh thu - chi phí" tone="blue" />
          <KpiCard
            label="PHẢI THU / PHẢI TRẢ"
            value={`${money(summary.accountsReceivable)} / ${money(summary.accountsPayable)}`}
            hint="Công nợ"
            tone="purple"
          />
        </div>
      )}

      {message && <p className="form-message">{message}</p>}

      <Panel title="Danh sách">
        <DataTable
          rows={rows}
          columns={
            view === 'finance'
              ? [
                  { key: 'recordNo', label: 'Số CT' },
                  { key: 'recordDate', label: 'Ngày' },
                  { key: 'categoryName', label: 'Danh mục' },
                  { key: 'counterparty', label: 'Đối tác' },
                  { key: 'amount', label: 'Số tiền', render: (row) => money(row.amount) },
                  { key: 'paymentStatus', label: 'Thanh toán', render: (row) => <StatusBadge value={row.paymentStatus} /> },
                ]
              : view === 'warehouses'
                ? [
                    { key: 'code', label: 'Mã kho' },
                    { key: 'name', label: 'Tên kho' },
                    { key: 'factoryName', label: 'Nhà máy' },
                    { key: 'active', label: 'Trạng thái', render: (row) => <StatusBadge value={row.active} /> },
                  ]
                : [
                    { key: 'transactionNo', label: 'Số phiếu' },
                    { key: 'transactionDate', label: 'Ngày' },
                    { key: 'transactionType', label: 'Loại' },
                    { key: 'warehouseName', label: 'Kho' },
                    { key: 'materialName', label: 'Vật tư' },
                    { key: 'quantity', label: 'Số lượng' },
                    { key: 'totalAmount', label: 'Thành tiền', render: (row) => money(row.totalAmount) },
                  ]
          }
        />
      </Panel>

      {open && (
        <div className="modal-backdrop">
          <div className="modal admin-modal">
            <div className="admin-modal-header">
              <h2>Nhập {view === 'finance' ? 'giao dịch tài chính' : view === 'warehouses' ? 'kho' : 'giao dịch kho'}</h2>
              <button className="modal-close" onClick={() => setOpen(false)}>×</button>
            </div>
            <div className="form-grid">
              {view === 'finance' ? (
                <>
                  {select('categoryId', 'Danh mục', options.categories)}
                  {select('factoryId', 'Nhà máy', options.factories)}
                  <Input fieldKey="recordDate" label="Ngày" type="date" form={form} setForm={setForm} />
                  <Input fieldKey="amount" label="Số tiền" type="number" form={form} setForm={setForm} />
                  <Input fieldKey="paidAmount" label="Đã thanh toán" type="number" form={form} setForm={setForm} />
                  <Input fieldKey="dueDate" label="Hạn thanh toán" type="date" form={form} setForm={setForm} />
                  <Input fieldKey="counterparty" label="Đối tác" form={form} setForm={setForm} />
                  <Input fieldKey="referenceNo" label="Số chứng từ" form={form} setForm={setForm} />
                </>
              ) : view === 'warehouses' ? (
                <>
                  {select('factoryId', 'Nhà máy', options.factories)}
                  <Input fieldKey="code" label="Mã kho" form={form} setForm={setForm} />
                  <Input fieldKey="name" label="Tên kho" form={form} setForm={setForm} />
                  <Input fieldKey="description" label="Mô tả" form={form} setForm={setForm} />
                </>
              ) : (
                <>
                  {select('warehouseId', 'Kho', options.warehouses)}
                  {select('materialId', 'Vật tư', options.materials)}
                  <Input fieldKey="transactionDate" label="Ngày" type="date" form={form} setForm={setForm} />
                  <label className="field">
                    <span>Loại</span>
                    <select
                      value={form.transactionType ?? ''}
                      onChange={(event) => setForm({ ...form, transactionType: event.target.value })}
                    >
                      {['', 'INBOUND', 'OUTBOUND', 'ADJUSTMENT_IN', 'ADJUSTMENT_OUT'].map((value) => (
                        <option key={value}>{value}</option>
                      ))}
                    </select>
                  </label>
                  <Input fieldKey="quantity" label="Số lượng" type="number" form={form} setForm={setForm} />
                  <Input fieldKey="unitCost" label="Đơn giá" type="number" form={form} setForm={setForm} />
                </>
              )}
            </div>
            <div className="form-actions">
              <button onClick={() => setOpen(false)}>Hủy</button>
              <button className="primary" onClick={() => void save()}>Lưu</button>
            </div>
          </div>
        </div>
      )}
    </>
  )
}

function Input({
  fieldKey,
  label,
  type = 'text',
  form,
  setForm,
}: {
  fieldKey: string
  label: string
  type?: string
  form: FormState
  setForm: (next: FormState) => void
}) {
  return (
    <label className="field">
      <span>{label}</span>
      <input
        type={type}
        value={form[fieldKey] ?? ''}
        onChange={(event) => setForm({ ...form, [fieldKey]: event.target.value })}
      />
    </label>
  )
}
