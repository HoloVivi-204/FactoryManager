# Factory API

Base URL: `http://localhost:8080/factory-management/api/v1/factories`

| Chức năng | Method | URL |
| --- | --- | --- |
| Tạo | POST | `/factories` |
| Danh sách hoạt động | GET | `/factories/all` |
| Lấy theo ID | GET | `/factories/{id}` |
| Cập nhật từng trường | PUT | `/factories/{id}` |
| Xóa mềm | DELETE | `/factories/{id}` |

## Tạo Factory

```json
{
  "code": "FAC001",
  "name": "Nhà máy Bình Dương",
  "address": "Khu công nghiệp Sóng Thần, Bình Dương",
  "active": true
}
```

Mã Factory là duy nhất toàn hệ thống và được chuẩn hóa thành chữ hoa.
