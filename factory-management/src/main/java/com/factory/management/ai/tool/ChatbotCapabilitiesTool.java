package com.factory.management.ai.tool;

import com.factory.management.ai.AiDataTool;
import com.factory.management.ai.AiToolArguments;
import com.factory.management.ai.AiToolContext;
import com.factory.management.ai.AiToolResult;
import com.factory.management.entity.Role;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class ChatbotCapabilitiesTool implements AiDataTool {
    private static final Set<Role> ALL_ROLES = Set.of(Role.values());

    @Override
    public String name() {
        return "get_chatbot_capabilities";
    }

    @Override
    public String description() {
        return "Giải thích chatbot có thể hỗ trợ gì cho vai trò hiện tại. Dùng cho lời chào, câu hỏi hướng dẫn "
                + "hoặc khi người dùng hỏi một loại dữ liệu mà vai trò không được cấp.";
    }

    @Override
    public Map<String, Object> parametersSchema() {
        return AiToolArguments.objectSchema(Map.of());
    }

    @Override
    public Set<Role> allowedRoles() {
        return ALL_ROLES;
    }

    @Override
    public AiToolResult execute(Map<String, Object> arguments, AiToolContext context) {
        List<String> capabilities = new ArrayList<>();
        if (hasAny(context, Role.ADMIN, Role.DIRECTOR, Role.FACTORY_MANAGER,
                Role.DEPARTMENT_MANAGER, Role.PRODUCTION_MANAGER, Role.TEAM_LEADER, Role.FINANCE)) {
            capabilities.add("Tra cứu sản lượng chính thức theo ngày, nhà máy, dây chuyền, tổ hoặc máy.");
        }
        if (hasAny(context, Role.ADMIN, Role.DIRECTOR, Role.FACTORY_MANAGER, Role.FINANCE)) {
            capabilities.add("So sánh doanh thu, chi phí, lợi nhuận và công nợ giữa hai kỳ.");
        }
        if (hasAny(context, Role.ADMIN, Role.DIRECTOR, Role.FACTORY_MANAGER,
                Role.DEPARTMENT_MANAGER, Role.PRODUCTION_MANAGER, Role.TEAM_LEADER, Role.FINANCE)) {
            capabilities.add("Phân tích biến động năng suất và các yếu tố liên quan từ dữ liệu chính thức.");
        }
        if (hasAny(context, Role.ADMIN, Role.DIRECTOR, Role.FACTORY_MANAGER,
                Role.DEPARTMENT_MANAGER, Role.PRODUCTION_MANAGER, Role.TEAM_LEADER)) {
            capabilities.add("Tra cứu chi tiết máy dừng, nhân viên vắng/nghỉ, lỗi chất lượng và sự cố vật tư theo ngày.");
            capabilities.add("Mở dashboard KPI, biểu đồ và bảng chi tiết ngay dưới câu trả lời có dữ liệu vận hành.");
        }
        if (hasAny(context, Role.ADMIN, Role.DIRECTOR, Role.FACTORY_MANAGER,
                Role.PRODUCTION_MANAGER, Role.FINANCE)) {
            capabilities.add("Xếp hạng chi phí bảo trì máy đã hoàn thành.");
        }
        if (capabilities.isEmpty()) {
            capabilities.add("Vai trò hiện tại mới chỉ được dùng chatbot hướng dẫn; chưa có Tool dữ liệu cá nhân.");
        }
        return new AiToolResult(
                name(), "NO_BUSINESS_DATA",
                Map.of(
                        "effectiveRoles", context.effectiveRoles(),
                        "accessibleTeamCount", context.accessibleTeamIds().size(),
                        "capabilities", capabilities
                ),
                List.of(),
                List.of("Chatbot chỉ trả dữ liệu mà vai trò và phạm vi hiện tại được phép xem.")
        );
    }

    private boolean hasAny(AiToolContext context, Role... roles) {
        for (Role role : roles) if (context.effectiveRoles().contains(role)) return true;
        return false;
    }
}
