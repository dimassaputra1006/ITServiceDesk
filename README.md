# IT Service Desk

Aplikasi desktop **JavaFX + SQLite** untuk simulasi operasional Service Desk internal.

Desain UI terinspirasi dari tampilan workstation pada [ServiceDesk Simulator](https://servicedesk-simulator.com/), dengan fokus utama pada **Ticket Queue** dan **Active Room**.

---

## Untuk AI Agent / Contributor

> Jika kamu menggunakan AI Agent (Cursor Agent, Copilot Agent, atau tool otomatis lain), **wajib baca README ini terlebih dahulu** sebelum mengubah kode — supaya perubahan tetap konsisten dengan arsitektur, flow tiket, dan aturan run project.

---

## Daftar Isi

- [Fitur](#fitur)
- [Known Issues](#known-issues)
- [Tech Stack](#tech-stack)
- [Cara Menjalankan](#cara-menjalankan-windows-powershell)
- [Struktur Project](#struktur-project)
- [Aturan Transisi Status Tiket](#aturan-transisi-status-tiket)
- [Changelog](#changelog)

---

## Fitur

| Fitur | Deskripsi |
| :--- | :--- |
| **Ticket Queue** | Menampilkan tiket `Open` yang belum diklaim, urut berdasarkan prioritas. |
| **Active Room** | Menampilkan tiket `In Progress` milik analyst yang sedang aktif. |
| **System Feed** | Ringkasan ticket history (`Resolved` / `Closed`) + log aktivitas terbaru. |
| **Live Telemetry** | Counter real-time untuk Queue, Active, dan Resolved. |
| **Analyst Auto-Assign** | Sistem otomatis memilih 1 analyst IT aktif sebagai analyst sesi berjalan. |
| **Create New Ticket** | Membuat tiket baru langsung dari UI (`+ NEW TICKET`). |
| **Release Ticket** | Mengembalikan tiket dari Active Room ke Queue. |
| **Ticket Lifecycle** | `Open → In Progress → Resolved/Closed`. |
| **Activity Log (DB)** | Semua aksi utama (create/claim/release/resolve/close) direkam ke tabel `activity_log`. |
| **User Directory** | CRUD data karyawan: tambah user, edit role/department, lock/unlock akun, upload foto profil. |

---

## Known Issues

- **"Analyst Switcher" belum berupa dropdown manual.** Saat ini sistem hanya melakukan *auto-assign* ke staff IT pertama (urut alfabetis) via `AnalystSession.ensureSelected(...)`. Belum ada UI untuk memilih analyst secara manual dari toolbar.
- **Multi-window state** belum disinkronkan penuh — analyst session sudah dibagikan lewat `AnalystSession` (static holder), tapi belum ada mekanisme refresh otomatis di semua controller ketika analyst berganti dari layar lain.

---

## Tech Stack

| Komponen | Teknologi |
| :--- | :--- |
| Bahasa | Java 21+ |
| UI | JavaFX 21 |
| Database | SQLite (`sqlite-jdbc`) |
| Password Hashing | jBCrypt |
| Build Tool | Maven Wrapper (`mvnw` / `mvnw.cmd`) |

---

## Cara Menjalankan (Windows PowerShell)

**1. Masuk ke root project**

```powershell
cd "d:\Semester_4\OOP\Cursor version\ITServiceDesk"
```

**2. Set `JAVA_HOME`** (sesuaikan versi JDK di laptop kamu)

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-25.0.2"
```

**3. Inisialisasi tabel**

```powershell
.\mvnw.cmd -q exec:java "-Dexec.mainClass=com.itservicedesk.config.DatabaseConfig"
```

**4. Seed data dummy**

```powershell
.\mvnw.cmd -q exec:java "-Dexec.mainClass=com.itservicedesk.seed.Seed"
```

> ⚠️ `Seed.main()` memanggil `DatabaseConfig.resetDatabase()` terlebih dahulu — proses ini **menghapus total** file database lama. Jangan jalankan step ini di database yang datanya ingin dipertahankan.

**5. Jalankan aplikasi**

```powershell
.\mvnw.cmd javafx:run
```

> Catatan: di PowerShell gunakan `.\mvnw.cmd`, bukan `mvnw.cmd`.

---

## Struktur Project

```text
├── Contoh
│   ├── layout_dashboard.fxml
│   ├── layout_directory.fxml
│   └── styles.css
├── src
│   └── main
│       ├── java/com/itservicedesk
│       │   ├── config/        # Koneksi DB & pembuatan tabel
│       │   ├── controller/    # Controller JavaFX (TicketController, UserController)
│       │   ├── dao/           # Akses data SQLite (TicketDao, UserDao, ActivityLogDao)
│       │   ├── model/         # Entitas (Ticket, User, ActivityLog)
│       │   ├── seed/          # Seed data dummy
│       │   ├── service/       # Business flow (TicketService, UserService)
│       │   ├── util/          # AnalystSession, helper lain
│       │   ├── ITServiceDeskApp.java
│       │   └── Launcher.java  # Main-Class jar (workaround JavaFX runtime check)
│       └── resources
│           ├── css/styles.css
│           ├── database/
│           ├── images/default-avatar.jpg
│           ├── layout/
│           │   ├── ticket_dashboard.fxml
│           │   └── user_dashboard.fxml
│           └── uploads/avatars/
├── mvnw / mvnw.cmd
├── pom.xml
└── README.md
```

---

## Aturan Transisi Status Tiket

Divalidasi langsung di level query SQL (`TicketDao`) agar status tidak bisa loncat atau jadi tidak konsisten kalau aksi dipanggil berulang / urutannya salah:

| Aksi | Syarat Status Sebelumnya | Status Sesudah |
| :--- | :--- | :--- |
| **Claim** | `Open` (dan belum ada assignee) | `In Progress` |
| **Resolve** | `In Progress` | `Resolved` |
| **Release** | `In Progress` (oleh assignee yang sama) | `Open` |
| **Close** | `In Progress` atau `Resolved` | `Closed` |

