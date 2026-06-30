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


# Sprint 2

## Dateien & wo sie hingehören

### F1 – Strategy-Pattern

| Datei | Ziel-Pfad |
|---|---|
| `strategy/TiebreakerStrategy.java` | `src/main/java/ch/bbw/wm/service/strategy/TiebreakerStrategy.java` (neuer Ordner) |
| `strategy/PointsStrategy.java` | `src/main/java/ch/bbw/wm/service/strategy/PointsStrategy.java` |
| `strategy/GoalDifferenceStrategy.java` | `src/main/java/ch/bbw/wm/service/strategy/GoalDifferenceStrategy.java` |
| `strategy/FairPlayStrategy.java` | `src/main/java/ch/bbw/wm/service/strategy/FairPlayStrategy.java` |
| `StandingsService.java` | **ersetzt** die Version aus Sprint 1 |
| `StandingsController.java` | **ersetzt** die bestehende Datei |

### F3 – Factory-Pattern

| Datei | Ziel-Pfad |
|---|---|
| `factory/MatchView.java` | `src/main/java/ch/bbw/wm/factory/MatchView.java` (neuer Ordner) |
| `factory/GroupMatchView.java` | `src/main/java/ch/bbw/wm/factory/GroupMatchView.java` |
| `factory/KnockoutMatchView.java` | `src/main/java/ch/bbw/wm/factory/KnockoutMatchView.java` |
| `factory/MatchViewFactory.java` | `src/main/java/ch/bbw/wm/factory/MatchViewFactory.java` |
| `Match.java` | **ersetzt** `src/main/java/ch/bbw/wm/entity/Match.java` |
| `MatchController.java` | **ersetzt** die bestehende Datei |
| `schema.sql` | **ersetzt** `src/main/resources/schema.sql` |
| `data_ergaenzung.sql` | Inhalt ans Ende der bestehenden `data.sql` anhängen (nicht ersetzen!) |

---

## Wichtig: DB-Migration

Da `ddl-auto=validate` gesetzt ist, MUSS `schema.sql` manuell angepasst werden
(Hibernate erzeugt das Schema nicht automatisch). Die neue Spalte `match_type`
hat einen `DEFAULT 'GROUP'`, damit alle bestehenden 96 Gruppenspiel-Zeilen aus
`data.sql` automatisch als Gruppenspiele erkannt werden – ohne dass ihr
jede einzelne Zeile anpassen müsst.

---

## Testen

### Backend neu starten
```bash
cd wm-backend
./mvnw spring-boot:run
```

### F1 – Strategy testen
```bash
# Standard (Punkte)
curl http://localhost:8080/api/standings/A

# Nach Tordifferenz
curl http://localhost:8080/api/standings/A?strategy=goalDifference

# Nach Fair-Play (simuliert)
curl http://localhost:8080/api/standings/A?strategy=fairPlay

# Verfügbare Strategien anzeigen
curl http://localhost:8080/api/standings/strategies
```

### F3 – Factory testen
```bash
# Gruppenspiel-View
curl http://localhost:8080/api/matches/101/view

# K.o.-Spiel-View (mit Verlängerung + Elfmeter)
curl http://localhost:8080/api/matches/301/view
```

---

## Was ihr erklären können müsst (Release-Präsentation)

**Strategy (F1):**
- Warum der `StandingsService` `List<TiebreakerStrategy>` im Konstruktor bekommt statt einzelner Strategien
- Wie Spring automatisch alle `@Component`-Strategien injiziert (Dependency Injection)
- Warum `resolveStrategy()` einen Fallback auf "points" hat statt einen Fehler zu werfen

**Factory (F3):**
- Warum `MatchController` nur das `MatchView`-Interface kennt, nicht `GroupMatchView`/`KnockoutMatchView`
- Wie man einen dritten Match-Typ hinzufügen würde (z.B. "FINAL") – nur neue Klasse + ein `case` in der Factory, Controller bleibt unverändert
- Bekannte Einschränkung: Fair-Play-Werte sind simuliert (deterministisch aus Team-ID), da keine echten Karten-Daten vorhanden sind – unbedingt im Transparenzvermerk der Doku erwähnen, das ist KEIN KI-Output sondern eine bewusste Design-Entscheidung von euch

---

## Frontend (optional, falls Zeit reicht in den 2 Lektionen)

Aktuell ist die Strategy-Wahl nur per Query-Parameter testbar (curl/Browser-URL).
Ein Dropdown im Frontend (`standings/page.tsx`) wäre ein optionaler nächster
Schritt, ist aber nicht Teil der Pflicht-Issues F1/F3 selbst – die Bewertung
verlangt nur, dass das Pattern im Backend korrekt und laufzeit-umschaltbar ist.