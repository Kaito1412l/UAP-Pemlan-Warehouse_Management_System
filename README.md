# 🏭 Warehouse Management System (WMS)

Warehouse Management System (WMS) adalah aplikasi berbasis Java yang dirancang untuk mengelola data gudang secara terstruktur menggunakan pendekatan **Object-Oriented Programming (OOP)**, **layered architecture**, serta **penyimpanan data berbasis Excel (Apache POI)**.

Aplikasi ini mendukung pengelolaan lokasi gudang dan data barang berbasis batch, dengan antarmuka **CLI** dan **GUI (Swing)**.

---

## 1. Fitur Utama 

### 1.1 Manajemen Lokasi Gudang
- Menampilkan seluruh lokasi gudang
- Menambahkan lokasi baru
- Mengubah (update) kode lokasi
- Menghapus lokasi
- Validasi lokasi agar tidak dapat dihapus jika masih digunakan oleh stok

### 1.2 Manajemen Data Barang (Stock Batch)
- Menyimpan data barang per batch
- Setiap batch memiliki:
  - UPC
  - SKU
  - Tanggal batch
  - Jumlah stok
  - Lokasi penyimpanan (object `Location`)
- Menampilkan seluruh daftar barang dari gudang

### 1.3 Logging Aktivitas
- Setiap perubahan stok dicatat ke dalam log
- Informasi log meliputi:
  - Waktu
  - Aksi
  - UPC
  - SKU
  - Jumlah
  - Lokasi asal dan tujuan

### 1.4 Antarmuka Aplikasi
#### CLI (Command Line Interface)
- Login admin
- Menu admin terpisah
- Akses penuh untuk manajemen lokasi dan stok

#### GUI (Swing)
- Tampilan modern dan rapi
- Styling tombol (warna, font, rounded border)
- Header/banner gambar
- JTable dengan styling:
  - Warna header
  - Row height
  - Border
- Layout konsisten dan terstruktur

---

## 2. Arsitektur Aplikasi

Aplikasi menggunakan **layered architecture** untuk menjaga keterpisahan tanggung jawab.

Presentation Layer  
├── CLI  
└── GUI (Swing)  

Service Layer  
└── Business Logic  

Repository Layer  
└── ExcelRepository (Apache POI)  

Model Layer  
└── Entity / Domain Object  

### Penjelasan Layer

- **Model**
  - Representasi data murni (Location, StockBatch)
- **Repository**
  - Satu-satunya layer yang berinteraksi dengan file Excel
- **Service**
  - Mengandung aturan bisnis dan validasi
- **Presentation**
  - CLI dan GUI sebagai antarmuka pengguna

---

## 3. Struktur Folder Proyek

src/  
└── main/  
└── java/  
└── wms/  
├── core/  
│ ├── model/  
│ │ ├── Location.java  
│ │ └── StockBatch.java  
│ ├── repository/  
│ │ ├── ExcelRepository.java  
│ │ └── ExcelOperation.java  
│ └── service/  
│ ├── AdminService.java  
│ └── StockService.java  
├── cli/  
│ ├── LoginCLI.java  
│ └── AdminCLI.java  
└── gui/  
└── SwingApp.java  

---

## 4. Teknologi yang Digunakan

- **Java SE**
- **Apache POI** (XSSF)
- **Java Swing**
- **Excel (.xlsx)** sebagai media penyimpanan data
- **OOP & SOLID Principles**

---

## 5. Penyimpanan Data (Excel)

Aplikasi menggunakan satu file database: warehouse_db.xlsx


### Sheet yang Digunakan

| Sheet Name | Fungsi |
|----------|-------|
| STOCK | Menyimpan data barang per batch |
| LOCATIONS | Menyimpan daftar lokasi gudang |
| LOG | Mencatat seluruh aktivitas |

---

## 6. Konsep Data Inti

### Location
- Direpresentasikan sebagai object
- Memiliki barcode unik
- Digunakan sebagai referensi langsung oleh StockBatch

### StockBatch
- Berbasis batch (bukan agregat SKU)
- Memiliki relasi langsung ke Location
- Tidak menggunakan string lokasi mentah

---

## 7. Prinsip Desain Penting

- Repository adalah **single source of truth**
- Service tidak mengetahui detail Excel
- GUI dan CLI tidak mengakses repository secara langsung
- Semua perubahan data melewati service
- Location adalah **first-class object**

---

## 8. Cara Menjalankan Program

1. Pastikan Java sudah terinstal
2. Pastikan Apache POI tersedia di classpath
3. Jalankan class `Main` atau `SwingApp`
4. File `warehouse_db.xlsx` akan otomatis dibuat jika belum ada

---

## 9. Catatan Pengembangan

- Sistem dirancang untuk mudah dikembangkan:
  - Penambahan role user
  - Migrasi database ke SQL
  - Dashboard visual
- Struktur kode sudah siap untuk refactor lanjutan
- Cocok untuk studi kasus Warehouse Management System skala kecil-menengah

---

## 10. Penutup

Aplikasi ini dibuat sebagai implementasi nyata dari:
- Pemrograman Lanjut
- GUI Java
- Manajemen data terstruktur
- Arsitektur perangkat lunak yang bersih

Dirancang bukan hanya untuk berjalan, tetapi untuk **mudah dirawat dan dikembangkan**.
