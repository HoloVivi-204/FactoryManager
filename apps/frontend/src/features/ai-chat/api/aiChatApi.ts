import type { Role } from '../../../shared/types'
import type { AiChatRequest, AiChatResponse } from '../types/aiChat'
import { sendSilent } from '../../../shared/api/client'

export const aiChatApi = {
  ask(message: string, workspaceRole: Role) {
    const request: AiChatRequest = { message, workspaceRole }
    return sendSilent<AiChatResponse>('/ai-chat/messages', 'POST', request)
  },
}
