# Travian Interceptor

A full-stack Travian timing tool with:

- a Java 17 backend that calculates travel, return, and intercept timings
- enum-backed tribe and troop catalogs hard-coded in the backend
- a React + TypeScript frontend with a focused UI for village coordinates and troop-speed selection
- official-source reference links for Tournament Square, hero maps, and unit-speed lookup

## What It Calculates

Given:

- attacker village coordinates
- defending village coordinates
- intercepting village coordinates
- the incoming landing time
- the attacker tribe and optional troop guess
- the attacker Tournament Square level
- the intercept village Tournament Square level
- the attacker hero map bonus on the return trip
- your chosen intercept troop

the app computes:

- wrapped Travian map distance from attacker to defender
- wrapped distance from your intercept village to the attacker village
- estimated original send time for each possible attacking troop speed
- estimated return-home time after the attack lands
- exact launch time for your intercept troop to land on the attacker village when the army comes back

Supported server speeds:

- x1
- x2
- x3
- x5
- x10

## Project Layout

- `backend`: Spring Boot API, Java 17
- `frontend`: React + TypeScript, powered by Vite

The troop and tribe catalog now lives in:

- `backend/src/main/java/com/travian/interceptor/model/Tribe.java`
- `backend/src/main/java/com/travian/interceptor/model/TroopUnit.java`

## Run Locally

Backend:

```bash
cd backend
mvn spring-boot:run
```

Frontend:

```bash
cd frontend
npm install
npm run dev
```

The Vite dev server proxies `/api` requests to `http://localhost:8080`.

## Notes And Assumptions

- Tournament Square is applied after the first 20 fields.
- Hero maps are applied as a return-trip speed bonus.
- The intercept launch timing targets the attacker village, so this is a backtime-style calculator.
- For Egyptians and Huns, infantry/cavalry/scout speeds come from the official comparison table. Siege, chief, and settler timings follow current regular-world speeds and can be updated easily in `backend/src/main/java/com/travian/interceptor/model/TroopUnit.java`.
