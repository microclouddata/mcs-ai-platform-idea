export type Agent = {
  id: string;
  userId: string;
  name: string;
  description: string;
  systemPrompt: string;
  provider: string;
  model: string;
  temperature: number;
  tools: string[];
  enabled: boolean;
  memoryEnabled: boolean;
  toolsEnabled: boolean;
};

export type ChatMessage = {
  id: string;
  role: string;
  content: string;
  createdAt: string;
};

export type AuthResponse = {
  token: string;
  userId: string;
  email: string;
  name: string;
};

export type ChatResponse = {
  sessionId: string;
  answer: string;
};

export type DocumentFile = {
  id: string;
  fileName: string;
  status: string;
  size: number;
};

export type AgentToolBinding = {
  id: string;
  agentId: string;
  toolType: string;
  enabled: boolean;
};

export type UsageLog = {
  id: string;
  userId: string;
  agentId: string;
  sessionId: string;
  provider: string;
  model: string;
  promptTokens: number;
  completionTokens: number;
  totalTokens: number;
  createdAt: string;
};

export type User = {
  id: string;
  email: string;
  name: string;
  role: string;
  createdAt: string;
};

export type UsageStatsResponse = {
  totalRequests: number;
  totalTokens: number;
  totalCost: number;
  tokensByAgent: Record<string, number>;
  tokensByModel: Record<string, number>;
  costByModel: Record<string, number>;
};

// Phase 4 types

export type OrgRole = 'OWNER' | 'ADMIN' | 'MEMBER';
export type PlanType = 'FREE' | 'PRO' | 'ENTERPRISE';
export type IntegrationType = 'SLACK' | 'WHATSAPP' | 'EMAIL' | 'WEBHOOK' | 'GOOGLE_DRIVE' | 'NOTION' | 'CONFLUENCE' | 'CRM';

export interface Organization {
  id: string;
  name: string;
  description?: string;
  slug?: string;
  plan: string;
  ownerId: string;
  createdAt?: string;
}

export interface OrgMembership {
  id: string;
  orgId: string;
  userId: string;
  userEmail: string;
  userName: string;
  role: OrgRole;
  invitedBy?: string;
  createdAt?: string;
}

export interface Subscription {
  id: string;
  userId: string;
  plan: PlanType;
  status: string;
  periodStart?: string;
  periodEnd?: string;
}

export interface PlanLimits {
  maxAgents: number;
  maxRequestsPerDay: number;
  maxDocuments: number;
}

export interface Integration {
  id: string;
  userId: string;
  type: IntegrationType;
  name: string;
  config: Record<string, string>;
  enabled: boolean;
  lastTriggeredAt?: string;
  createdAt?: string;
}

// Skill types
export type SkillStatus = 'ACTIVE' | 'INACTIVE';

export interface Skill {
  id: string;
  agentId: string;
  name: string;
  description?: string;
  license?: string;
  compatibility?: string;
  skillMetadata?: Record<string, string>;
  allowedTools?: string[];
  instructions?: string;
  status: SkillStatus;
  modelTool: boolean;
  createdAt?: string;
}
