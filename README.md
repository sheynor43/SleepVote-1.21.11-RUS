# SleepVote

A lightweight Paper plugin that lets players vote to skip the night by sleeping in a bed.

Built and tested for Paper 1.21.x (targeting 1.21.11). Русская версия: [README.ru.md](README.ru.md).

## Features

- A player entering a bed at night starts a vote; other players in the same world click "Yes"/"No" in
  chat or run `/sleepvote <yes|no>`.
- If enough players vote yes within the time limit, the night is skipped and everyone's "time since rest"
  statistic is reset.
- Fully configurable vote duration and required percentage.
- English and Russian messages, selectable in the config, with the underlying language files editable.
- The countdown and the vote result can each be shown in chat or in the action bar.

## Fixed in this update

- **Nether/End false trigger**: previously, exploding a bed in the Nether or the End could start a vote
  because the plugin only checked the world time, not whether entering the bed actually succeeded. The
  listener now checks `PlayerBedEnterEvent#getBedEnterResult()` and only starts a vote when the bed was
  entered successfully (`BedEnterResult.OK`), which vanilla only reports in the Overworld at night.

## Installation

1. Download `SleepVote-<version>.jar` from the [Releases](../../releases) page.
2. Drop it into your server's `plugins/` folder and (re)start the server.
3. Edit `plugins/SleepVote/config.yml` and `plugins/SleepVote/lang/*.yml` to taste, then restart or reload
   the plugin for changes to take effect.

## Configuration (`config.yml`)

```yaml
language: ru            # en or ru

vote:
  duration-seconds: 15      # how long a vote runs
  required-percent: 50      # percentage of players in the world required to vote "yes"

display:
  countdown: CHAT           # CHAT or ACTIONBAR - where the countdown is shown
  result: CHAT               # CHAT or ACTIONBAR - where the vote result is shown

time:
  skip-speed: 20             # ticks the world time advances per server tick while skipping
```

Boss bar output is intentionally not supported, only `CHAT` and `ACTIONBAR`.

## Language files (`lang/en.yml`, `lang/ru.yml`)

The active language is picked by the `language` key in `config.yml`. The matching file is copied to
`plugins/SleepVote/lang/` on first startup and can be edited freely; `&` color codes are supported.

## Commands

- `/sleepvote yes` - vote to skip the night.
- `/sleepvote no` - vote against skipping the night.

Players can also click the `[Yes]` / `[No]` buttons that appear in chat when a vote starts.

## Building from source

Requires JDK 21 and Maven.

```bash
mvn package
```

The compiled jar is written to `target/SleepVote-<version>.jar`.

## Running the tests

The project ships with unit tests based on [MockBukkit](https://github.com/MockBukkit/MockBukkit), covering
the vote flow and the Nether/End fix:

```bash
mvn test
```

Note: `forkCount=1` is set for Surefire because MockBukkit's dynamic plugin class loading requires it.
