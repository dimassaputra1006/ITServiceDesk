# Program Flow (Belajar Java untuk Pemula)

Dokumen ini menjelaskan **alur logika program** dari UI (tombol/komponen) sampai ke query database SQLite.
Tujuannya supaya kamu bisa “trace” kode saat memahami cara kerja aplikasi.

---

## 1) Gambaran Arsitektur (Siapa melakukan apa)

Ringkasnya dibagi 4 layer:

1. **UI layer (FXML + Controller)**
   - `layout_dashboard.fxml` mendefinisikan layout: `Ticket Queue`, `Active Room`, dan `System Feed`.
   - `TicketController` menangani event tombol dan mengisi komponen UI.

2. **Service layer (Business flow)**
   - `TicketService` adalah penghubung utama untuk aksi bisnis tiket.
   - Semua aksi tiket (create/claim/release/resolve/close) masuk ke sini.

3. **DAO layer (akses SQLite)**
   - `TicketDao` mengurus query `tickets`.
   - `ActivityLogDao` mengurus query `activity_log`.
   - `UserDao` untuk mencari analyst IT aktif (untuk selector analyst).

4. **Config/Seed layer**
   - `DatabaseConfig` membuat tabel.
   - `Seed` menghapus data lama lalu mengisi dummy data.

---

## 2) Skema Database yang Dipakai

Tabel utama:

### `tickets`
Kolom penting:
- `ticket_id` (TEXT, PK)
- `title` (TEXT)
- `description` (TEXT)
- `status` (TEXT): nilai yang dipakai program
  - `Open`
  - `In Progress`
  - `Resolved`
  - `Closed`
- `priority` (TEXT): `Critical`, `High`, `Medium`, `Low`
- `reporter_id` (TEXT)
- `assignee_id` (TEXT) (nullable)
- `created_at`, `updated_at`, `resolved_at` (TEXT)

### `activity_log`
Untuk mencatat aktivitas penting.
Kolom penting:
- `ticket_id`
- `analyst_id`
- `action` (mis: `CREATED`, `CLAIMED`, `RELEASED`, `RESOLVED`, `CLOSED`)
- `message`
- `created_at`

---

## 3) Urutan Startup / Setup Data

Biasanya kamu lakukan manual lewat `main()` berikut:

1. Jalankan:
   - `DatabaseConfig.main()`
   - Efek: membuat tabel `tickets`, `users`, dan `activity_log` (jika belum ada).

2. Jalankan:
   - `Seed.main()`
   - Efek:
     - menghapus isi tabel (`tickets`, `users`, dan `activity_log`)
     - membuat dummy users
     - membuat dummy tickets dan sebagian melakukan:
       - `claimTicket`
       - `resolveTicket`

3. Jalankan aplikasi:
   - `ITServiceDeskApp.main()`
   - Efek: memuat `layout_dashboard.fxml`, lalu `TicketController.initialize()`.

---

## 4) Alur UI Saat Aplikasi Jalan

Saat `TicketController.initialize()` dipanggil:

1. `setupFilters()`
   - isi combobox prioritas (mis. `Critical`, `High`, dll)

2. `setupAnalystSelector()`
   - ambil daftar IT staff aktif dari `UserDao.getActiveItStaff()`
   - isi `cbAnalyst`
   - saat analyst diganti: `refreshAll()`

3. `setupCreateTicket()`
   - tombol `btnNewTicket` buka dialog `openCreateTicketDialog()`

4. `refreshAll()` (inti refresh UI)
   - `updateTelemetry()`
   - `loadQueue()`
   - `loadActiveRoom()`
   - `loadHistoryFeed()`
   - `loadActivityLog()`

Catatan penting untuk pemula:
Semua load di atas adalah pemanggilan query DAO secara langsung, lalu hasilnya dipetakan menjadi komponen UI.

---

## 5) Ticket Lifecycle (Urutan Status)

1. **Create Ticket**
   - UI: dialog “+ NEW TICKET”
   - Controller: membuat objek `Ticket(title, description, reporterId, priority)`
     - di konstruktor `Ticket`, status otomatis:
       - `Open`
       - `assigneeId = null`
   - Service: `TicketService.createTicket(...)`
     - DAO: `TicketDao.insertTicket(ticket)`
     - Log: `activity_log` action = `CREATED`

2. **Claim Ticket (Open → In Progress)**
   - UI: tombol `▶ CLAIM` di Queue
   - Controller: `claimTicket(ticket)`
   - Service: `TicketService.claimTicket(ticketId, analystId)`
     - DAO: `TicketDao.claimTicket(...)`
       - update:
         - `assignee_id = analystId`
         - `status = 'In Progress'`
         - `updated_at = now`
       - hanya terjadi jika `assignee_id IS NULL`
     - Log: action = `CLAIMED`

3. **Release Ticket (In Progress → Open)**
   - UI: tombol `↩ RELEASE` di Active Room
   - Controller: `releaseTicket(ticket)`
   - Service: `TicketService.releaseTicket(...)`
     - DAO: `TicketDao.releaseTicket(...)`
       - update:
         - `assignee_id = NULL`
         - `status = 'Open'`
         - `updated_at = now`
       - hanya terjadi jika:
         - `assignee_id = analyst yang melakukan release`
         - status saat itu `In Progress`
     - Log: action = `RELEASED`

4. **Resolve Ticket (In Progress → Resolved)**
   - UI: tombol `✓ RESOLVE` di Active Room
   - Service: `TicketService.resolveTicket(...)`
     - DAO: `TicketDao.resolveTicket(...)`
       - update:
         - `status = 'Resolved'`
         - `resolved_at = now`
         - `updated_at = now`
     - Log: action = `RESOLVED`

5. **Close Ticket (Resolved → Closed)**
   - UI: tombol `✕ CLOSE` di Active Room
   - Service: `TicketService.closeTicket(...)`
     - DAO: `TicketDao.closeTicket(...)`
       - update: `status = 'Closed'`
     - Log: action = `CLOSED`

---

## 6) Bagian Panel UI yang Menggunakan Query Berbeda

### Ticket Queue (Open)
Sumber: `TicketDao.getQueueTickets()`
- filter: `status = 'Open'`
- urutan:
  1. prioritas Critical → High → Medium → Low
  2. `created_at ASC`

### Active Room (In Progress milik analyst)
Sumber: `TicketDao.getActiveTickets(analystId)`
- filter:
  - `status = 'In Progress'`
  - `assignee_id = analystId`
- urutan: `updated_at DESC`

### System Feed (history + activity log)
1. History feed: `TicketDao.getHistoryTickets()`
   - status in (`Resolved`, `Closed`)
   - urutan `updated_at DESC`
2. Activity log: `ActivityLogDao.getRecent(6)`
   - urutan `created_at DESC`

---

## 7) Cara “Menelusuri” Kode (Trik Belajar Pemula)

Kalau kamu mau memahami suatu aksi, pakai urutan ini:

1. Cari di FXML: tombol mana yang ada `fx:id` dan event apa yang dipakai.
2. Cari method di `TicketController` yang dipanggil event itu.
3. Masuk ke `TicketService` method yang sesuai aksi.
4. Lanjut ke `TicketDao` / `ActivityLogDao` untuk lihat SQL update/select.
5. Cocokkan hasil SQL dengan tampilan panel mana yang berubah.

Kalau kamu mau, sebutkan aksi mana yang ingin kamu pahami (mis. “release tiket”),
nanti aku bantu jelaskan dengan menelusuri file-file terkait langkah demi langkah.

