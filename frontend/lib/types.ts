export type Agent = {
  id: string;
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
  knowledgeBaseIds: string[];
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

export type KnowledgeBase = {
  id: string;
  name: string;
  description: string;
  documentCount: number;
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

export type WorkflowStep = {
  id: string;
  name: string;
  type: 'KNOWLEDGE_SEARCH' | 'WEB_SEARCH' | 'LLM_PROMPT' | 'SUMMARIZE' | 'HTTP_REQUEST';
  inputTemplate: string;
  outputKey: string;
  config: Record<string, string>;
};

export type Workflow = {
  id: string;
  name: string;
  description: string;
  agentId: string;
  steps: WorkflowStep[];
  enabled: boolean;
  createdAt: string;
};

export type StepResult = {
  stepId: string;
  stepName: string;
  status: 'SUCCESS' | 'FAILED' | 'SKIPPED';
  output: string;
  error: string;
  durationMs: number;
};

export type WorkflowExecution = {
  id: string;
  workflowId: string;
  status: 'PENDING' | 'RUNNING' | 'COMPLETED' | 'FAILED';
  stepResults: StepResult[];
  finalOutput: string;
  error: string;
  startedAt: string;
  finishedAt: string;
  createdAt: string;
};

export type ScheduledJob = {
  id: string;
  name: string;
  workflowId: string;
  scheduleType: 'HOURLY' | 'DAILY' | 'WEEKLY';
  enabled: boolean;
  lastRunAt: string;
  nextRunAt: string;
};

export type PromptTemplate = {
  id: string;
  name: string;
  description: string;
  content: string;
  variables: string[];
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
export type MarketplaceItemType = 'AGENT' | 'TEMPLATE' | 'WORKFLOW';

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

export interface ApiKey {
  id: string;
  name: string;
  keyPrefix: string;
  plainKey?: string;
  scopes: string[];
  enabled: boolean;
  lastUsedAt?: string;
  expiresAt?: string;
  createdAt?: string;
}

export interface AuditLog {
  id: string;
  userId: string;
  action: string;
  resourceType?: string;
  resourceId?: string;
  detail?: string;
  ipAddress?: string;
  createdAt?: string;
}

export interface MarketplaceItem {
  id: string;
  authorId: string;
  authorName: string;
  type: MarketplaceItemType;
  title: string;
  description?: string;
  category?: string;
  tags?: string[];
  config?: Record<string, unknown>;
  published: boolean;
  downloads: number;
  rating: number;
  createdAt?: string;
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
