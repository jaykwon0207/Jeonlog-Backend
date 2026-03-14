CREATE TABLE IF NOT EXISTS ugc_reports (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reporter_id BIGINT NOT NULL,
    reported_user_id BIGINT NOT NULL,
    target_type VARCHAR(20) NOT NULL,
    target_id BIGINT NOT NULL,
    reason VARCHAR(30) NOT NULL,
    detail VARCHAR(1000) NULL,
    status VARCHAR(20) NOT NULL,
    action VARCHAR(30) NOT NULL,
    admin_memo VARCHAR(1000) NULL,
    created_at DATETIME NOT NULL,
    due_at DATETIME NOT NULL,
    processed_at DATETIME NULL,
    offender_notice_read_at DATETIME NULL,
    CONSTRAINT fk_ugc_reports_reporter FOREIGN KEY (reporter_id) REFERENCES users(id),
    CONSTRAINT fk_ugc_reports_reported_user FOREIGN KEY (reported_user_id) REFERENCES users(id)
);

CREATE INDEX idx_ugc_reports_status_created_at ON ugc_reports(status, created_at);
CREATE INDEX idx_ugc_reports_status_due_at ON ugc_reports(status, due_at);
CREATE INDEX idx_ugc_reports_target ON ugc_reports(target_type, target_id);
