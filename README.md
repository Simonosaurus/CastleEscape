# Castle Escape

Textová hra v Javě pro konzoli. Projekt do předmětu 4IT107 Programování v Javě (VŠE Praha, FIS, 2026/2027 ZS).

Neznámý hrdina se ocitne v hradním přístavu a chce se hradem probojovat do rodné vesnice Villedor. Cestou sbírá předměty, které mu mění vlastnosti, a bojuje s nepřáteli. Hra končí vítězstvím po vstupu do Villedoru, nebo smrtí, pokud zdraví klesne na nulu.

Podrobné zadání je v dokumentu *Castle Escape* (Game Design Document). Rozdělení práce v týmu je v [TASKS.md](TASKS.md).

## Autoři

Václav, Šimon, Matúš a Marek.

## Požadavky

- JDK 21 nebo novější (kód používá `StringBuilder.repeat`, `switch` s šipkami, textové bloky a `instanceof` s vzorem).

## Spuštění

Z kořenové složky projektu (kde je složka `data/`):

```bash
javac -d out src/*.java
```

```bash
java -cp out Main
```

V IntelliJ IDEA stačí spustit `Main` a nechat pracovní složku nastavenou na kořen projektu. Složka `out/` je v `.gitignore`.

## Ovládání

| Příkaz | Význam |
|---|---|
| `N` / `E` / `S` / `W` | pohyb na sever, východ, jih, západ |
| `I` | vypíše inventář a vlastnosti hráče |
| `P` | sebere předmět (hra se zeptá na přesný název) |
| `D` | zahodí předmět z inventáře (hra se zeptá na přesný název) |
| `L` | vypíše předměty v místnosti |
| `H` | zobrazí nápovědu |
| `SAVE` | uloží hru do souboru `savegame.txt` |
| `LOAD` | načte hru ze souboru `savegame.txt` |
| `Q` | ukončí hru |

Uložená hra je v souboru `savegame.txt` v pracovní složce (soubor je v `.gitignore`). `LOAD` vrátí pozici, vlastnosti, inventář, předměty na zemi i živé nepřátele.

## Herní mechaniky

- Hráč má čtyři vlastnosti: síla, moudrost, obratnost a zdraví. Všechny začínají na 10.
- Sebrané předměty vlastnosti trvale mění, dokud je hráč drží. Zbraně navíc přidávají poškození v boji.
- Některé místnosti vyžadují minimální vlastnost: Hradní hala moudrost 15, Tajná komnata moudrost 30, Villedor sílu 30.
- Po vstupu do místnosti s nepřáteli hra varuje a hráč si vybere, zda bojuje (`A`), nebo ustoupí (`N`). Souboj začíná ten s vyšší obratností a střídají se tahy. Poškození hráče je síla plus poškození držených zbraní.
- Při prohře hráč přijde o polovinu zdraví z doby před bojem a ustoupí do předchozí místnosti. Po výhře nad všemi nepřáteli se zdraví obnoví na hodnotu před bojem.

## Struktura projektu

```
data/           CSV soubory s výchozím stavem světa (oddělovač ;)
src/
  Main          vstupní bod
  Game          herní smyčka a zpracování příkazů
  Intro         úvodní obrazovka
  WorldLoader   načtení světa z data/*.csv
  Room, RoomArt místnosti a jejich výpis
  EntryCondition podmínka vstupu do místnosti (data/conditions.csv)
  Player, Stats, Item, Weapon   hráč, vlastnosti a předměty
  Monster, CombatSystem         nepřátelé a souboje
  SaveManager   ukládání a načítání hry (SAVE, LOAD)
```

### Datové soubory

| Soubor | Obsah |
|---|---|
| `rooms.csv` | `id;name;description;win;welcome` |
| `exits.csv` | `from_id;direction;to_id` |
| `items.csv` | `room_id;name;type;stat;value;damage` |
| `monsters.csv` | `room_id;name;health;strength;agility;intro` |
| `conditions.csv` | `room_id;stat;min_value;message` |

První řádek každého souboru je hlavička. Klíče vlastností (`strength`, `wisdom`, `agility`, `health`) a typ předmětu (`item`, `weapon`) jsou v CSV anglicky, vše, co hráč vidí, je česky.

## Práce s gitem

- Pracujeme na větvi `dev`, `main` je jen pro odevzdání.
- Před prací `git pull origin dev`, potom commit a `git push origin dev`.
- Do `Game.java` sahejte jen malými zásahy, je to nejčastější zdroj konfliktů.

## P.S.
- Když by jsme se řídili čisťe a přesně podle stanoveného GDD, matematicky by nebylo možne hru vyhrát
- Hráč by neměl dost "wisdom" aby se dostal do secret roomky a jeho itemy by neměli dost "strength" aby porazili final bosse
- Proto byli do items.csv přidány dvě kopie itemú navíc