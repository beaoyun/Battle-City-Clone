# Battle-City-Clone
A Battle City (Tank 1990) clone built in Java for CSE212 Software Development Methodologies at Yeditepe University

Features
3 playable levels with unique map layouts, each with increasing enemy difficulty
3 enemy tank types — Basic, Fast, and Armor — each with distinct speed, behavior, and durability
6 power-ups — Extra Life, Star (weapon upgrade), Bomb, Clock (freeze), Shovel (fortify base), and Shield
Map Editor — create and play custom levels using a tile-based editor
High Score system — top 10 scores saved to a CSV file with player name, score, and date
Background music with mute/unmute toggle in the Options menu
Splash screen, pause/resume, and separate YOU WIN / GAME OVER end screens
Technical Highlights
Pure Java SE — no external libraries, only the standard javax.swing, java.awt, and javax.sound.sampled APIs
Multi-threaded architecture — a dedicated GameLoop thread drives the game at 60 FPS; each enemy tank runs its own EnemyAIThread for independent AI behavior
Thread safety via a shared worldLock monitor object, synchronizing all reads and writes to the game world between the render thread and AI threads
Inheritance hierarchy — GameObject → Tank → PlayerTank / EnemyTank; Wall → BrickWall / SteelWall / Bush / Water; PowerUp with six concrete subclasses
Pixel-art rendering using nearest-neighbor interpolation to scale 16×16 NES sprites to 32×32 display pixels without blurring
How to Run
Clone the repository
Compile from the project root:
javac -encoding UTF-8 -d out $(find src -name "*.java")
Or on Windows:
dir /s /b src\*.java > sources.txt && javac -encoding UTF-8 -d out @sources.txt
Run:
java -cp out battlecity.BattleCityGame
Assets (music.wav, sprite PNGs, maps/) must remain in the project root directory alongside the src/ folder.
