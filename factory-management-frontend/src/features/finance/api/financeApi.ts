import { get, send } from '../../../shared/api/client'
import type { TableRow } from '../../../shared/types'

export type FinanceSummary = {
  totalRevenue?: number
  totalExpense?: number
  profit?: number
  accountsReceivable?: number
  accountsPayable?: number
}

export type FinanceFormPayload = Record<string, unknown>

export const financeApi = {
  summary: (query = '') => get<FinanceSummary>(`/financial-records/dashboard${query}`),
  records: (query = '') => get<TableRow[]>(`/financial-records/search${query}`),
  categories: () => get<TableRow[]>('/financial-categories/all'),
  factories: () => get<TableRow[]>('/factories/all'),
  departments: () => get<TableRow[]>('/departments/all'),
  lines: () => get<TableRow[]>('/production-lines/all'),
  materials: () => get<TableRow[]>('/materials/all'),
  warehouses: () => get<TableRow[]>('/warehouses/all'),
  createRecord: (data: FinanceFormPayload) => send('/financial-records', 'POST', data),
  createWarehouse: (data: FinanceFormPayload) => send('/warehouses', 'POST', data),
  transactions: (query = '') => get<TableRow[]>(`/inventory/transactions${query}`),
  stocks: (query = '') => get<TableRow[]>(`/inventory/stocks${query}`),
  createTransaction: (data: FinanceFormPayload) => send('/inventory/transactions', 'POST', data),
}
