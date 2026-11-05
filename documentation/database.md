# Database and local storage

### ER diagram

[Link to diagram](https://mermaid.live/edit#pako:eNqtVd9v2jAQ_lcsP20SZeU3ibQHBOnE2rVdk0pbhWR5iUksJXZmO2sZ8L_vnBBaIGwvy1Ps7-6789139hqHMmLYxUzNOI0VzRYCwff1cf5EfM_353e3aLO5uJBr2PP8ANZkEgTel_sAuWiBE6rRO7CZeTde4KHpxJ9OZt77Ba54ppPA-3T38B1tZLtdcrzhtf48-iBoxpAWNNeJNC0kJLq6rv0P7NfVnv3mt8DrPSAeofvr1-3A-xagGPhIUQC0wI-3c0i7ZtubZHBoQMOUas1DtEEZVdQkUry1rGOE1LBYqhUpGUWRpvRHytBHRMXqhHlvbE91hEV8ueRhkZrVEfCzYNpwKYhZ5ew0AW2oMiwi1ED8uYClMCjTTbkuueA6-bttGTKVIU1JBNmC3Y1dzOz_3L9roo0KKI9NULNQikifWlChn5kihkPtMw2cusiQXKJyQ-dMGNKcsZEGEqkr0MAcSqVYaEgoC2FO4WclRVyBVk4iTIuI6TKuLExzyAo7x6ghIiM5UyFrgumvmOxOm-mjqjIREcWoBiGVwLZW8dHk_FvJ-2QYKBQKD_BVA5xLzW3d9lJHz9wkb7xOGv-qNfZi_od4DzVbbsm87CUk9dmHudVGcRGjlGtzOi-77lYVPQI1SwGzai7Rw-nbdbGpwVyTHW9z818FeUbH5wdoe3SvneskOE_u56ihAYe1rZ2K3M7i-bC4hWPFI-waVbAWzpjKqF3iMoEFNgkDWmyvVMi-eLkIZSqVdbeuORVPUma1t5JFnGB3SVMNqyr07vrfm4CUmZraCcHuqF9SYHeNX7Dbvxy2-_1BdzByep3uoNMdtvAKu-Nuezjs9Jxeb3Q5GPWd8baFf5dBL9uj7rjjOOOeM3A6zriFWcSNVF-qp6d8gbZ_ALYD9g8)

```mermaid
erDiagram
    QUIZ_SESSION ||--o{ QUESTION_ATTEMPT : "has (ON DELETE CASCADE)"
    CATEGORY |o..o{ QUIZ_SESSION : "id/name snapshot, no FK"
    QUIZ_SESSION {
        INTEGER id PK
        TEXT game_uuid "UNIQUE"
        TEXT mode "classic | marathon"
        INTEGER category_id "nullable = any"
        TEXT category_name
        TEXT difficulty
        TEXT question_type
        INTEGER started_at "Instant ms"
        INTEGER finished_at "Instant ms"
        TEXT local_date "LocalDate ISO"
        INTEGER duration_seconds
        INTEGER answer_time_ms "sum of time_spent_ms"
        INTEGER total_questions
        INTEGER correct_count
        INTEGER wrong_count "includes timeouts"
        INTEGER timeout_count
        INTEGER score_percent
        INTEGER avg_answer_ms
        TEXT end_reason
    }
    QUESTION_ATTEMPT {
        INTEGER id PK
        INTEGER session_id FK
        INTEGER position "UNIQUE with session_id"
        TEXT question_text
        TEXT category_name
        TEXT difficulty
        TEXT type
        TEXT options "JSON string list"
        TEXT correct_answer
        TEXT selected_answer "nullable = timeout"
        INTEGER is_correct
        INTEGER time_spent_ms
        INTEGER answered_at "Instant ms"
    }
    CATEGORY {
        INTEGER id PK "API id"
        TEXT name
        INTEGER updated_at "Instant ms"
    }
```

### Tables

- **`quiz_session`**: one row per finished game, with its configuration, timestamps, duration and
  totals.
- **`question_attempt`**: one row per question asked in a game (the 1:N relation). It stores the
  question, the options in the order they were shown, the correct answer, the player's answer
  (`null` for a timeout) and the time spent.
- **`category`**: a cache of the Open Trivia DB category list.