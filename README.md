# ⛓️ Blockchain Data Monitor

Aplikacja Java do monitorowania i raportowania danych z sieci **Ethereum (Sepolia)**. Projekt stworzony przez zespół **Skompilowani** z dbałością o czysty kod i architekturę.

## 🛠️ Zespół Deweloperski
<div align="center">

| Project & Quality Lead & Developer (Access Layer) | Backend Developer (Business Logic Layer) | Quality Assurance & Developer (Testing Layer) | Developer (Reporting Layer: UI & Raporty) | Data Engineer & Developer (Integration Layer) |
| :---: | :---: | :---: | :---: | :---: |
| <img src="https://github.com/Deerion.png" width="100" height="100"> | <img src="https://github.com/Karolkzsp5.png" width="100" height="100"> | <img src="https://github.com/piolud.png" width="100" height="100"> | <img src="https://github.com/lifeoverthinker.png" width="100" height="100"> | <img src="https://github.com/7ASL.png" width="100" height="100"> |
| **Hubert Jarosz** | **Karol Kondracki** | **Piotr Ludowicz** | **Martyna Niżyńska** | **Arkadiusz Dojlido** |
| [@Deerion](https://github.com/Deerion) | [@Karolkzsp5](https://github.com/Karolkzsp5) | [@piolud](https://github.com/piolud) | [@lifeoverthinker](https://github.com/lifeoverthinker) | [@7ASL](https://github.com/7ASL) |

</div>

## 🚀 Funkcje
* **Monitoring bloków**: Pobieranie 100 najnowszych bloków (Numer, Hash, Transakcje).
* **Szczegóły transakcji**: Podgląd wartości ETH i zużycia Gasu dla wybranych bloków.
* **Statystyki**: Raport podsumowujący pracę aplikacji po jej wyłączeniu.

## 🛠️ Technologie
* **Język**: Java 21
* **Biblioteki**: Web3j, Dotenv, Logback
* **Testy**: JUnit 5 + JaCoCo (pokrycie min. 70%)

## 🏗️ Architektura
Projekt oparty na modelu trójwarstwowym:
1. **Access Layer**: Komunikacja z blockchainem przez Alchemy.
2. **Business Logic**: Przetwarzanie i agregacja danych.
3. **Reporting Layer**: Wyświetlanie danych w konsoli.

## ⚙️ Szybki Start
1. Utwórz plik `.env` w głównym katalogu.
2. Dodaj swój klucz: `BLOCKCHAIN_URL=https://eth-sepolia.g.alchemy.com/v2/TWÓJ_KLUCZ`
3. Zbuduj i uruchom:
   ```bash
   mvn clean install
   mvn exec:java -Dexec.mainClass="pl.skompilowani.Main"
