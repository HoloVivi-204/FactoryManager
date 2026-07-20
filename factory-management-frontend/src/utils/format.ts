export const nf=new Intl.NumberFormat('vi-VN')
export const number=(value?:number)=>nf.format(value??0)
export const percent=(value?:number)=>`${Number(value??0).toFixed(2)}%`
export const text=(value:unknown)=>value==null||value===''?'—':String(value)
