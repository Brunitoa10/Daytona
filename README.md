# Daytona — arcade racing en Java

Juego de carreras arcade con la estética de los salones de 1994: autos en **polígonos 3D con
sombreado plano**, HUD con **dígitos de 7 segmentos**, barra de RPM de LEDs, monitor **CRT** con
líneas de barrido, reloj con checkpoints, 4 marchas, derrapes y hasta **40 autos en pista**.
Pistas, autos y gráficos son diseños propios dibujados por código: no hay archivos de imagen ni de sonido.

**Estilo visual** (Opciones): *Arcade 1994 (CRT)* · *Arcade HD* · *Clásico 2D (sprites)*.
**Cámaras** (tecla C / botón de vista): Lejana · Cercana · Paragolpes · Aérea.

- **Windows 7, 8, 10 y 11** (32 y 64 bits) con **Java 8 o superior**. También corre en Linux.
- 100 % Java + una DLL chiquita propia para los controles (incluida, sin instalar drivers extra).
- **Funciona sin red**: base de datos SQLite local, todo dentro de la carpeta del juego.
- Teclado, **gamepad (XInput)**, **volante + pedales + palanca H** (DirectInput/WinMM) en Windows y joysticks en Linux.

## Cómo jugar

| Opción | Pasos |
|---|---|
| Con Java 8+ instalado | Doble clic en `dist\Jugar.bat` |
| Portable (sin instalar Java) | Ejecutá `crear-portable.bat` una vez (con un Java 8) → copiá `portable\Daytona` a un pendrive → `Jugar.bat` |
| Compilar sin internet | `compilar.bat` (cualquier JDK 8+, usa las librerías de `lib\`) |

### Windows 7
- Instalá **Java 8** (Oracle Java 8 o Temurin 8). En PCs de 32 bits usá el Java de 32 bits: el juego trae la DLL de controles para x86 y x64 y elige sola.
- Para llevarlo a varias PCs con Windows 7 sin instalar nada, generá la versión portable con ese Java 8.
- Gamepads Xbox en Windows 7 usan `xinput1_3.dll` (DirectX) o `xinput9_1_0.dll` (incluida en Windows 7).
- El juego activa el temporizador de 1 ms de Windows para que el límite de FPS no tironee.
| Maven / NetBeans | Abrí la carpeta como proyecto Maven (`pom.xml`) |

### Modos de juego
- **Arcade**: carrera contra el reloj; cada checkpoint y cada vuelta suman tiempo. 11 rivales (configurable hasta 19).
- **Campeonato**: las 4 pistas seguidas, puntos 10-8-6-5-4-3-2-1.
- **Contrarreloj**: solo vos y el **fantasma** de tu mejor vuelta (se guarda en la base).
- **Duelo**: mano a mano contra el rival más rápido.

### Pistas
| Pista | Nivel | Características |
|---|---|---|
| Sunrise Speedway | Principiante | Óvalo con muros, tribunas, rebufo |
| Red Rock Canyon | Avanzado | Colinas, eses, túnel, horquilla, atardecer |
| Neon Harbor | Experto | Ciudad nocturna, curvas de 90°, puente y túnel |
| Alpine Pass | Maestro | Montaña nevada, grandes desniveles, túnel |

Todas tienen **modo espejo**.

### Autos
Falcon GT (equilibrado), Thunder V8 (punta), Viper RS (aceleración y agarre), Comet X (6 marchas, expertos).
Caja **automática o manual**; la manual acepta **secuencial/levas** o **palanca H** con punto muerto.

### Controles
- **Teclado**: ←→/A D dirección · ↑/W acelerar · ↓/S/Espacio frenar · X/E/Shift subir · Z/Q/Ctrl bajar · 1-6 marcha directa · C cámara · Esc/P pausa · F11 pantalla completa · F3 FPS.
- **Gamepad Xbox**: stick izquierdo, RT acelerar, LT frenar, RB/LB marchas, Y cámara, Start pausa.
- **Volante**: el modo de control se elige en *Controles* o en la selección de auto. Hay un perfil estimado para Logitech (G25/G27/G29/G920/G923), Thrustmaster y genéricos; para cualquier otro usá **"Asignar ejes y botones (asistente)"**: te pide girar, pisar cada pedal y enganchar cada marcha, y mide solo el reposo y el recorrido (sirve para pedales invertidos o ejes combinados). El monitor **"Prueba en vivo"** muestra dirección, pedales, marcha, ejes y botones crudos.
- **Derrape**: toque de freno + volantazo a fondo en curva rápida.
- **Largada perfecta**: mantené el motor a media potencia durante la cuenta regresiva.

## Base de datos
Por defecto **SQLite** en `data\daytona.db` (junto al .jar). Guarda opciones, perfiles de controles,
top 10 por pista/modo y la mejor vuelta con su fantasma comprimido.
Para usar **PostgreSQL** editá `config.properties` (se crea al primer arranque):
```
db.type=postgres
db.postgres.url=jdbc:postgresql://localhost:5432/daytona
db.postgres.user=postgres
db.postgres.password=...
```
Si PostgreSQL no responde, el juego vuelve solo a SQLite (nunca deja de arrancar).

## Arquitectura

```
com.daytona
├── app          Main, GameApp (raíz de composición), GameLoop (120 Hz fijo + interpolación), GameWindow
├── config       AppConfig (config.properties), Settings tipadas, enums de opciones
├── core         Screen, ScreenManager (fundidos), EventBus + GameEvent
├── domain
│   ├── car      CarSpec, CarCatalog, Car, CarPhysics, Transmission (AT/MT), DriveCommand
│   ├── track    Track, Segment, TrackBuilder (DSL), TrackCatalog, tracks/*
│   └── race     Race, Racer, AiDriver, GameMode + modes/*, CollisionSystem, Ghost*, ChampionshipSession
├── input        InputManager, KeyboardState, Keyboard/JoystickDriveSource
│   ├── joystick XInputBackend, WinMmBackend (JNI: NativeInput + native/daytona_input.c), LinuxJsBackend, Composite/Null
│   └── binding  Binding, BindingProfile, DefaultProfiles, BindingWizard
├── audio        SynthAudio (motor, gomas, choques sintetizados), RaceSoundEffects
├── persistence  Database (SQLite/PostgreSQL + migraciones), repositorios JDBC
├── render       RoadRenderer, RaceView, HudRenderer, Camera, SpriteAtlas, art/*
│   ├── road     RoadFrame (proyección) + capas GroundLayer, TunnelLayer, SceneryLayer
│   ├── car      CarLayer, CarRenderer, MeshCarRenderer (3D), SpriteCarRenderer, CarMesh
│   ├── hud      HudSkin, Arcade94HudSkin, ModernHudSkin, SevenSegment
│   └── theme    VisualTheme (Abstract Factory), temas, PostProcessor (CRT / suave)
└── screens      Título (demo), menús, selección, carrera, resultados, campeonato, controles, opciones, récords
```

### Patrones de diseño aplicados
| Patrón | Dónde |
|---|---|
| State | `Screen` + `ScreenManager` |
| Strategy | `Driver` (humano/IA), `GameMode`, `Transmission`, `DriveInputSource`, `CarRenderer` (3D/sprite), `HudSkin`, `PostProcessor` |
| Abstract Factory | `VisualTheme` → `Arcade94Theme`, `ArcadeHdTheme`, `Classic2dTheme` (autos + HUD + pantalla coherentes) |
| Observer | `EventBus` → sonido, HUD, cámara |
| Builder | `TrackBuilder` (DSL de pistas), `GridBuilder` (grilla de largada) |
| Chain / capas | `SegmentLayer`: `GroundLayer`, `TunnelLayer`, `SceneryLayer`, `CarLayer` recorridas por `RoadRenderer` |
| Factory | `CarCatalog`, `TrackCatalog`, `Transmissions`, `GameModeType`, `JoystickBackends`, `Navigator` |
| Composite | `CompositeBackend` (XInput + DirectInput) |
| Null Object | `NullBackend`, `SilentAudio` |
| Facade | `InputManager` |
| Flyweight | `SpriteAtlas`, `FogPalette`, `CarMesh` (un modelo para todos los autos), caché de glifos |
| Template Method | `MenuScreen` |
| Command | ítems de `MenuList` |
| Repository | `SettingsRepository`, `RecordRepository`, `BindingRepository` |

**SOLID**
- **S**: la física no dibuja, la carrera no conoce dispositivos; `Race` delega la grilla (`GridBuilder`) y las
  posiciones (`Standings`); el motor de pista delega cada cosa en una capa; el HUD delega el aspecto en un skin.
- **O**: agregar una pista, un auto, un modo, un estilo visual, una capa de render o un backend de joystick es
  agregar una clase, sin modificar las existentes.
- **L**: cualquier `Driver`, `CarRenderer`, `HudSkin` o `JoystickBackend` se puede usar en lugar de otro.
- **I**: interfaces chicas (`RaceContext` de solo lectura para la IA, `DisplayControl` para las pantallas).
- **D**: el dominio y las pantallas dependen de abstracciones (repositorios, backends, temas), y todo se
  conecta en un único lugar (`GameApp`).

### Fluidez
Física a paso fijo de 120 Hz con **interpolación** de posición en el render, imagen volátil en VRAM
(Direct3D en Windows), resolución interna configurable escalada a la ventana, sprites pre-generados
y convertidos al formato de la placa, colores con niebla cacheados y límite de FPS preciso (60–240 o sin límite).

## Controles nativos (DLL)
`native/daytona_input.c` es la fuente de la DLL (WinMM + XInput dinámico). Las DLL ya compiladas están en
`src/main/resources/native` (dentro del .jar) y en `dist/lib/native`. Solo importan `kernel32`, `msvcrt` y
`winmm`, por eso funcionan desde Windows 7. Para recompilarlas: `native/compilar-dll.sh` (MinGW-w64).

## Tests
`src/test/java`: pistas válidas y espejo, física (aceleración, punto muerto, banquina), carreras
completas simuladas (la IA termina todas las pistas y un buen piloto no se queda sin tiempo en Arcade),
repositorios SQLite, pedales invertidos, asistente de asignación y fantasma.

## Ideas para seguir
Force feedback en volantes (DirectInput), música chiptune sintetizada, clima (lluvia), pantalla dividida
para 2 jugadores, editor de pistas que guarde en la base, y ranking online opcional cuando haya red.

## Licencia
El código de Daytona se distribuye bajo la **licencia MIT** (ver [`LICENSE`](LICENSE)).
© 2026 Bruno Ariel Parisi.

Las bibliotecas de `lib/` (SQLite JDBC, PostgreSQL JDBC y SLF4J) conservan sus propias licencias:
el detalle está en [`THIRD-PARTY-NOTICES.txt`](THIRD-PARTY-NOTICES.txt).

"Daytona" y "Daytona USA" son marcas de SEGA. Este proyecto es un homenaje sin fines comerciales,
no está afiliado a SEGA y la licencia no otorga derechos sobre esas marcas.
