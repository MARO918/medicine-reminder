# お薬リマインダー (medicine-reminder)

お薬の服薬時間を設定し、LINE で「お薬の時間ですよ♡」と通知するアプリ。

## Phase 1（現在）

- お薬の CRUD（名前・メモ・服薬時間）
- 1 つのお薬あたり服薬時間 1〜6 回
- H2 データベースで永続化
- 開発用ユーザー（LINE Login は Phase 2）

## Tech Stack

- Frontend: React + Vite
- Backend: Spring Boot 3 + JPA
- Database: H2（ローカル）/ PostgreSQL（本番予定）

## Run Backend

```bash
cd backend
mvn spring-boot:run
```

Runs at: `http://localhost:8080`

## Run Frontend

```bash
cd frontend
npm install
npm run dev
```

Runs at: `http://localhost:5173`

## API

- `GET /api/medicines` - お薬一覧
- `GET /api/medicines/today` - 今日の服薬予定
- `GET /api/medicines/{id}` - お薬詳細
- `POST /api/medicines` - お薬登録
- `PUT /api/medicines/{id}` - お薬更新
- `DELETE /api/medicines/{id}` - お薬削除

### Request example

```json
{
  "name": "降圧剤",
  "memo": "食後",
  "schedules": ["08:00", "20:00"],
  "active": true
}
```

## Next Phases

- Phase 2: LINE Login
- Phase 3: LINE 友だち追加フロー
- Phase 4: LINE Push 通知（「お薬の時間ですよ♡」）
- Phase 5: UI 仕上げ・本番デプロイ
