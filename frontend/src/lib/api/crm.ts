import { apiRequest } from "@/lib/api/client";

export type Page<T> = {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
};

export type Gender = "MALE" | "FEMALE" | "OTHER" | "UNSPECIFIED";

export type Customer = {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
  city?: string;
  age?: number;
  gender: Gender;
  createdAt: string;
};

export type CustomerInput = Omit<Customer, "id" | "createdAt">;

export type Order = {
  id: string;
  customerId: string;
  customerName: string;
  amount: number;
  category: string;
  purchaseDate: string;
};

export type OrderInput = Omit<Order, "id" | "customerName">;

export type Segment = {
  id: string;
  name: string;
  description?: string;
  filterJson: string;
  createdAt: string;
};

export type SegmentInput = Omit<Segment, "id" | "createdAt">;

export type CustomerProfile = {
  customer: Customer;
  totalSpend: number;
  averageOrderValue: number;
  orderCount: number;
  lastPurchaseDate?: string;
  daysSinceLastPurchase?: number;
  orders: Order[];
};

export type CsvPreview<T> = {
  records: Array<{ rowNumber: number; data: T | null; valid: boolean; errors: string[] }>;
  validCount: number;
  invalidCount: number;
};

export type DashboardStats = {
  totalCustomers: number;
  totalOrders: number;
  revenue: number;
  averageOrderValue: number;
};

export function dashboardStats() {
  return apiRequest<DashboardStats>("/dashboard/stats");
}

export function listCustomers(params: { search?: string; page?: number; size?: number }) {
  const query = new URLSearchParams({
    page: String(params.page ?? 0),
    size: String(params.size ?? 10),
    ...(params.search ? { search: params.search } : {})
  });
  return apiRequest<Page<Customer>>(`/customers?${query}`);
}

export function getCustomer(id: string) {
  return apiRequest<CustomerProfile>(`/customers/${id}`);
}

export function createCustomer(input: CustomerInput) {
  return apiRequest<Customer>("/customers", { method: "POST", body: JSON.stringify(input) });
}

export function updateCustomer(id: string, input: CustomerInput) {
  return apiRequest<Customer>(`/customers/${id}`, { method: "PUT", body: JSON.stringify(input) });
}

export function deleteCustomer(id: string) {
  return apiRequest<void>(`/customers/${id}`, { method: "DELETE" });
}

export function importCustomers(file: File, preview = false) {
  const body = new FormData();
  body.append("file", file);
  return apiRequest<CsvPreview<CustomerInput>>(preview ? "/customers/import/preview" : "/customers/import", { method: "POST", body });
}

export function listOrders(params: { customerId?: string; page?: number; size?: number }) {
  const query = new URLSearchParams({
    page: String(params.page ?? 0),
    size: String(params.size ?? 10),
    ...(params.customerId ? { customerId: params.customerId } : {})
  });
  return apiRequest<Page<Order>>(`/orders?${query}`);
}

export function createOrder(input: OrderInput) {
  return apiRequest<Order>("/orders", { method: "POST", body: JSON.stringify(input) });
}

export function updateOrder(id: string, input: OrderInput) {
  return apiRequest<Order>(`/orders/${id}`, { method: "PUT", body: JSON.stringify(input) });
}

export function deleteOrder(id: string) {
  return apiRequest<void>(`/orders/${id}`, { method: "DELETE" });
}

export function importOrders(file: File, preview = false) {
  const body = new FormData();
  body.append("file", file);
  return apiRequest<CsvPreview<OrderInput>>(preview ? "/orders/import/preview" : "/orders/import", { method: "POST", body });
}

export function listSegments(params: { page?: number; size?: number }) {
  const query = new URLSearchParams({ page: String(params.page ?? 0), size: String(params.size ?? 10) });
  return apiRequest<Page<Segment>>(`/segments?${query}`);
}

export function createSegment(input: SegmentInput) {
  return apiRequest<Segment>("/segments", { method: "POST", body: JSON.stringify(input) });
}

export function updateSegment(id: string, input: SegmentInput) {
  return apiRequest<Segment>(`/segments/${id}`, { method: "PUT", body: JSON.stringify(input) });
}

export function deleteSegment(id: string) {
  return apiRequest<void>(`/segments/${id}`, { method: "DELETE" });
}

export type CampaignStatus = "DRAFT" | "LAUNCHED" | "ARCHIVED";
export type CommunicationChannel = "EMAIL" | "SMS" | "WHATSAPP" | "RCS";
export type CommunicationStatus = "PENDING" | "SENT" | "DELIVERED" | "READ" | "CLICKED" | "CONVERTED" | "FAILED";
export type CommunicationEventType = "CREATED" | "SENT" | "DELIVERED" | "READ" | "CLICKED" | "CONVERTED" | "FAILED";

export type Campaign = {
  id: string;
  name: string;
  objective: string;
  segmentId: string;
  segmentName: string;
  status: CampaignStatus;
  channel: CommunicationChannel;
  message: string;
  reasoning?: string;
  createdAt: string;
  updatedAt: string;
};

export type CampaignInput = Omit<Campaign, "id" | "segmentName" | "createdAt" | "updatedAt">;

export type Communication = {
  id: string;
  campaignId: string;
  customerId: string;
  customerName: string;
  channel: CommunicationChannel;
  status: CommunicationStatus;
  message: string;
  createdAt: string;
};

export type CommunicationEvent = {
  id: string;
  communicationId: string;
  eventType: CommunicationEventType;
  timestamp: string;
};

export type CampaignMetrics = {
  audienceSize: number;
  communications: number;
  dispatched: number;
  delivered: number;
  failed: number;
};

export type CampaignAnalytics = {
  campaignId: string;
  campaignName: string;
  sent: number;
  delivered: number;
  read: number;
  clicked: number;
  converted: number;
  failed: number;
  total: number;
  deliveryRate: number;
  readRate: number;
  clickRate: number;
  conversionRate: number;
};

export type ChannelAnalytics = {
  campaignId: string;
  campaignName: string;
  channelMetrics: Array<{
    channel: CommunicationChannel;
    sent: number;
    delivered: number;
    read: number;
    clicked: number;
    total: number;
    deliveryRate: number;
    readRate: number;
    clickRate: number;
  }>;
};

export type Insights = {
  performanceSummary: string;
  reasonsForSuccess: string;
  reasonsForFailure: string;
  recommendedImprovements: string;
  nextBestAction: string;
};

export type CampaignCopilotResponse = {
  campaignName: string;
  campaignObjective: string;
  recommendedAudience: string;
  generatedFilters: string;
  recommendedChannel: CommunicationChannel;
  messageContent: string;
  campaignReasoning: string;
  estimatedAudienceSize: number;
};

export type AudienceBuilderResponse = {
  audienceSize: number;
  customers: Customer[];
  explanation: string;
  generatedFilters: string;
  segmentId?: string;
  segmentName?: string;
};

export function listCampaigns(params: { page?: number; size?: number }) {
  const query = new URLSearchParams({
    page: String(params.page ?? 0),
    size: String(params.size ?? 10),
  });
  return apiRequest<Page<Campaign>>(`/campaigns?${query}`);
}

export function getCampaign(id: string) {
  return apiRequest<Campaign>(`/campaigns/${id}`);
}

export function createCampaign(input: CampaignInput) {
  return apiRequest<Campaign>("/campaigns", { method: "POST", body: JSON.stringify(input) });
}

export function updateCampaign(id: string, input: CampaignInput) {
  return apiRequest<Campaign>(`/campaigns/${id}`, { method: "PUT", body: JSON.stringify(input) });
}


export function launchCampaign(id: string) {
  return apiRequest<{ campaignId: string; audienceSize: number; communicationsCreated: number; status: CampaignStatus }>(`/campaigns/${id}/launch`, { method: "POST" });
}

export function getCampaignMetrics(id: string) {
  return apiRequest<CampaignMetrics>(`/campaigns/${id}/metrics`);
}

export function getCampaignAnalytics(id: string) {
  return apiRequest<CampaignAnalytics>(`/analytics/campaigns/${id}`);
}

export function getChannelAnalytics(campaignId: string) {
  return apiRequest<ChannelAnalytics>(`/analytics/channels/${campaignId}`);
}

export function getCampaignInsights(campaignId: string) {
  return apiRequest<Insights>(`/insights/campaigns/${campaignId}`);
}

export function generateAudience(prompt: string, persistSegment: boolean = false, segmentName?: string) {
  return apiRequest<AudienceBuilderResponse>("/audience-builder", {
    method: "POST",
    body: JSON.stringify({ prompt, persistSegment, segmentName }),
  });
}

export function generateCampaignCopilot(goal: string) {
  return apiRequest<CampaignCopilotResponse>("/campaign-copilot", {
    method: "POST",
    body: JSON.stringify({ goal }),
  });
}

export function getCommunications(campaignId: string) {
  return apiRequest<Communication[]>(`/communications/campaign/${campaignId}`);
}

export function getCommunicationEvents(communicationId: string) {
  return apiRequest<CommunicationEvent[]>(`/communications/${communicationId}/events`);
}

export function archiveCampaign(id: string) {
  return apiRequest<void>(`/campaigns/${id}/archive`, {
    method: "PATCH",
  });
}