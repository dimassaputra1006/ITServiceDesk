# IT Service Desk
Aplikasi desktop JavaFX + SQLite untuk pengelolaan tiket IT (khusus IT Staff/Technician).

---

## Fitur Saat Ini (Minimum Viable Product)
* **Dashboard Ticket Queue:** Memantau antrean tiket masuk.
* **Dynamic Ticket Cards:** Daftar tiket dirender secara dinamis dengan visual yang informatif.
* **Accordion Detail:** Klik tombol *"Detail"* untuk melakukan *expand/collapse* deskripsi dan tombol aksi.
* **Modern Dark Theme:** Menggunakan CSS Variables, nyaman untuk penggunaan jangka panjang.
* **Local SQLite Integration:** Penyimpanan data lokal yang ringan dan cepat tanpa perlu setup server database terpisah.

---

## Tech Stack & Prerequisites

| Komponen | Teknologi / Versi | Catatan |
| :--- | :--- | :--- |
| **Bahasa Utama** | Java 21 | Gunakan JDK 21 (LTS) |
| **Framework UI** | JavaFX 21 | Berjalan di atas skema modular |
| **Database** | SQLite | Serverless via `sqlite-jdbc` |
| **Build Tool** | Apache Maven | Manajemen dependensi otomatis |

---


## Panduan Setup Project

Ikuti langkah-langkah di bawah ini untuk memasang dan menjalankan project di lingkungan lokal kamu:

### 1. Clone Project dari GitHub

Buka terminal atau command prompt, lalu jalankan perintah berikut untuk menggandakan repositori:

```bash
# Clone menggunakan HTTPS
git clone https://github.com/dimassaputra1006/ITServiceDesk.git

# Atau clone menggunakan SSH (jika dikonfigurasi)
git clone git@github.com:dimassaputra1006/ITServiceDesk.git
```
Setelah selesai, masuk ke direktori project:

```bash
cd it-servicedesk
```

### 2. Membuka Project di IDE

Buka aplikasi IDE pilihan kamu (disarankan IntelliJ IDEA).

- Pilih menu **Open** atau **Import Project**.
- Arahkan ke folder hasil clone tadi dan pilih file `pom.xml` atau folder root-nya untuk memuat sebagai project Maven.

### 3. Konfigurasi SDK & Language Level

Pastikan lingkungan Java mengarah ke versi yang tepat:

- Masuk ke menu **File → Project Structure → Project**.
- Pada bagian **Project SDK**, pilih atau tambahkan JDK 21.
- Pastikan **Language Level** disetel ke **21 - Spiral**.

### 4. Sinkronisasi Dependensi

Buka panel Maven di sebelah kanan IDE, lalu klik ikon 🔄 **Reload All Maven Projects** untuk mengunduh semua library yang dibutuhkan berdasarkan file `pom.xml`.

### 5. Konfigurasi Run (VM Options)

Karena JavaFX menggunakan sistem modular, kamu perlu menambahkan parameter modul saat menjalankan aplikasi di IDE.

Edit **Run Configuration** untuk kelas `ITServiceDeskApp`.

Tambahkan baris berikut pada bagian **VM Options**:

```bash
--module-path /path/to/your/.m2/repository/org/openjfx/javafx-base/21.0.6/javafx-base-21.0.6-linux.jar:/path/to/your/.m2/repository/org/openjfx/javafx-controls/21.0.6/javafx-controls-21.0.6-linux.jar:/path/to/your/.m2/repository/org/openjfx/javafx-fxml/21.0.6/javafx-fxml-21.0.6-linux.jar:/path/to/your/.m2/repository/org/openjfx/javafx-graphics/21.0.6/javafx-graphics-21.0.6-linux.jar --add-modules javafx.controls,javafx.fxml
```

⚠️ **Catatan Penting:** Sesuaikan path `/home/disa/.m2/repository/...` di atas sesuai dengan direktori lokal Maven kamu (misalnya jika kamu pindah ke komputer lain atau sistem operasi non-Linux).

### 6. Database & Seeding Dummy Data
Jalankan main method yang ada di dalam kelas:

`src/main/java/com/itservicedesk/config/DatabaseConfig.java`

Sebelum menjalankan aplikasi utama, isi database dengan skema tabel awal dan data dummy:

Jalankan main method yang ada di dalam kelas:

`src/main/java/com/itservicedesk/seed/Seed.java`

### 7. Menjalankan Aplikasi

Setelah database terisi, jalankan aplikasi utama melalui kelas:

`src/main/java/com/itservicedesk/ITServiceDeskApp.java`

## 📂 Struktur Project

```
.
├── src/
│   └── main/
│       ├── java/com/itservicedesk/
│       │   ├── config/          # Pengaturan konfigurasi aplikasi / database
│       │   ├── controller/      # Logika pengontrol UI (e.g., TicketController.java)
│       │   ├── dao/             # Data Access Object untuk query ke SQLite
│       │   ├── model/           # Kelas entitas / struktur data (e.g., Ticket.java)
│       │   ├── seed/            # Script/Class penanam data dummy awal
│       │   ├── service/         # Logika bisnis utama (Business Logic Layer)
│       │   ├── util/            # Kelas pembantu / helper (date formatter, dll)
│       │   └── ITServiceDeskApp.java  # Main class aplikasi JavaFX
│       └── resources/
│           ├── css/
│           │   └── styles.css   # Seluruh styling tampilan aplikasi
│           ├── database/        # Lokasi file database SQLite (.db) jika disimpan lokal
│           ├── layout_dashboard.fxml  # Layout visual untuk halaman utama dashboard
│           └── layout_directory.fxml  # Layout visual untuk direktori/halaman lainnya
├── mvnw                     # Maven wrapper script (Linux/macOS)
├── mvnw.cmd                 # Maven wrapper script (Windows)
├── pom.xml                  # File konfigurasi dependensi Maven
└── README.md                # Dokumentasi project
```
