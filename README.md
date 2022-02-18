# Tubes-Stima-1 Overdrive 2020 Belum Kepikiran
## Anggota
* 13520118 Mohamad Daffa Argakoesoemah
* 13520125 Ikmal Alfaozi
* 13520149 Mohamad Hilmi Rinaldi

## Struktur Folder
- `src`: source code program
- `bin`: executable (File .jar)
- `doc`: laporan

## Tools
* Java (minimal Java 8): https://www.oracle.com/java/technologies/downloads/#java8
* IntelIiJ IDEA: https://www.jetbrains.com/idea/
* Visualizer: https://entelect-replay.raezor.co.za/

## Strategi Algoritma Greedy
* Greedy berdasarkan obstacle yang bisa dihindari  
* Greedy berdasarkan kecepatan 
* Greedy berdasarkan pemakaian powerups 

Kami memilih strategi greedy di atas karena tiga strategi tersebut jika diimplementasikan bersamaan akan menutupi kekurangan antarstrategi lainnya. Misalnya, strategi greedy berdasarkan pemakaian powerups berisiko untuk mengurangi skor karena dapat terkena obstacle dan strategi greedy berdasarkan obstacle menghindari obstacle tersebut. Selain itu, strategi greedy  berdasarkan kecepatan akan menutupi kekurangan strategi greedy pertama yang bisa mengurangi jangkauan perpindahan mobil. 

## How to Build
Lakukan modifikasi pada starter-bot menggunakan IntelliJ IDEA. 
1. Build bot dengan membuka tab "Maven Project" di sisi kanan layar. 
2. Pilih "java-starter-bot"> "Lifecycle" dan klik dua kali "Install". 
3. File .jar akan otomatis terbuat di folder bernama "target". File akan bernama "java-starter-bot-jar-with-dependencies.jar".
