import { ChangeEvent, FormEvent, useEffect, useState } from "react";
import { DateTime } from "luxon";

type UnitCategory = "INFANTRY" | "CAVALRY" | "SCOUT" | "SIEGE" | "CHIEF" | "SETTLER";

type TroopUnit = {
  id: string;
  name: string;
  baseSpeed: number;
  category: UnitCategory;
  cavalry: boolean;
};

type TribeDefinition = {
  id: string;
  name: string;
  sourceNote: string;
  units: TroopUnit[];
};

type MapBonusOption = {
  label: string;
  percentBonus: number;
};

type BootsOption = {
  label: string;
  percentBonus: number;
};

type ReferenceSource = {
  label: string;
  url: string;
};

type ReferenceData = {
  tribes: TribeDefinition[];
  worldSizes: number[];
  serverSpeeds: number[];
  mapBonuses: MapBonusOption[];
  bootsOptions: BootsOption[];
  sources: ReferenceSource[];
};

type CandidateWindow = {
  attackerUnitId: string;
  attackerUnitName: string;
  attackerUnitBaseSpeed: number;
  category: UnitCategory;
  estimatedAttackSentAt: string;
  attackLandingAt: string;
  estimatedReturnHomeAt: string;
  interceptLaunchAt: string;
  outboundTravelSeconds: number;
  returnTravelSeconds: number;
  interceptorTravelSeconds: number;
  speedSummary: string;
};

type CalculationResponse = {
  distances: {
    attackerToDefender: number;
    interceptorToAttacker: number;
  };
  interceptor: {
    tribeId: string;
    unitId: string;
    unitName: string;
    unitBaseSpeed: number;
  };
  candidates: CandidateWindow[];
};

type CoordinateState = {
  x: string;
  y: string;
};

type FormState = {
  attackerVillage: CoordinateState;
  defenderVillage: CoordinateState;
  interceptorVillage: CoordinateState;
  landingTime: string;
  timeZone: string;
  attackerTribeId: string;
  attackerUnitId: string;
  interceptorTribeId: string;
  interceptorUnitId: string;
  worldSize: number;
  serverSpeed: number;
  attackerTournamentSquareLevel: number;
  interceptorTournamentSquareLevel: number;
  attackerBootsBonusPercent: number;
  interceptorBootsBonusPercent: number;
  attackerReturnMapBonusPercent: number;
};

const DEFAULT_TIME_ZONE = Intl.DateTimeFormat().resolvedOptions().timeZone || "UTC";
const TIME_ZONE_OPTIONS = buildTimeZoneOptions(DEFAULT_TIME_ZONE);
const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL ?? "").replace(/\/$/, "");

function apiUrl(path: string) {
  return `${API_BASE_URL}${path}`;
}

export default function App() {
  const [reference, setReference] = useState<ReferenceData | null>(null);
  const [form, setForm] = useState<FormState>(() => createInitialForm(DEFAULT_TIME_ZONE));
  const [result, setResult] = useState<CalculationResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const loadReference = async () => {
      try {
        const response = await fetch(apiUrl("/api/reference"));
        if (!response.ok) {
          throw new Error("Failed to load troop reference data.");
        }

        const data = sanitizeReferenceData((await response.json()) as Partial<ReferenceData>);
        setReference(data);
        setForm((current) => normalizeForm(current, data));
      } catch (requestError) {
        setError(requestError instanceof Error ? requestError.message : "Failed to load reference data.");
      }
    };

    void loadReference();
  }, []);

  const attackerUnits = reference ? getUnitsForTribe(reference, form.attackerTribeId) : [];
  const interceptorUnits = reference ? getUnitsForTribe(reference, form.interceptorTribeId) : [];
  const selectedInterceptorUnit = interceptorUnits.find((unit) => unit.id === form.interceptorUnitId);
  const selectedMapBonus = reference?.mapBonuses.find((option) => option.percentBonus === form.attackerReturnMapBonusPercent);
  const attackerBoots = reference?.bootsOptions.find((option) => option.percentBonus === form.attackerBootsBonusPercent);
  const interceptorBoots = reference?.bootsOptions.find((option) => option.percentBonus === form.interceptorBootsBonusPercent);

  const submit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setLoading(true);
    setError(null);

    try {
      const attackerVillage = parseCoordinateState(form.attackerVillage, "Attacker village");
      const defenderVillage = parseCoordinateState(form.defenderVillage, "Defending village");
      const interceptorVillage = parseCoordinateState(form.interceptorVillage, "Intercept village");
      const landingTime = parseLocalDateTime(form.landingTime, form.timeZone, "Incoming landing time")
        .toUTC()
        .toISO({ suppressMilliseconds: true });

      if (!landingTime) {
        throw new Error("Incoming landing time is invalid.");
      }

      const response = await fetch(apiUrl("/api/calculate"), {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          attackerVillage,
          defenderVillage,
          interceptorVillage,
          landingTime,
          attackerTribeId: form.attackerTribeId,
          attackerUnitId: form.attackerUnitId,
          interceptorTribeId: form.interceptorTribeId,
          interceptorUnitId: form.interceptorUnitId,
          worldSize: form.worldSize,
          serverSpeed: form.serverSpeed,
          attackerTournamentSquareLevel: form.attackerTournamentSquareLevel,
          interceptorTournamentSquareLevel: form.interceptorTournamentSquareLevel,
          attackerBootsBonusPercent: form.attackerBootsBonusPercent,
          interceptorBootsBonusPercent: form.interceptorBootsBonusPercent,
          attackerReturnMapBonusPercent: form.attackerReturnMapBonusPercent,
        }),
      });

      if (!response.ok) {
        const body = (await response.json().catch(() => null)) as { detail?: string } | null;
        throw new Error(body?.detail ?? "Calculation failed.");
      }

      const data = (await response.json()) as CalculationResponse;
      setResult(data);
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : "Calculation failed.");
      setResult(null);
    } finally {
      setLoading(false);
    }
  };

  const handleCoordinateChange =
    (village: keyof Pick<FormState, "attackerVillage" | "defenderVillage" | "interceptorVillage">, axis: keyof CoordinateState) =>
    (event: ChangeEvent<HTMLInputElement>) => {
      const value = event.target.value;
      if (!/^-?\d*$/.test(value)) {
        return;
      }

      setForm((current) => ({
        ...current,
        [village]: {
          ...current[village],
          [axis]: value,
        },
      }));
    };

  const handleNumericChange =
    (
      field: keyof Pick<
        FormState,
        | "worldSize"
        | "serverSpeed"
        | "attackerTournamentSquareLevel"
        | "interceptorTournamentSquareLevel"
        | "attackerBootsBonusPercent"
        | "interceptorBootsBonusPercent"
        | "attackerReturnMapBonusPercent"
      >,
    ) =>
    (event: ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
      const value = Number(event.target.value);
      setForm((current) => ({
        ...current,
        [field]: Number.isNaN(value) ? 0 : value,
      }));
    };

  const handleTribeChange =
    (field: "attackerTribeId" | "interceptorTribeId", pairedUnitField: "attackerUnitId" | "interceptorUnitId", allowAll: boolean) =>
    (event: ChangeEvent<HTMLSelectElement>) => {
      const tribeId = event.target.value;
      setForm((current) => {
        if (!reference) {
          return {
            ...current,
            [field]: tribeId,
          };
        }

        const nextUnits = getUnitsForTribe(reference, tribeId);
        const fallbackUnitId = allowAll ? "all" : nextUnits[0]?.id ?? "";

        return {
          ...current,
          [field]: tribeId,
          [pairedUnitField]: fallbackUnitId,
        };
      });
    };

  const handleUnitChange =
    (field: "attackerUnitId" | "interceptorUnitId") =>
    (event: ChangeEvent<HTMLSelectElement>) => {
      setForm((current) => ({
        ...current,
        [field]: event.target.value,
      }));
    };

  const handleTimeZoneChange = (event: ChangeEvent<HTMLSelectElement>) => {
    const nextTimeZone = event.target.value;
    setForm((current) => ({
      ...current,
      timeZone: nextTimeZone,
      landingTime: convertTimeZone(current.landingTime, current.timeZone, nextTimeZone),
    }));
  };

  if (!reference) {
    return (
      <div className="app-shell">
        <div className="loading-panel">
          <p className="eyebrow">Travian Interceptor</p>
          <h1>Loading reference data</h1>
          <p>The calculator is fetching tribe, time, and hero-speed options from the backend.</p>
          {error ? <p className="error-text">{error}</p> : null}
        </div>
      </div>
    );
  }

  return (
    <div className="app-shell">
      <div className="backdrop-pattern" />

      <header className="topbar">
        <div className="crest">T</div>
        <div className="brand-copy">
          <p className="eyebrow">Travian Tools</p>
          <h1>Interceptor War Table</h1>
        </div>
        <div className="topbar-meta">
          <div className="topbar-pill">
            <span>Time zone</span>
            <strong>{formatTimeZoneLabel(form.timeZone)}</strong>
          </div>
          <div className="topbar-pill">
            <span>World preset</span>
            <strong>x{form.serverSpeed}</strong>
          </div>
        </div>
      </header>

      <section className="hero-banner">
        <div className="hero-copy-block">
          <p className="eyebrow">Council View</p>
          <h2>Calculate send time, return home, and the exact intercept launch in your chosen timezone.</h2>
          <p>
            Boots are now included after the first 20 fields, maps affect the attacker return trip, and every result row is shown in the timezone you select.
          </p>
        </div>
        <div className="hero-stats">
          <div>
            <span>Attacker boots</span>
            <strong>{attackerBoots?.label ?? "No boots"}</strong>
          </div>
          <div>
            <span>Interceptor boots</span>
            <strong>{interceptorBoots?.label ?? "No boots"}</strong>
          </div>
          <div>
            <span>Return item</span>
            <strong>{selectedMapBonus?.label ?? "No map"}</strong>
          </div>
        </div>
      </section>

      <main className="layout">
        <section className="panel">
          <div className="panel-title">Battle Desk</div>

          <form onSubmit={submit} className="calculator-form">
            <div className="card-grid">
              <VillageCard
                title="Attacker Village"
                subtitle="Incoming origin"
                village={form.attackerVillage}
                onXChange={handleCoordinateChange("attackerVillage", "x")}
                onYChange={handleCoordinateChange("attackerVillage", "y")}
              />
              <VillageCard
                title="Defending Village"
                subtitle="Incoming target"
                village={form.defenderVillage}
                onXChange={handleCoordinateChange("defenderVillage", "x")}
                onYChange={handleCoordinateChange("defenderVillage", "y")}
              />
              <VillageCard
                title="Intercept Village"
                subtitle="Your launch point"
                village={form.interceptorVillage}
                onXChange={handleCoordinateChange("interceptorVillage", "x")}
                onYChange={handleCoordinateChange("interceptorVillage", "y")}
              />
            </div>

            <div className="field-grid">
              <label>
                <span>Incoming landing time</span>
                <input
                  type="datetime-local"
                  value={form.landingTime}
                  onChange={(event) => setForm((current) => ({ ...current, landingTime: event.target.value }))}
                  required
                />
              </label>

              <label>
                <span>Time zone</span>
                <select value={form.timeZone} onChange={handleTimeZoneChange}>
                  {TIME_ZONE_OPTIONS.map((timeZone) => (
                    <option key={timeZone} value={timeZone}>
                      {formatTimeZoneLabel(timeZone)}
                    </option>
                  ))}
                </select>
              </label>

              <label>
                <span>World size</span>
                <select value={form.worldSize} onChange={handleNumericChange("worldSize")}>
                  {reference.worldSizes.map((size) => (
                    <option key={size} value={size}>
                      ±{size}
                    </option>
                  ))}
                </select>
              </label>

              <label>
                <span>Server speed preset</span>
                <select value={form.serverSpeed} onChange={handleNumericChange("serverSpeed")}>
                  {reference.serverSpeeds.map((speed) => (
                    <option key={speed} value={speed}>
                      x{speed}
                    </option>
                  ))}
                </select>
              </label>

              <label>
                <span>Attacker TS level</span>
                <input
                  type="number"
                  min={0}
                  max={20}
                  value={form.attackerTournamentSquareLevel}
                  onChange={handleNumericChange("attackerTournamentSquareLevel")}
                />
              </label>

              <label>
                <span>Interceptor TS level</span>
                <input
                  type="number"
                  min={0}
                  max={20}
                  value={form.interceptorTournamentSquareLevel}
                  onChange={handleNumericChange("interceptorTournamentSquareLevel")}
                />
              </label>
            </div>

            <div className="travian-note">
              <strong>Official movement presets</strong>
              <span>x1 worlds use troop speed x1. x2, x3, and x5 worlds use troop speed x2. x10 worlds use troop speed x4.</span>
            </div>

            <div className="dual-grid">
              <div className="selection-card">
                <div className="selection-header">
                  <p className="eyebrow">Attacker</p>
                  <h3>Incoming assumptions</h3>
                </div>

                <label>
                  <span>Attacker tribe</span>
                  <select value={form.attackerTribeId} onChange={handleTribeChange("attackerTribeId", "attackerUnitId", true)}>
                    {reference.tribes.map((tribe) => (
                      <option key={tribe.id} value={tribe.id}>
                        {tribe.name}
                      </option>
                    ))}
                  </select>
                </label>

                <label>
                  <span>Attacker troop</span>
                  <select value={form.attackerUnitId} onChange={handleUnitChange("attackerUnitId")}>
                    <option value="all">Any unit in this tribe</option>
                    {attackerUnits.map((unit) => (
                      <option key={unit.id} value={unit.id}>
                        {unit.name} ({unit.baseSpeed} f/h)
                      </option>
                    ))}
                  </select>
                </label>

                <label>
                  <span>Attacker hero boots</span>
                  <select value={form.attackerBootsBonusPercent} onChange={handleNumericChange("attackerBootsBonusPercent")}>
                    {reference.bootsOptions.map((option) => (
                      <option key={option.percentBonus} value={option.percentBonus}>
                        {option.label}
                      </option>
                    ))}
                  </select>
                </label>

                <label>
                  <span>Attacker return item</span>
                  <select value={form.attackerReturnMapBonusPercent} onChange={handleNumericChange("attackerReturnMapBonusPercent")}>
                    {reference.mapBonuses.map((bonus) => (
                      <option key={bonus.percentBonus} value={bonus.percentBonus}>
                        {bonus.label}
                      </option>
                    ))}
                  </select>
                </label>

                <p className="hint">
                  Boots only apply beyond the first 20 fields when the hero travels with the army. Maps only speed up the return trip.
                </p>
              </div>

              <div className="selection-card">
                <div className="selection-header">
                  <p className="eyebrow">Interceptor</p>
                  <h3>Your returning catch</h3>
                </div>

                <label>
                  <span>Interceptor tribe</span>
                  <select value={form.interceptorTribeId} onChange={handleTribeChange("interceptorTribeId", "interceptorUnitId", false)}>
                    {reference.tribes.map((tribe) => (
                      <option key={tribe.id} value={tribe.id}>
                        {tribe.name}
                      </option>
                    ))}
                  </select>
                </label>

                <label>
                  <span>Interceptor troop</span>
                  <select value={form.interceptorUnitId} onChange={handleUnitChange("interceptorUnitId")}>
                    {interceptorUnits.map((unit) => (
                      <option key={unit.id} value={unit.id}>
                        {unit.name} ({unit.baseSpeed} f/h)
                      </option>
                    ))}
                  </select>
                </label>

                <label>
                  <span>Interceptor hero boots</span>
                  <select value={form.interceptorBootsBonusPercent} onChange={handleNumericChange("interceptorBootsBonusPercent")}>
                    {reference.bootsOptions.map((option) => (
                      <option key={option.percentBonus} value={option.percentBonus}>
                        {option.label}
                      </option>
                    ))}
                  </select>
                </label>

                <div className="stat-strip">
                  <div>
                    <span>Chosen speed</span>
                    <strong>{selectedInterceptorUnit?.baseSpeed ?? 0} f/h</strong>
                  </div>
                  <div>
                    <span>Role</span>
                    <strong>{selectedInterceptorUnit?.category.toLowerCase() ?? "n/a"}</strong>
                  </div>
                </div>
              </div>
            </div>

            <div className="action-row">
              <button type="submit" disabled={loading}>
                {loading ? "Calculating..." : "Calculate Intercept Window"}
              </button>
              {error ? <p className="error-text">{error}</p> : null}
            </div>
          </form>
        </section>

        <section className="panel">
          <div className="panel-title">Results Ledger</div>

          {result ? (
            <>
              <div className="summary-banner">
                <div>
                  <span>Attacker → defender</span>
                  <strong>{result.distances.attackerToDefender.toFixed(2)} fields</strong>
                </div>
                <div>
                  <span>Interceptor → attacker</span>
                  <strong>{result.distances.interceptorToAttacker.toFixed(2)} fields</strong>
                </div>
                <div>
                  <span>Displayed in</span>
                  <strong>{formatTimeZoneLabel(form.timeZone)}</strong>
                </div>
              </div>

              <div className="results-table">
                <div className="results-head">
                  <span>Attacker troop</span>
                  <span>Sent at</span>
                  <span>Returns home</span>
                  <span>Launch from interceptor</span>
                </div>

                {result.candidates.map((candidate) => {
                  const launchInPast = DateTime.fromISO(candidate.interceptLaunchAt).toMillis() < Date.now();

                  return (
                    <article className="result-row" key={candidate.attackerUnitId}>
                      <div>
                        <strong>{candidate.attackerUnitName}</strong>
                        <p>
                          {candidate.attackerUnitBaseSpeed} f/h • {candidate.category.toLowerCase()}
                        </p>
                        <p>{candidate.speedSummary}</p>
                      </div>
                      <div>
                        <strong>{formatDateTime(candidate.estimatedAttackSentAt, form.timeZone)}</strong>
                        <p>{formatDuration(candidate.outboundTravelSeconds)} outbound</p>
                      </div>
                      <div>
                        <strong>{formatDateTime(candidate.estimatedReturnHomeAt, form.timeZone)}</strong>
                        <p>{formatDuration(candidate.returnTravelSeconds)} return</p>
                      </div>
                      <div>
                        <strong className={launchInPast ? "expired" : ""}>
                          {formatDateTime(candidate.interceptLaunchAt, form.timeZone)}
                        </strong>
                        <p>{formatDuration(candidate.interceptorTravelSeconds)} from your village</p>
                        {launchInPast ? <p className="expired">This launch time is already in the past.</p> : null}
                      </div>
                    </article>
                  );
                })}
              </div>
            </>
          ) : (
            <div className="empty-state">
              <h3>Awaiting battle data</h3>
              <p>Fill in the villages, pick the timezone and hero items, then the ledger will show your send, return, and intercept timings.</p>
            </div>
          )}
        </section>

        <section className="panel">
          <div className="panel-title">Official References</div>
          <div className="source-grid">
            {reference.sources.map((source) => (
              <a key={source.url} className="source-card" href={source.url} target="_blank" rel="noreferrer">
                <strong>{source.label}</strong>
                <span>Open official article</span>
              </a>
            ))}
          </div>
        </section>
      </main>
    </div>
  );
}

type VillageCardProps = {
  title: string;
  subtitle: string;
  village: CoordinateState;
  onXChange: (event: ChangeEvent<HTMLInputElement>) => void;
  onYChange: (event: ChangeEvent<HTMLInputElement>) => void;
};

function VillageCard({ title, subtitle, village, onXChange, onYChange }: VillageCardProps) {
  return (
    <section className="village-card">
      <div className="village-head">
        <p className="eyebrow">{subtitle}</p>
        <h3>{title}</h3>
      </div>
      <div className="coord-grid">
        <label>
          <span>X</span>
          <input type="text" inputMode="numeric" value={village.x} onChange={onXChange} />
        </label>
        <label>
          <span>Y</span>
          <input type="text" inputMode="numeric" value={village.y} onChange={onYChange} />
        </label>
      </div>
    </section>
  );
}

function buildTimeZoneOptions(browserTimeZone: string) {
  const base = [
    "UTC",
    "Europe/London",
    "Europe/Berlin",
    "Europe/Moscow",
    "Asia/Dubai",
    "Asia/Kolkata",
    "Asia/Bangkok",
    "Asia/Singapore",
    "Asia/Ho_Chi_Minh",
    "Asia/Seoul",
    "Asia/Tokyo",
    "Australia/Sydney",
    "America/New_York",
    "America/Chicago",
    "America/Denver",
    "America/Los_Angeles",
    "America/Sao_Paulo",
  ];

  return Array.from(new Set([browserTimeZone, ...base])).filter(Boolean);
}

function createInitialForm(timeZone: string): FormState {
  return {
    attackerVillage: { x: "0", y: "0" },
    defenderVillage: { x: "25", y: "-15" },
    interceptorVillage: { x: "12", y: "-4" },
    landingTime: defaultLandingTime(timeZone),
    timeZone,
    attackerTribeId: "teutons",
    attackerUnitId: "all",
    interceptorTribeId: "gauls",
    interceptorUnitId: "theutates-thunder",
    worldSize: 400,
    serverSpeed: 1,
    attackerTournamentSquareLevel: 0,
    interceptorTournamentSquareLevel: 0,
    attackerBootsBonusPercent: 0,
    interceptorBootsBonusPercent: 0,
    attackerReturnMapBonusPercent: 0,
  };
}

function getUnitsForTribe(reference: ReferenceData, tribeId: string): TroopUnit[] {
  return reference.tribes.find((tribe) => tribe.id === tribeId)?.units ?? [];
}

function sanitizeReferenceData(data: Partial<ReferenceData>): ReferenceData {
  return {
    tribes: Array.isArray(data.tribes) ? data.tribes : [],
    worldSizes: Array.isArray(data.worldSizes) && data.worldSizes.length > 0 ? data.worldSizes : [400],
    serverSpeeds: Array.isArray(data.serverSpeeds) && data.serverSpeeds.length > 0 ? data.serverSpeeds : [1, 3, 5, 10],
    mapBonuses: Array.isArray(data.mapBonuses) && data.mapBonuses.length > 0
      ? data.mapBonuses
      : [{ label: "No map", percentBonus: 0 }],
    bootsOptions: Array.isArray(data.bootsOptions) && data.bootsOptions.length > 0
      ? data.bootsOptions
      : [{ label: "No boots", percentBonus: 0 }],
    sources: Array.isArray(data.sources) ? data.sources : [],
  };
}

function normalizeForm(current: FormState, reference: ReferenceData): FormState {
  const attackerUnits = getUnitsForTribe(reference, current.attackerTribeId);
  const interceptorUnits = getUnitsForTribe(reference, current.interceptorTribeId);

  return {
    ...current,
    attackerUnitId:
      current.attackerUnitId === "all" || attackerUnits.some((unit) => unit.id === current.attackerUnitId)
        ? current.attackerUnitId
        : "all",
    interceptorUnitId: interceptorUnits.some((unit) => unit.id === current.interceptorUnitId)
      ? current.interceptorUnitId
      : interceptorUnits[0]?.id ?? "",
    worldSize: reference.worldSizes.includes(current.worldSize) ? current.worldSize : reference.worldSizes[0],
    serverSpeed: reference.serverSpeeds.includes(current.serverSpeed) ? current.serverSpeed : reference.serverSpeeds[0],
    attackerBootsBonusPercent: reference.bootsOptions.some((option) => option.percentBonus === current.attackerBootsBonusPercent)
      ? current.attackerBootsBonusPercent
      : reference.bootsOptions[0]?.percentBonus ?? 0,
    interceptorBootsBonusPercent: reference.bootsOptions.some((option) => option.percentBonus === current.interceptorBootsBonusPercent)
      ? current.interceptorBootsBonusPercent
      : reference.bootsOptions[0]?.percentBonus ?? 0,
    attackerReturnMapBonusPercent: reference.mapBonuses.some((bonus) => bonus.percentBonus === current.attackerReturnMapBonusPercent)
      ? current.attackerReturnMapBonusPercent
      : reference.mapBonuses[0]?.percentBonus ?? 0,
  };
}

function defaultLandingTime(timeZone: string) {
  return DateTime.now().setZone(timeZone).plus({ minutes: 90 }).toFormat("yyyy-LL-dd'T'HH:mm");
}

function parseCoordinateState(coordinate: CoordinateState, label: string) {
  const x = parseIntegerInput(coordinate.x, `${label} X`);
  const y = parseIntegerInput(coordinate.y, `${label} Y`);

  return { x, y };
}

function parseIntegerInput(rawValue: string, label: string) {
  if (rawValue.trim() === "" || rawValue === "-") {
    throw new Error(`${label} is incomplete.`);
  }

  const value = Number(rawValue);
  if (!Number.isInteger(value)) {
    throw new Error(`${label} must be an integer.`);
  }

  return value;
}

function parseLocalDateTime(rawValue: string, timeZone: string, label: string) {
  const parsed = DateTime.fromFormat(rawValue, "yyyy-LL-dd'T'HH:mm", { zone: timeZone });
  if (!parsed.isValid) {
    throw new Error(`${label} is invalid for ${formatTimeZoneLabel(timeZone)}.`);
  }

  return parsed;
}

function convertTimeZone(rawValue: string, fromTimeZone: string, toTimeZone: string) {
  const parsed = DateTime.fromFormat(rawValue, "yyyy-LL-dd'T'HH:mm", { zone: fromTimeZone });
  if (!parsed.isValid) {
    return rawValue;
  }

  return parsed.setZone(toTimeZone).toFormat("yyyy-LL-dd'T'HH:mm");
}

function formatDateTime(value: string, timeZone: string) {
  return DateTime.fromISO(value, { zone: "utc" }).setZone(timeZone).toFormat("dd LLL yyyy, HH:mm:ss ZZZZ");
}

function formatDuration(totalSeconds: number) {
  const hours = Math.floor(totalSeconds / 3600);
  const minutes = Math.floor((totalSeconds % 3600) / 60);
  const seconds = totalSeconds % 60;

  return `${hours}h ${minutes}m ${seconds}s`;
}

function formatTimeZoneLabel(timeZone: string) {
  return timeZone.replace(/_/g, " ");
}
