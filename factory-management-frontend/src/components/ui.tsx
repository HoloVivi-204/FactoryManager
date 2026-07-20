import type { ReactNode } from 'react'

export function Panel({title,children,action}:{title:string;children:ReactNode;action?:ReactNode}){return <section className="panel"><header><h2>{title}</h2>{action}</header>{children}</section>}
export function KpiCard({label,value,hint,tone='blue'}:{label:string;value:string;hint:string;tone?:string}){return <article className={`kpi ${tone}`}><span>{label}</span><strong>{value}</strong><small>{hint}</small></article>}
export function StatusBadge({value}:{value:unknown}){const text=String(value??'—');return <span className={`badge ${text.toLowerCase()}`}>{text}</span>}
export function LoadingState({loading,error}:{loading:boolean;error?:string}){if(loading)return <div className="state">Đang tải dữ liệu…</div>;if(error)return <div className="state error">{error}</div>;return null}
export function DataTable({columns,rows}:{columns:{key:string;label:string;render?:(row:any)=>ReactNode}[];rows:any[]}){return <div className="table-wrap"><table><thead><tr>{columns.map(c=><th key={c.key}>{c.label}</th>)}</tr></thead><tbody>{rows.map((r,i)=><tr key={r.id??i}>{columns.map(c=><td key={c.key}>{c.render?c.render(r):String(r[c.key]??'—')}</td>)}</tr>)}{!rows.length&&<tr><td colSpan={columns.length} className="empty">Chưa có dữ liệu</td></tr>}</tbody></table></div>}
