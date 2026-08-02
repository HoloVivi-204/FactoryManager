import { downloadFile, uploadFile } from './client'

export type StagingExcelImportError = {
  sheet: string
  row: number
  column: string
  code: string
  message: string
  value?: string
}

export type StagingExcelImportResult = {
  fileName: string
  valid: boolean
  imported: boolean
  totalRows: number
  productionRows: number
  employeeRows: number
  downtimeRows: number
  qualityRows: number
  materialRows: number
  createdReports: number
  updatedReports: number
  createdDetails: number
  updatedDetails: number
  errors: StagingExcelImportError[]
}

export const stagingExcelApi = {
  template: () => downloadFile('/staging-report-excel/template', 'mau-nhap-bao-cao-ca.xlsx'),
  preview: (file: File) =>
    uploadFile<StagingExcelImportResult>('/staging-report-excel/preview', file),
  import: (file: File) =>
    uploadFile<StagingExcelImportResult>('/staging-report-excel/import', file, true),
}
