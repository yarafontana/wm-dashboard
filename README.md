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


# Sprint 3

## Dateien & wo sie hingehören

### F2 – Decorator-Pattern (neu)

| Datei | Ziel-Pfad |
|---|---|
| `decorator/MatchDecorator.java` | `src/main/java/ch/bbw/wm/decorator/MatchDecorator.java` (neuer Ordner) |
| `decorator/StatsDecorator.java` | `src/main/java/ch/bbw/wm/decorator/StatsDecorator.java` |
| `decorator/TopMatchDecorator.java` | `src/main/java/ch/bbw/wm/decorator/TopMatchDecorator.java` |

### Bestehende Dateien ersetzen (wegen getMatch() Erweiterung)

| Datei | Ziel-Pfad |
|---|---|
| `factory/MatchView.java` | **ersetzt** die Version aus Sprint 2 |
| `factory/GroupMatchView.java` | **ersetzt** die Version aus Sprint 2 |
| `factory/KnockoutMatchView.java` | **ersetzt** die Version aus Sprint 2 |
| `MatchController.java` | **ersetzt** die Version aus Sprint 2 |

---

## Warum mussten F3-Dateien angepasst werden?

F2 (Decorator) braucht Zugriff auf die rohen Match-Daten (Tore), um z.B.
die Tordifferenz für die Statistik zu berechnen. Dafür wurde dem
`MatchView`-Interface aus F3 eine Methode `getMatch()` hinzugefügt
(mit `@JsonIgnore`, damit sie nicht im JSON-Response auftaucht).

Das zeigt gut, wie Strategy/Factory/Decorator in echten Projekten
zusammenspielen: F2 baut direkt auf der Struktur von F3 auf, statt
eine komplett neue Pipeline zu bauen.

---

## Testen

```bash
cd wm-backend
./mvnw spring-boot:run
```

```bash
# Nur Roh-Ansicht (wie in Sprint 2)
curl http://localhost:8080/api/matches/101/view

# + Statistik-Schicht
curl "http://localhost:8080/api/matches/101/view?stats=true"

# + Top-Spiel/Überraschungs-Schicht
curl "http://localhost:8080/api/matches/151/view?top=true"

# Beide Schichten gestapelt -- unabhängig kombinierbar
curl "http://localhost:8080/api/matches/151/view?stats=true&top=true"
```

Match 151 (Niederlande 2:2 Japan, Spieltag 1) eignet sich gut zum Testen
der Top-Match-Erkennung, da insgesamt 4 Tore fielen. Für eine echte
"Überraschung" (Tordifferenz >= 4) eignet sich z.B. Match 141
(Deutschland 4:0 Curacao).

---

## Was ihr erklären können müsst (Release-Präsentation)

- Warum `MatchDecorator` selbst `MatchView` implementiert (das ist der Kern
  des Patterns: ein Decorator ist von aussen nicht von einem normalen
  MatchView zu unterscheiden, deshalb lässt er sich beliebig stapeln)
- Warum die Reihenfolge `stats=true&top=true` vs. `top=true&stats=true`
  hier keinen Unterschied macht (beide Decorator hängen nur an die
  bestehende Summary an, statt sie zu ersetzen)
- Wie man einen dritten Decorator hinzufügen würde (z.B. ein
  "RivalryDecorator" für klassische Duelle) -- nur eine neue Klasse,
  `MatchController` muss nur minimal erweitert werden

---

## C3 – Docker Hub Re-Push (v2.0)

Da sich Backend-Code geändert hat (neue Decorator/Factory-Klassen),
müsst ihr ein neues Image mit neuem Tag pushen:

```bash
docker login

docker build -t yarabbw/wm-backend:v2.0 ./wm-backend
docker build -t yarabbw/wm-frontend:v2.0 ./wm-frontend

docker push yarabbw/wm-backend:v2.0
docker push yarabbw/wm-frontend:v2.0
```

**Wichtig für die Doku:** Beide Versionen (v1.0 und v2.0) bleiben auf
Docker Hub sichtbar -- das zeigt der Lehrperson eine nachvollziehbare
Versionierung über die Sprints hinweg (Pflichtkriterium bei C3).
Trag in der Dokumentation unter "Docker Hub Links" beide Tags ein:

```
Frontend v1.0: https://hub.docker.com/r/yarabbw/wm-frontend (Tag: v1.0)
Frontend v2.0: https://hub.docker.com/r/yarabbw/wm-frontend (Tag: v2.0)
Backend  v1.0: https://hub.docker.com/r/yarabbw/wm-backend (Tag: v1.0)
Backend  v2.0: https://hub.docker.com/r/yarabbw/wm-backend (Tag: v2.0)
```

---

## Bekannte Einschränkung (Transparenzvermerk Doku)

Die Top-Match/Überraschungs-Schwellenwerte (5 Tore, Tordifferenz 4)
sind bewusst einfache, fest codierte Heuristiken für den Lernzweck --
kein echtes Statistik-Modell. Das gehört, wie schon die simulierten
Fair-Play-Werte aus Sprint 2, in den Transparenzvermerk der Doku
als eigene technische Design-Entscheidung des Teams.