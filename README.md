# WM-Dashboard

# Sprint 1

## Was wurde gemacht?

| Issue | Was | Wo |
|-------|-----|----|
| F4 | StandingsService refactored (Clean Code) | `wm-backend/.../service/StandingsService.java` |
| C0 | Dockerfile Backend (Multi-Stage-Build) | `wm-backend/Dockerfile` |
| C0 | Dockerfile Frontend (Multi-Stage-Build) | `wm-frontend/Dockerfile` |
| C1 | H2 In-Memory-DB | Bereits in `application.properties` – nichts zu tun |
| C2 | docker-compose.yml | `docker-compose.yml` |

---

## Dateien einsetzen

### 1. StandingsService ersetzen
```
src/main/java/ch/bbw/wm/service/StandingsService.java  → ersetzen
```

### 2. Dockerfiles platzieren
```
wm-backend/Dockerfile   → neu erstellen
wm-frontend/Dockerfile  → neu erstellen
wm-frontend/next.config.ts → output: "standalone" hinzufügen
```

### 3. docker-compose.yml ins Root
```
docker-compose.yml  → ins Root-Verzeichnis des Repos
```

---

## Starten

```bash
# Im Root-Verzeichnis des Repos:
docker compose up --build

# Frontend: http://localhost:3000
# Backend:  http://localhost:8080
# H2-Konsole: http://localhost:8080/h2-console
#   JDBC URL: jdbc:h2:mem:wmdb
#   Username: sa  |  Password: (leer)
```

---

## Was passiert beim Build?

### Backend (Multi-Stage):
1. Stage `build`: Maven lädt Abhängigkeiten (gecacht), baut JAR
2. Stage `runtime`: Nur JRE + JAR → kleines Image

### Frontend (Multi-Stage):
1. Stage `deps`: npm install (gecacht wenn package.json gleich)
2. Stage `build`: next build → .next/standalone
3. Stage `runtime`: Nur server.js + static files

---

## Warum `standalone` im next.config.ts?
Next.js erzeugt damit einen `server.js`-Einstiegspunkt,
der ohne das 300MB grosse `node_modules`-Verzeichnis läuft.
Das macht das Docker-Image deutlich kleiner.

---

## F4 – Was wurde refactored?

| Vorher (Code-Smell) | Nachher (Clean Code) |
|---------------------|----------------------|
| `ts`, `ms`, `hi`, `ai` | `teams`, `matches`, `homeIndex`, `awayIndex` |
| Eine 80-Zeilen-Methode | Aufgeteilt: `initializeRows()`, `applyMatchResult()`, `addGoals()`, `addWin()`, `addLoss()`, `addDraw()`, `sortByPoints()` |
| Handgeschriebener Bubble-Sort | `Comparator.comparingInt(...).reversed()` |
| `ids.indexOf(...)` in Schleife | Eigene Methode `indexOfTeam()` |