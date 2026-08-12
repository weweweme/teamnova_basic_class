<?php
// ============================================================
// config.php — 비밀 설정값 (API 키, DB 비번 등)
//   ★★ 이 파일은 Git에 올리지 않는다 (.gitignore 처리).
//      키·비번 같은 '비밀'은 코드와 분리해 관리하는 게 실무 원칙.
//      → 다른 사람은 config.example.php를 보고 자기 키로 이 파일을 만든다.
// ============================================================

// ── TMDB (영화·드라마 API) ──────────────────────────────────
//   v4 Read Access Token — 요청 헤더에 "Authorization: Bearer ..." 로 보낸다.
//   읽기 전용 토큰이라 검색·조회만 가능 (쓰기 권한 없음).
const TMDB_TOKEN = 'eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiJmMDYwZTlmYzhjZjllYzk2YmMyZTM0Zjc5OTAxYzMwYyIsIm5iZiI6MTc4NTE0NzM1Mi45NzksInN1YiI6IjZhNjcyZmQ4NmZiMDc2M2U3Mzk3ZmZkMSIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.rQChMikxT0pxMU89a-6cTdBX5JSTGBkMUDf5YyHKp9Q';

// ── MariaDB 접속 정보 ───────────────────────────────────────
const DB_HOST = '127.0.0.1';
const DB_PORT = 3306;
const DB_NAME = 'review_community';
const DB_USER = 'dev';
const DB_PASS = 'dev1234';
