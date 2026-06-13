-- ============================================================================
-- Database Index Migration for XenoPilot AI Application
-- Version: V001 - Performance Optimization Phase
-- Database: PostgreSQL 16
-- Description: Add strategic indexes for production performance optimization
--              focused on read-heavy queries (analytics, campaign details, 
--              communication tracking)
-- ============================================================================

-- ============================================================================
-- CAMPAIGNS TABLE INDEXES
-- ============================================================================
-- Index on campaign_id for efficient foreign key lookups
-- Used by: Communication table joins, cascade operations
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_campaigns_id 
    ON campaigns(id);

-- Composite index on (campaign_id, status) for launch and status queries
-- Used by: Campaign launch queries, analytics filtering by status
-- Query pattern: WHERE campaign_id = ? AND status = ?
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_campaigns_campaign_id_status 
    ON campaigns(id, status);

-- Index on segment_id for segment-scoped campaign queries
-- Used by: Get campaigns by segment, segment analysis
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_campaigns_segment_id 
    ON campaigns(segment_id);

-- Index on status for campaign lifecycle filtering
-- Used by: Dashboard queries (DRAFT, LAUNCHED, COMPLETED), analytics
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_campaigns_status 
    ON campaigns(status);

-- ============================================================================
-- COMMUNICATIONS TABLE INDEXES
-- ============================================================================
-- Index on campaign_id for efficient campaign communication retrieval
-- Used by: Get all communications for a campaign, campaign analytics
-- Query pattern: SELECT * FROM communications WHERE campaign_id = ?
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_communications_campaign_id 
    ON communications(campaign_id);

-- Index on customer_id for customer communication history
-- Used by: Get communication history per customer, customer profile
-- Query pattern: SELECT * FROM communications WHERE customer_id = ?
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_communications_customer_id 
    ON communications(customer_id);

-- Index on communication_id for foreign key lookups
-- Used by: CommunicationEvent table joins, cascade operations
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_communications_id 
    ON communications(id);

-- Composite index on (campaign_id, status) for campaign analytics
-- Used by: Analytics queries filtering by campaign and status
-- Query pattern: SELECT COUNT(*) FROM communications WHERE campaign_id = ? AND status = ?
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_communications_campaign_id_status 
    ON communications(campaign_id, status);

-- Composite index on (customer_id, campaign_id) for deduplication and lookups
-- Used by: Check if customer already in campaign, customer-campaign analysis
-- Query pattern: SELECT * FROM communications WHERE customer_id = ? AND campaign_id = ?
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_communications_customer_id_campaign_id 
    ON communications(customer_id, campaign_id);

-- Index on status for communication lifecycle filtering
-- Used by: PENDING status queries, completion tracking
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_communications_status 
    ON communications(status);

-- Index on created_at for time-series queries and range filtering
-- Used by: Time-based analytics, communication reporting by date range
-- Query pattern: SELECT * FROM communications WHERE created_at >= ? AND created_at <= ?
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_communications_created_at 
    ON communications(created_at DESC);

-- ============================================================================
-- COMMUNICATION_EVENTS TABLE INDEXES
-- ============================================================================
-- Index on communication_id for event timeline retrieval
-- Used by: Get event history for a communication, event tracking
-- Query pattern: SELECT * FROM communication_events WHERE communication_id = ?
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_communication_events_communication_id 
    ON communication_events(communication_id);

-- Index on event_type for filtering specific events
-- Used by: Analytics on specific event types (sent, opened, clicked, bounced, etc.)
-- Query pattern: SELECT * FROM communication_events WHERE event_type = ?
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_communication_events_event_type 
    ON communication_events(event_type);

-- Index on timestamp for time-range queries
-- Used by: Event history filtering, timeline queries by date range
-- Query pattern: SELECT * FROM communication_events WHERE timestamp >= ? AND timestamp <= ?
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_communication_events_timestamp 
    ON communication_events(timestamp DESC);

-- Composite index on (communication_id, event_type) for event timeline filtering
-- Used by: Get specific event type for a communication
-- Query pattern: SELECT * FROM communication_events WHERE communication_id = ? AND event_type = ?
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_communication_events_communication_id_event_type 
    ON communication_events(communication_id, event_type);

-- Composite index on (communication_id, timestamp) for ordered event retrieval
-- Used by: Get events for communication in chronological order
-- Query pattern: SELECT * FROM communication_events WHERE communication_id = ? ORDER BY timestamp
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_communication_events_communication_id_timestamp 
    ON communication_events(communication_id, timestamp DESC);

-- ============================================================================
-- SEGMENTS TABLE INDEXES
-- ============================================================================
-- (Segment table doesn't have user_id in current implementation)
-- Index on id for efficient lookups
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_segments_id 
    ON segments(id);

-- ============================================================================
-- CUSTOMERS TABLE INDEXES
-- ============================================================================
-- (Customer table doesn't have user_id in current implementation)
-- Index on id for efficient lookups
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_customers_id 
    ON customers(id);

-- Index on email for user lookups and authentication
-- Used by: Login, user verification, duplicate prevention
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_customers_email 
    ON customers(email);

-- ============================================================================
-- APP_USERS TABLE INDEXES
-- ============================================================================
-- Index on id for efficient lookups
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_app_users_id 
    ON app_users(id);

-- Index on email for user lookups and authentication
-- Used by: Login, user verification, duplicate prevention
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_app_users_email 
    ON app_users(email);

-- ============================================================================
-- INDEX STATISTICS AND ANALYSIS
-- ============================================================================
-- Analyze the tables to update query planner statistics
ANALYZE campaigns;
ANALYZE communications;
ANALYZE communication_events;
ANALYZE segments;
ANALYZE customers;
ANALYZE app_users;

-- ============================================================================
-- NOTES ON INDEX DESIGN
-- ============================================================================
-- CONCURRENTLY option: Used to create indexes without blocking reads
-- DESC on created_at/timestamp: Optimizes for recent data queries
-- Composite indexes: Ordered to support multiple query patterns
-- 
-- Performance Impact Expected:
-- - Campaign queries: ~60-70% improvement for status/segment filtering
-- - Communication queries: ~50-80% improvement for analytics queries
-- - Event queries: ~40-60% improvement for timeline/event filtering
-- - Read operations overall: ~50% average improvement
-- 
-- Trade-offs:
-- - Increased storage (~15-20% more disk space for indexes)
-- - Slightly slower writes (~5-10%) due to index maintenance
-- - Worth the trade-off for read-heavy production workload
