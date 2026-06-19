# IT Service Desk
Aplikasi desktop JavaFX + SQLite untuk simulasi operasional Service Desk internal.

Desain UI terinspirasi dari tampilan workstation pada [ServiceDesk Simulator](https://servicedesk-simulator.com/), dengan fokus utama pada **Ticket Queue** dan **Active Room**.

---

## Penting untuk AI Agent / Contributor
Jika kamu menggunakan AI Agent (Cursor Agent, Copilot Agent, atau tool otomatis lain), **WAJIB baca README ini terlebih dahulu sebelum mengubah kode**.  
Tujuannya supaya perubahan tetap konsisten dengan arsitektur, flow tiket, dan aturan run project.

---

## Fitur Saat Ini
- **Ticket Queue**: Menampilkan tiket `Open` yang belum diklaim, urut prioritas.
- **Active Room**: Menampilkan tiket `In Progress` milik analyst aktif.
- **System Feed**: Menampilkan ringkasan ticket history (`Resolved`/`Closed`) dan log aktivitas terbaru.
- **Live Telemetry**: Counter real-time untuk Queue, Active, dan Resolved.
- **Analyst Switcher**: Pilih analyst IT aktif langsung dari toolbar.
- **Create New Ticket**: Buat tiket baru dari UI (`+ NEW TICKET`).
- **Release Ticket**: Kembalikan tiket dari Active Room ke Queue.
- **Ticket Lifecycle**: `Open → In Progress → Resolved/Closed`.
- **Activity Log (DB)**: Semua aksi utama (create/claim/release/resolve/close) direkam ke tabel `activity_log`.

---

## Yang Diubah dari Versi Awal (Changelog Ringkas)

### V1 (Awal)
- Dashboard dasar daftar ticket.
- Styling dark theme sederhana.

### V2 (Sebelumnya)
- Rework UI ke split panel: Ticket Queue + Active Room.
- Tema workstation ala simulator.
- Counter telemetry dan aksi claim/resolve/close.

### V3 (Update sekarang)
- Tambah **System Feed** di panel ketiga.
- Tambah **Create New Ticket dialog**.
- Tambah **Release Ticket** dari Active Room.
- Tambah **Analyst selector** (IT staff aktif).
- Tambah tabel + DAO **activity_log**.
- Tambah **TicketService** untuk business flow + logging aksi.
- Seed diperbarui supaya membersihkan `activity_log`.

### V3.1 (Hardening flow)
- Matangkan aturan transisi status di level query SQL:
  - `Claim` hanya boleh dari status `Open`.
  - `Resolve` hanya boleh dari status `In Progress`.
  - `Close` hanya boleh dari status `In Progress` atau `Resolved`.
- Tujuan: mencegah status tiket loncat/inkonsisten saat aksi dipanggil berulang atau urutannya salah.

### V3.2 (Stabilitas tombol & konsistensi UI)
- Perbaikan parsing datetime di `UserDao` agar data analyst IT bisa terbaca normal (ComboBox analyst kembali berfungsi).
- Aplikasi sekarang auto-create tabel saat startup (`DatabaseConfig.createTables()`), jadi `activity_log` tidak lagi missing saat run langsung.
- Tombol aksi (`Claim/Resolve/Close/Release`) sekarang memberi feedback jika aksi gagal (mis. status tidak valid / analyst belum dipilih).
- Dialog **New Ticket** sekarang menggunakan stylesheet yang sama dengan main screen agar tampilan lebih konsisten.

### V3.3 (Accordion UI)
- Tampilan tiket di **Ticket Queue** diubah ke format accordion (expand/collapse per tiket).
- Tampilan sesi tiket di **Active Room** juga menggunakan accordion agar detail aksi lebih terstruktur.
- Ditambahkan styling accordion khusus agar tetap konsisten dengan tema workstation.

---

## Tech Stack
| Komponen | Teknologi |
| :--- | :--- |
| Bahasa | Java 21+ |
| UI | JavaFX 21 |
| Database | SQLite (`sqlite-jdbc`) |
| Build Tool | Maven Wrapper (`mvnw` / `mvnw.cmd`) |

---

## Cara Run (Windows PowerShell)

1. Buka terminal di root project:
```powershell
cd "d:\Semester_4\OOP\Cursor version\ITServiceDesk"
```

2. Set `JAVA_HOME` (sesuaikan versi JDK di laptop kamu):
```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-25.0.2"
```

3. Inisialisasi tabel:
```powershell
.\mvnw.cmd -q exec:java "-Dexec.mainClass=com.itservicedesk.config.DatabaseConfig"
```

4. Seed data dummy:
```powershell
.\mvnw.cmd -q exec:java "-Dexec.mainClass=com.itservicedesk.seed.Seed"
```

5. Jalankan aplikasi:
```powershell
.\mvnw.cmd javafx:run
```

> Catatan: di PowerShell gunakan `.\mvnw.cmd`, bukan `mvnw.cmd`.

---

## Struktur Project
```text
src/main/java/com/itservicedesk/
├── config/        # Koneksi DB & create table
├── controller/    # Controller JavaFX (TicketController)
├── dao/           # Akses data SQLite (TicketDao, UserDao, ActivityLogDao)
├── model/         # Entitas (Ticket, User, ActivityLog)
├── seed/          # Seed data dummy
├── service/       # Business flow (TicketService)
└── ITServiceDeskApp.java

src/main/resources/
├── css/styles.css
├── database/
└── layout_dashboard.fxml
```
