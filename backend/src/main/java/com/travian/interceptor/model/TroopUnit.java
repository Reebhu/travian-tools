package com.travian.interceptor.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum TroopUnit {
    LEGIONNAIRE(Tribe.ROMANS, "legionnaire", "Legionnaire", 6, UnitCategory.INFANTRY, false),
    PRAETORIAN(Tribe.ROMANS, "praetorian", "Praetorian", 5, UnitCategory.INFANTRY, false),
    IMPERIAN(Tribe.ROMANS, "imperian", "Imperian", 7, UnitCategory.INFANTRY, false),
    EQUITES_LEGATI(Tribe.ROMANS, "equites-legati", "Equites Legati", 16, UnitCategory.SCOUT, true),
    EQUITES_IMPERATORIS(Tribe.ROMANS, "equites-imperatoris", "Equites Imperatoris", 14, UnitCategory.CAVALRY, true),
    EQUITES_CAESARIS(Tribe.ROMANS, "equites-caesaris", "Equites Caesaris", 10, UnitCategory.CAVALRY, true),
    BATTERING_RAM(Tribe.ROMANS, "battering-ram", "Battering Ram", 4, UnitCategory.SIEGE, false),
    FIRE_CATAPULT(Tribe.ROMANS, "fire-catapult", "Fire Catapult", 3, UnitCategory.SIEGE, false),
    SENATOR(Tribe.ROMANS, "senator", "Senator", 4, UnitCategory.CHIEF, false),
    ROMAN_SETTLER(Tribe.ROMANS, "settler", "Settler", 5, UnitCategory.SETTLER, false),

    CLUBSWINGER(Tribe.TEUTONS, "clubswinger", "Clubswinger", 7, UnitCategory.INFANTRY, false),
    SPEARMAN(Tribe.TEUTONS, "spearman", "Spearman", 7, UnitCategory.INFANTRY, false),
    AXEMAN(Tribe.TEUTONS, "axeman", "Axeman", 6, UnitCategory.INFANTRY, false),
    SCOUT(Tribe.TEUTONS, "scout", "Scout", 9, UnitCategory.SCOUT, true),
    PALADIN(Tribe.TEUTONS, "paladin", "Paladin", 10, UnitCategory.CAVALRY, true),
    TEUTONIC_KNIGHT(Tribe.TEUTONS, "teutonic-knight", "Teutonic Knight", 9, UnitCategory.CAVALRY, true),
    RAM(Tribe.TEUTONS, "ram", "Ram", 4, UnitCategory.SIEGE, false),
    CATAPULT(Tribe.TEUTONS, "catapult", "Catapult", 3, UnitCategory.SIEGE, false),
    CHIEF(Tribe.TEUTONS, "chief", "Chief", 4, UnitCategory.CHIEF, false),
    TEUTON_SETTLER(Tribe.TEUTONS, "settler", "Settler", 5, UnitCategory.SETTLER, false),

    PHALANX(Tribe.GAULS, "phalanx", "Phalanx", 7, UnitCategory.INFANTRY, false),
    SWORDSMAN(Tribe.GAULS, "swordsman", "Swordsman", 6, UnitCategory.INFANTRY, false),
    PATHFINDER(Tribe.GAULS, "pathfinder", "Pathfinder", 17, UnitCategory.SCOUT, true),
    THEUTATES_THUNDER(Tribe.GAULS, "theutates-thunder", "Theutates Thunder", 19, UnitCategory.CAVALRY, true),
    DRUIDRIDER(Tribe.GAULS, "druidrider", "Druidrider", 16, UnitCategory.CAVALRY, true),
    HAEDUAN(Tribe.GAULS, "haeduan", "Haeduan", 13, UnitCategory.CAVALRY, true),
    GAUL_RAM(Tribe.GAULS, "ram", "Ram", 4, UnitCategory.SIEGE, false),
    TREBUCHET(Tribe.GAULS, "trebuchet", "Trebuchet", 3, UnitCategory.SIEGE, false),
    CHIEFTAIN(Tribe.GAULS, "chieftain", "Chieftain", 5, UnitCategory.CHIEF, false),
    GAUL_SETTLER(Tribe.GAULS, "settler", "Settler", 5, UnitCategory.SETTLER, false),

    SLAVE_MILITIA(Tribe.EGYPTIANS, "slave-militia", "Slave Militia", 7, UnitCategory.INFANTRY, false),
    ASH_WARDEN(Tribe.EGYPTIANS, "ash-warden", "Ash Warden", 6, UnitCategory.INFANTRY, false),
    KHOPESH_WARRIOR(Tribe.EGYPTIANS, "khopesh-warrior", "Khopesh Warrior", 7, UnitCategory.INFANTRY, false),
    SOPDU_EXPLORER(Tribe.EGYPTIANS, "sopdu-explorer", "Sopdu Explorer", 16, UnitCategory.SCOUT, true),
    ANHUR_GUARD(Tribe.EGYPTIANS, "anhur-guard", "Anhur Guard", 15, UnitCategory.CAVALRY, true),
    RESHEPH_CHARIOT(Tribe.EGYPTIANS, "resheph-chariot", "Resheph Chariot", 10, UnitCategory.CAVALRY, true),
    EGYPTIAN_RAM(Tribe.EGYPTIANS, "ram", "Ram", 4, UnitCategory.SIEGE, false),
    STONE_CATAPULT(Tribe.EGYPTIANS, "stone-catapult", "Stone Catapult", 3, UnitCategory.SIEGE, false),
    NOMARCH(Tribe.EGYPTIANS, "nomarch", "Nomarch", 4, UnitCategory.CHIEF, false),
    EGYPTIAN_SETTLER(Tribe.EGYPTIANS, "settler", "Settler", 5, UnitCategory.SETTLER, false),

    MERCENARY(Tribe.HUNS, "mercenary", "Mercenary", 7, UnitCategory.INFANTRY, false),
    BOWMAN(Tribe.HUNS, "bowman", "Bowman", 6, UnitCategory.INFANTRY, false),
    SPOTTER(Tribe.HUNS, "spotter", "Spotter", 19, UnitCategory.SCOUT, true),
    STEPPE_RIDER(Tribe.HUNS, "steppe-rider", "Steppe Rider", 16, UnitCategory.CAVALRY, true),
    MARKSMAN(Tribe.HUNS, "marksman", "Marksman", 15, UnitCategory.CAVALRY, true),
    MARAUDER(Tribe.HUNS, "marauder", "Marauder", 14, UnitCategory.CAVALRY, true),
    BATTERING_RAM_HUN(Tribe.HUNS, "battering-ram", "Battering Ram", 5, UnitCategory.SIEGE, false),
    CATAPULT_HUN(Tribe.HUNS, "catapult", "Catapult", 4, UnitCategory.SIEGE, false),
    LOGADES(Tribe.HUNS, "logades", "Logades", 4, UnitCategory.CHIEF, false),
    HUN_SETTLER(Tribe.HUNS, "settler", "Settler", 5, UnitCategory.SETTLER, false);

    private static final Map<Tribe, List<TroopUnit>> UNITS_BY_TRIBE = valuesByTribe();

    private final Tribe tribe;
    private final String id;
    private final String displayName;
    private final double baseSpeed;
    private final UnitCategory category;
    private final boolean cavalry;

    TroopUnit(
            Tribe tribe,
            String id,
            String displayName,
            double baseSpeed,
            UnitCategory category,
            boolean cavalry
    ) {
        this.tribe = tribe;
        this.id = id;
        this.displayName = displayName;
        this.baseSpeed = baseSpeed;
        this.category = category;
        this.cavalry = cavalry;
    }

    @JsonIgnore
    public Tribe getTribe() {
        return tribe;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return displayName;
    }

    public double getBaseSpeed() {
        return baseSpeed;
    }

    public UnitCategory getCategory() {
        return category;
    }

    public boolean isCavalry() {
        return cavalry;
    }

    public static List<TroopUnit> forTribe(Tribe tribe) {
        return UNITS_BY_TRIBE.getOrDefault(tribe, List.of());
    }

    public static TroopUnit fromIds(String tribeId, String unitId) {
        Tribe tribe = Tribe.fromId(tribeId);

        return forTribe(tribe).stream()
                .filter(unit -> unit.id.equals(unitId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown unit: " + unitId + " for tribe " + tribeId));
    }

    private static Map<Tribe, List<TroopUnit>> valuesByTribe() {
        return java.util.Arrays.stream(values())
                .collect(Collectors.groupingBy(
                        TroopUnit::getTribe,
                        () -> new EnumMap<>(Tribe.class),
                        Collectors.toUnmodifiableList()
                ));
    }
}
