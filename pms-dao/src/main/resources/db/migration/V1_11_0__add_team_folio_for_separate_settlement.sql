-- V1_11_0__add_team_folio_for_separate_settlement.sql
-- 为分开结算但没有 team_folio 的团队预定补充创建 team_folio

-- 为分开结算但没有 team_folio 的团队预定补充创建
INSERT INTO team_folio (hotel_id, team_reservation_id, folio_no, total_amount, paid_amount, balance, status, created_at, updated_at, deleted, version)
SELECT 
    tr.hotel_id,
    tr.id as team_reservation_id,
    CONCAT('TF', TO_CHAR(NOW(), 'YYYYMMDD'), LPAD(CAST(ROW_NUMBER() OVER (ORDER BY tr.id) AS TEXT), 4, '0')) as folio_no,
    tr.total_amount,
    COALESCE(
        (SELECT SUM(f.paid_amount) FROM folio f 
         JOIN stay s ON f.stay_id = s.id 
         WHERE s.team_reservation_id = tr.id), 
        0
    ) as paid_amount,
    tr.total_amount - COALESCE(
        (SELECT SUM(f.paid_amount) FROM folio f 
         JOIN stay s ON f.stay_id = s.id 
         WHERE s.team_reservation_id = tr.id), 
        0
    ) as balance,
    'OPEN' as status,
    NOW() as created_at,
    NOW() as updated_at,
    FALSE as deleted,
    0 as version
FROM team_reservation tr
WHERE tr.settlement_type = 'SEPARATE'
AND NOT EXISTS (
    SELECT 1 FROM team_folio tf 
    WHERE tf.team_reservation_id = tr.id
);

