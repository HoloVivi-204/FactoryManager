# Chatbot AI có kiểm soát quyền dữ liệu

Chatbot là lớp điều phối phía trên các service Spring Boot. Mô hình AI không có tài khoản PostgreSQL, không nhận schema database và không được thực thi SQL. Mô hình chỉ chọn một Tool trong danh sách backend cho phép; Spring Boot kiểm tra JWT, vai trò và phạm vi dữ liệu trước khi truy vấn.

```text
React / Postman
      │ JWT
      ▼
AiChatController
      ▼
AiChatService
      ├── Ollama local: chọn Tool
      ▼
AiToolRegistry
      ▼
AuthorizationScope: role + factory/department/line/team
      ▼
Repository/Service → dữ liệu tổng hợp
      ▼
Ollama local: diễn giải kết quả
```

## Cấu hình

Chatbot dùng Ollama chạy trên máy, không cần API key hay credit. Cài Ollama, tải model rồi cấu hình `.env`:

```powershell
ollama pull qwen3:4b
ollama run qwen3:4b
```

```properties
AI_ENABLED=true
OLLAMA_CHAT_URL=http://localhost:11434/api/chat
OLLAMA_MODEL=qwen3:4b
AI_MAX_TOOL_CALLS=3
AI_MAX_OUTPUT_TOKENS=1200
AI_CONNECT_TIMEOUT_SECONDS=5
AI_READ_TIMEOUT_SECONDS=120
AI_REQUESTS_PER_MINUTE=10
```

`qwen3:4b` phù hợp máy RAM khoảng 8 GB. Máy từ 16 GB có thể đổi thành `qwen3:8b` để cải thiện khả năng chọn Tool và diễn giải tiếng Việt. Model phải hỗ trợ tool calling.

Không cần chạy `ollama run` liên tục nếu ứng dụng Ollama trên Windows đã chạy nền. Có thể kiểm tra bằng `ollama list`.

## API

```http
POST /api/v1/ai-chat/messages
Authorization: Bearer <access-token>
Content-Type: application/json
```

Với context path mặc định:

```text
http://localhost:8080/factory-management/api/v1/ai-chat/messages
```

Request:

```json
{
  "message": "Hôm nay sản lượng dây chuyền Line A là bao nhiêu?",
  "workspaceRole": "PRODUCTION_MANAGER"
}
```

`workspaceRole` không bắt buộc. Nếu truyền, giá trị phải là role thực sự có trong JWT/tài khoản và chỉ được dùng để thu hẹp quyền. Backend không chấp nhận role do frontend tự khai nếu tài khoản chưa được cấp.

Các giá trị hiện có:

```text
ADMIN
DIRECTOR
FACTORY_MANAGER
DEPARTMENT_MANAGER
FINANCE
PRODUCTION_MANAGER
TEAM_LEADER
EMPLOYEE
```

Response mẫu:

```json
{
  "code": 1000,
  "message": "Chatbot đã trả lời",
  "result": {
    "answer": "Dây chuyền Line A hôm nay ghi nhận ...",
    "dataStatus": "OFFICIAL",
    "model": "qwen3:4b",
    "workspaceRole": "PRODUCTION_MANAGER",
    "toolsUsed": ["get_production_summary"],
    "sources": [
      {
        "type": "PRODUCTION_REPORT",
        "label": "Báo cáo sản xuất chính thức",
        "dataStatus": "OFFICIAL",
        "recordCount": 3,
        "asOf": "2026-07-15T19:30:00"
      }
    ],
    "warnings": [],
    "answeredAt": "2026-07-15T19:30:01"
  }
}
```

## Tool hiện có

### `get_production_summary`

Tổng hợp kế hoạch, thực tế, hàng đạt, hàng lỗi, thời gian làm việc, downtime, tỷ lệ hoàn thành, availability và chất lượng.

- Nguồn mặc định: `production_report` chính thức.
- Có thể lọc theo ngày, nhà máy, phòng ban, dây chuyền, tổ và máy bằng mã hoặc tên.
- Chỉ đọc staging khi câu hỏi yêu cầu rõ dữ liệu nháp/chưa chốt và vai trò được phép.
- Staging được trả riêng với nhãn `TEMPORARY_UNCONFIRMED`; không cộng vào số chính thức.

Ví dụ:

```json
{
  "message": "Sản lượng Line A ngày 2026-07-14 là bao nhiêu? Có bao nhiêu báo cáo tạm chưa chốt?",
  "workspaceRole": "TEAM_LEADER"
}
```

### `compare_financial_periods`

So sánh doanh thu, chi phí, lợi nhuận, công nợ phải thu/phải trả và nhóm chi phí giữa hai kỳ.

- Chỉ dùng `financial_record.active=true` và `status=POSTED`.
- Không lấy `DRAFT` hoặc `VOIDED`.
- Mỗi bản ghi tiếp tục được kiểm tra bằng `AuthorizationScope.canAccessFinancialScope(...)`.

### `analyze_productivity`

So sánh năng suất hàng đạt trên một giờ vận hành theo nhà máy, phòng ban, dây chuyền, tổ hoặc máy.

Backend đối chiếu các chi tiết chính thức:

- Downtime ngoài kế hoạch.
- Tỷ lệ sản phẩm lỗi.
- Sự cố vật tư.
- Lượt vắng/nghỉ, đi trễ/về sớm và tăng ca.

Kết quả chỉ gọi đây là yếu tố liên quan hoặc có khả năng đóng góp, không tự khẳng định quan hệ nhân quả.

### `rank_maintenance_cost`

Xếp hạng máy theo chi phí bảo trì đã hoàn thành:

```text
totalCost = laborCost + partCost + externalCost
```

Chỉ lấy work order `COMPLETED`, dùng `actualEnd` để xác định kỳ và giới hạn máy theo các tổ mà tài khoản được xem.

### `get_chatbot_capabilities`

Tool không đọc dữ liệu nghiệp vụ. Nó dùng cho lời chào, hướng dẫn và trường hợp role hiện tại chưa có Tool phù hợp. Vì mỗi lượt bắt buộc đi qua một Tool, mô hình không được tự trả lời số liệu doanh nghiệp mà không có dữ kiện backend.

## Phân quyền

| Tool | Vai trò |
|---|---|
| Sản lượng | ADMIN, DIRECTOR, FACTORY_MANAGER, DEPARTMENT_MANAGER, PRODUCTION_MANAGER, TEAM_LEADER, FINANCE |
| So sánh tài chính | ADMIN, DIRECTOR, FACTORY_MANAGER, FINANCE |
| Phân tích năng suất | ADMIN, DIRECTOR, FACTORY_MANAGER, DEPARTMENT_MANAGER, PRODUCTION_MANAGER, TEAM_LEADER, FINANCE |
| Chi phí bảo trì | ADMIN, DIRECTOR, FACTORY_MANAGER, PRODUCTION_MANAGER, FINANCE |
| Hướng dẫn chatbot | Tất cả role |

Danh sách role chỉ quyết định Tool nào được đưa cho mô hình. Dữ liệu bên trong Tool vẫn tiếp tục bị lọc bằng `AuthorizationScope` và `UserDataScope`.

Tài khoản có nhiều role nên gửi `workspaceRole` đang chọn trên frontend. Backend sử dụng `accessibleTeamIdsAsRole(...)` để không vô tình dùng quyền ADMIN trong màn hình TEAM_LEADER.

## Chính sách an toàn

- Không có Tool chạy SQL tự do.
- Không gửi JWT, mật khẩu, API key hoặc connection string cho mô hình.
- Chỉ gửi số liệu tổng hợp và tối đa 20–30 nhóm kết quả, không gửi toàn bộ entity.
- Function schema được gửi cho Ollama; mọi tham số vẫn được backend parse, kiểm tra và giới hạn lại.
- Mỗi khoảng truy vấn tối đa 366 ngày cho một kỳ.
- Mặc định tối đa 10 câu hỏi/phút/tài khoản trên mỗi instance backend.
- Timeout kết nối và đọc được cấu hình riêng.
- Mỗi lượt thành công tạo audit event `AI_QUERY`; audit chỉ lưu Tool, trạng thái dữ liệu, số bản ghi và kích thước scope, không lưu nguyên câu hỏi/câu trả lời nhạy cảm.
- Ollama mặc định chạy tại `localhost`; không gửi Tool result ra nhà cung cấp AI bên ngoài.
- Phiên bản đầu chỉ đọc; chatbot không được tạo, sửa, duyệt hoặc xóa dữ liệu.

## Mã lỗi chatbot

| Code | Ý nghĩa |
|---:|---|
| 1800 | Chưa bật hoặc thiếu URL/model Ollama |
| 1801 | Không kết nối được Ollama; kiểm tra ứng dụng Ollama và model đã tải |
| 1802 | Dịch vụ AI trả response không hợp lệ |
| 1803 | Tool không tồn tại hoặc role không được dùng |
| 1804 | Tham số Tool không hợp lệ |
| 1805 | Khoảng ngày vượt giới hạn |
| 1806 | Quá nhiều Tool trong một lượt |
| 1807 | `workspaceRole` không thuộc tài khoản |
| 1808–1809 | Câu hỏi rỗng hoặc dài quá 2000 ký tự |
| 1810 | Vượt giới hạn câu hỏi/phút |

## Giới hạn phiên bản hiện tại

- API đang stateless, chưa lưu conversation/message.
- Chưa streaming câu trả lời.
- Model local nhỏ có thể chọn sai Tool thường xuyên hơn model cloud; backend sẽ từ chối câu trả lời không có Tool result.
- Role EMPLOYEE chưa có Tool lịch làm, chấm công, KPI, nghỉ phép và tăng ca cá nhân.
- Chưa có RAG cho quy trình/SOP/tài liệu; các câu hỏi số liệu không cần vector database.
- Rate limit hiện lưu trong RAM từng backend instance. Khi chạy nhiều instance nên chuyển sang Redis hoặc API gateway.

Giai đoạn frontend nên gắn một drawer chat vào `DashboardLayout`, gửi JWT hiện tại cùng `workspaceRole` và hiển thị rõ `dataStatus`, `sources`, `warnings` từ response.
