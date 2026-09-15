-- 为 hotel_config 表添加 deleted 和 version 字段
ALTER TABLE hotel_config ADD COLUMN IF NOT EXISTS deleted BOOLEAN DEFAULT FALSE;
ALTER TABLE hotel_config ADD COLUMN IF NOT EXISTS version INTEGER DEFAULT 0;
