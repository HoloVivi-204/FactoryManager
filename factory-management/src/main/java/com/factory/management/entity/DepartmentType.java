package com.factory.management.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DepartmentType {
    PRODUCTION(
            "SX",
            "Xưởng sản xuất",
            "Phụ trách tổ chức, vận hành và giám sát hoạt động sản xuất."
    ),
    QUALITY(
            "QA",
            "Phòng quản lý chất lượng",
            "Kiểm soát chất lượng nguyên vật liệu, quy trình sản xuất và thành phẩm."
    ),
    MAINTENANCE(
            "BT",
            "Phòng bảo trì",
            "Bảo trì, sửa chữa máy móc và thiết bị của nhà máy."
    ),
    WAREHOUSE(
            "KHO",
            "Bộ phận kho",
            "Quản lý nhập, xuất, lưu trữ và tồn kho vật tư, thành phẩm."
    ),
    FINANCE(
            "TC",
            "Phòng tài chính kế toán",
            "Quản lý chi phí, doanh thu, công nợ và báo cáo tài chính."
    ),
    HUMAN_RESOURCES(
            "NS",
            "Phòng nhân sự",
            "Quản lý tuyển dụng, hồ sơ, chấm công và chính sách nhân sự."
    );

    private final String codeSuffix;
    private final String displayName;
    private final String defaultDescription;
}
