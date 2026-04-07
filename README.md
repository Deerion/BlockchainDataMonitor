# ⛓️ Blockchain Data Monitor

Aplikacja Java do monitorowania i raportowania danych z sieci **Ethereum (Sepolia)**. Projekt stworzony przez zespół **Skompilowani** z dbałością o czysty kod i architekturę.

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
