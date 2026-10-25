# Nebula Client (Fabric, Minecraft 26.2)

Клиентский мод: ClickGUI + модули **AutoMine**, **SwingAnimations**, **NameTags**, **Ambience**.

## Сборка .jar

Нужно: **JDK 25** и **Gradle 9.5.1** (или новее), доступ в интернет (Fabric Maven).

```
gradle build
```

Готовый мод: `build/libs/nebula-1.0.0.jar` (файл `-sources.jar` не нужен).

Если хотите wrapper: один раз `gradle wrapper --gradle-version 9.5.1`, дальше `./gradlew build`.

## Установка

1. Fabric Loader 0.19.3+ для Minecraft 26.2
2. Fabric API 0.161.0+26.2 в папку `mods`
3. `nebula-1.0.0.jar` в папку `mods`

Меню: **Right Shift** (меняется в Управлении → Misc). Клик по карточке модуля разворачивает настройки,
переключатель справа включает модуль. Настройки сохраняются в `config/nebula.properties`.

## Модули

| Модуль | Категория | Что делает |
|---|---|---|
| SwingAnimations | Render | Smooth / Spin / Poke / Chop / Slide, сила и скорость взмаха |
| NameTags | Render | Неймтеги со здоровьем и дистанцией, видны сквозь стены |
| Ambience | Render | Своё время суток, погода (Clear/Rain/Thunder), цветной фильтр |
| AutoMine | Misc | Копает блок под прицелом (Hold или Pulse) |

## Если сборка падает

Весь код, который трогает рендер-API игры, в `gui/Draw.java`: поправьте там имена методов
(`fill`, `text`, `enableScissor`). Остальные места, где возможны расхождения в 26.2:
`NebulaClient.java` (регистрация клавиши и HUD), сигнатуры `mouseClicked/keyPressed/charTyped` в `ClickGui.java`.
Миксины помечены `require = 0`: если метод переименован, мод загрузится, но анимация взмаха не сработает.

## Сборка без установки JDK/Gradle (через GitHub)

1. Создайте новый репозиторий на github.com и загрузите в него все файлы проекта (включая папку `.github`).
2. Вкладка **Actions** → workflow **build** → дождитесь зелёной галочки (запускается сам при загрузке).
3. Откройте завершённый запуск → внизу **Artifacts** → скачайте `nebula-mod`. Внутри лежит готовый `nebula-1.0.0.jar`.
4. Положите его в папку `mods`.
