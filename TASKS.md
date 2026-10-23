# Castle Escape – rozdělení práce pro 4 lidi

Zadání: Game Design Document (`Castle Escape.pdf`). Čísla v závorkách odkazují na jeho kapitoly.

**Stav teď:** funguje pohyb mezi pár hardcoded místnostmi (`Game.buildWorld()`), příkazy N/E/S/W/I/P/L/H/Q a sbírání předmětů. Chybí vše ostatní.

## Git workflow

- Používáme jen dvě větve: `dev` (sem pracují všichni) a `main` (jen pro odevzdání, merge z `dev` až na konci).
- Před každou prací: `git pull origin dev`. Pak commit a hned `git push origin dev`.
- Commitujte malé celky a často, ať se konflikty řeší po kouskách a ne na konci.
- Pokud push selže, protože někdo pushnul před vámi: znovu `git pull origin dev`, vyřešit konflikt a pushnout.
- Každý edituje hlavně své soubory (viz sekce členů níže). Do `Game.java` sahejte co nejméně a jen malými zásahy, je to kolizní soubor. Nové věci dávejte do nových tříd.
- Nerozbíjejte `dev`: před pushem ověřte, že se projekt zkompiluje a hra jde spustit.

## Domluva předem (hotovo do 1. dne, ať se neblokujete)

Než začnete, odsouhlaste si tyto rozhraní. Každý pak může psát proti nim, i když ostatní část ještě nemá.

| Třída | Co musí nabízet |
|---|---|
| `Stats` | `strength`, `wisdom`, `agility`, `health` (start 10), `get/set/add` |
| `Item` | `name`, `Stats modifier` (jaká vlastnost a o kolik); `Weapon extends Item` má navíc `damage` |
| `Monster` | `name`, `health`, `strength`, `agility`, `maxHealth` |
| `Room` | `getMonsters()`, `getEntryCondition()`, `isWinRoom()`, `getWelcomeMessage()` |
| `EntryCondition` | `stat`, `minValue`, `failMessage`, `boolean isMetBy(Player)` |
| `Player` | `Stats getStats()`, `getInventory()`, `getEquippedWeapons()`, `getPreviousRoom()` |

---

## Člen 1 (Václav) – Načítání světa z CSV (kap. 2, 3, 7)

Priorita: **Critical**. Ostatní na tom závisí, takže začněte první a rychle pushněte alespoň kostru.

- Soubor `data/world.csv` (nebo víc souborů): místnosti (ID, název, popis), propojení (S/V/J/Z), předměty, zbraně, nepřátelé, podmínky vstupu, vítězná místnost + uvítací zpráva.
- Třída `WorldLoader` – načte CSV a vrátí startovní místnost (ID 0). Nahradí `Game.buildWorld()`.
- Celá mapa podle GDD: místnosti 0–11 a 101 (Secret Chamber napojená na 5 západně).
- Rozmístění: Magical Flower v 0; Shield a Sword v 1; Spider v 5; Rat + Fat Rat v 9; Heavy Claymore v 101; Dragon v 10; Siren King v 7.
- Rozumné chování při chybném/chybějícím souboru (srozumitelná chyba, ne stacktrace).

Soubory: `WorldLoader.java`, `data/*.csv`, drobná úprava `Game.run()`.

## Člen 2 (Šimon) – Hráč, statistiky, předměty + ukládání (kap. 4, 6, 8)

- `Stats` a `Player`: strength, wisdom, agility, health, všechny na 10.
- `Item` s modifikátorem vlastnosti, `Weapon` s poškozením. Předměty z GDD: Magical Flower (+10 wisdom), Potion (+30 health), Sword (+10 dmg, +12 strength), Heavy Claymore (50 dmg, −20 strength), Shield.
- Sebrání předmětu okamžitě upraví vlastnosti a vypíše to (`Your wisdom has increased to 20.`). Zbraně jdou i do seznamu zbraní pro boj.
- Příkaz **D** (zahodit): zeptá se na název, vlastnosti se odečtou, předmět zůstane v aktuální místnosti.
- **I** vypíše inventář i aktuální vlastnosti.
- **Ukládání (Critical) a načítání hry** (kap. 8): pozice, vlastnosti, inventář, stav místností (předměty na zemi, živí nepřátelé). Při načtení se nejdřív postaví čistý svět a pak se přepíše uloženým stavem. Příkazy např. `SAVE` / `LOAD` (domluvte se s členem 3 na jejich zapojení do `Game`).
- Ukládání je Critical, takže ho nenechávejte na poslední chvíli: rozjeďte ho, jakmile budou hotové `Stats`, `Player` a základ `Room` (tedy brzy), a stav nepřátel doplňte, až bude `Monster` od Marka.

Soubory: `Stats.java`, `Player.java`, `Item.java`, `Weapon.java`, `SaveManager.java`.

## Člen 3 (Matúš) – Pohyb, podmínky, konec hry, texty (kap. 2, 6, 9)

- `EntryCondition` a kontrola při pohybu: Hradní hala vyžaduje wisdom 15, Tajná komnata wisdom 30, Villedor strength 30. Při nesplnění se vypíše chybová zpráva a hráč zůstane (`Nelze vstoupit: Nevíš, jak se dostat do hradu.`). Zprávy jsou v `data/conditions.csv`.
- Vítězná místnost: vstup do Villedor vypíše uvítací zprávu a hru ukončí vítězstvím.
- Konec hry smrtí, když zdraví klesne na 0 (napojit na souboj od člena 4).
- Výpis popisu místnosti při vstupu (`Přesun do: Hradní hala` + popis), úvodní text a nápověda `H` podle GDD (kap. 9–10).
- Hlášky: `Neplatný vstup. Zadej N, E, S, W, I, P, D, H nebo Q.` a `Tímto směrem se z místnosti ... jít nedá.`
- **Celá hra je česky** (hlášky, názvy místností, předměty a nepřátelé v CSV, popisy). Držte se toho ve všech nových textech. V kódu zůstávají anglické identifikátory (např. klíče `wisdom`, `strength` v CSV).
- Zapojit příkazy `D` a `SAVE/LOAD` do `handleCommand` (volají metody členů 2).
- **Nice to have:** logování celého průběhu do `log.txt` (`GameLogger`, zapisuje vstupy i výstupy hry).

Soubory: `Game.java` (hlavní vlastník), `Room.java`, `EntryCondition.java`, `GameLogger.java`.

## Člen 4 (Marek) – Nepřátelé a souboj (kap. 5, 6)

- `Monster`: Rat / Fat Rat (zdraví 5, síla 5), Spider, Siren King (zdraví 100, síla 25), Dragon (zdraví 100, síla 12). Doladit hodnoty tak, aby hra šla vyhrát.
- Varování při vstupu do místnosti s nepřáteli. Hráč si vybere, zda bojuje, nebo ustoupí zpět.
- `CombatSystem`:
  - začíná ten, kdo má vyšší obratnost (agility),
  - hráčovo poškození = základní síla + součet poškození všech držených zbraní,
  - střídání tahů, dokud jeden nezemře,
  - nepřátelé se porážejí postupně jeden po druhém.
- Následky: při prohře (a přežití) hráč ztratí polovinu výchozího zdraví z doby před bojem a ustoupí do předchozí místnosti. Při výhře nad všemi se zdraví hráče obnoví na hodnotu před prvním soubojem v dané místnosti. Při smrti konec hry.
- **Nice to have:** boss hlášky a speciální útoky (Dragon: zemětřesení a řev před vstupem).

Soubory: `Monster.java`, `CombatSystem.java`, drobná úprava `Room` (seznam monster) a `Game.move()`.

---

## Pořadí a závislosti

1. Všichni: odsouhlasit tabulku rozhraní výše (cca hodina).
2. Člen 1 pushne kostru loaderu, ostatní zatím pracují na hardcoded mapě.
3. Členové 2, 3, 4 pracují paralelně na svých třídách.
4. Šimon začne ukládáním, jakmile jsou hotové `Stats`, `Player` a `Room`. Stav nepřátel a souboje doplní po zmergování Markovy části.
5. Společně: průchod celou hrou od přístavu po Villedor, oprava chyb, README s návodem ke spuštění.

## Priority podle GDD

- **Critical:** načtení CSV, svět a pohyb, sbírání a statistiky, výhra a smrt, ukládání a načítání hry (členové 1, 2, 3).
- **Should be:** souboj, dialogy P/D, blokování místností s varováním (členové 2, 3, 4).
- **Nice to have:** log do souboru, rozšířené bossy (členové 3, 4).
