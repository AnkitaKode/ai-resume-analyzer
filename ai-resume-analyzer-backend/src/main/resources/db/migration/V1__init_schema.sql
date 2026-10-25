-- Baseline schema for resumes and chunks

CREATE TABLE IF NOT EXISTS resumes (
    id VARCHAR(255) PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL,
    file_type VARCHAR(50) NOT NULL,
    raw_text TEXT NOT NULL,
    file_size BIGINT NOT NULL,
    status VARCHAR(50),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS resume_chunks (
    id VARCHAR(255) PRIMARY KEY,
    resume_id VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    chunk_index INTEGER NOT NULL,
    embedding_json TEXT,
    CONSTRAINT fk_resume_chunks_resume FOREIGN KEY (resume_id) REFERENCES resumes(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_resume_chunks_resume_id ON resume_chunks(resume_id);
