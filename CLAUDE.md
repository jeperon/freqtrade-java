# CLAUDE.md — freqtrade-java

AI assistant guide for understanding and working with this codebase.

---

## Project Overview

**freqtrade-java** is a Java port of the [freqtrade](https://github.com/gcarq/freqtrade) Python cryptocurrency high-frequency trading bot. It connects to the Bittrex exchange, applies technical analysis indicators to detect buy signals, executes limit orders, and manages open positions against configurable profit/stop-loss targets. Control is available via a Telegram bot interface.

**This software is for educational purposes only.**

- **Group/Artifact:** `ch.urbanfox:freqtrade`
- **Version:** `0.0.1-SNAPSHOT`
- **Java:** 11
- **Framework:** Spring Boot 2.7.18
- **Build:** Maven (wrapper included)

---

## Repository Structure

```
freqtrade-java/
├── pom.xml                          # Maven build descriptor
├── mvnw / mvnw.cmd                  # Maven wrapper scripts
├── .mvn/wrapper/                    # Maven wrapper configuration
├── .travis.yml                      # CI (Travis CI, language: java)
├── README.md
├── src/
│   ├── main/
│   │   ├── java/ch/urbanfox/freqtrade/
│   │   │   ├── FreqTradeApplication.java        # Spring Boot entry point (@SpringBootApplication)
│   │   │   ├── FreqTradeMainRunner.java          # Core trading loop
│   │   │   ├── FreqTradeConfiguration.java       # Spring @Configuration beans
│   │   │   ├── FreqTradeProperties.java          # @ConfigurationProperties(prefix="freqtrade")
│   │   │   ├── BittrexProperties.java            # Bittrex API key/secret config
│   │   │   ├── analyze/                          # Technical analysis
│   │   │   │   ├── AnalyzeService.java           # Interface
│   │   │   │   ├── AnalyzeServiceImpl.java       # ta4j-based implementation
│   │   │   │   └── indicator/                    # Custom ta4j indicators
│   │   │   │       ├── BuySignalIndicator.java   # Composite buy signal (EMA + SAR + ADX)
│   │   │   │       ├── UpSwingIndicator.java     # Price > EMA check
│   │   │   │       ├── SwapIndicator.java        # Bearish-to-bullish SAR transition
│   │   │   │       └── ShiftedIndicator.java     # Utility: shifts indicator by N periods
│   │   │   ├── exchange/                         # Exchange API wrapper
│   │   │   │   ├── FreqTradeExchangeService.java # Interface
│   │   │   │   ├── FreqTradeExchangeServiceImpl.java # Bittrex (KnowM XChange) implementation
│   │   │   │   ├── converter/
│   │   │   │   │   └── BittrexDataConverter.java # Converts raw Bittrex candles → ta4j TimeSeries
│   │   │   │   └── exception/
│   │   │   │       └── FreqTradeExchangeInitializationException.java
│   │   │   ├── trade/                            # Trade persistence & execution
│   │   │   │   ├── TradeEntity.java              # JPA entity (@Entity → 'trades' table)
│   │   │   │   ├── TradeService.java             # Interface
│   │   │   │   ├── TradeServiceImpl.java         # @Transactional implementation
│   │   │   │   └── TradeRepository.java          # Spring Data JPA repository
│   │   │   ├── telegram/                         # Telegram bot integration
│   │   │   │   ├── TelegramService.java          # Interface
│   │   │   │   ├── TelegramServiceImpl.java      # @PostConstruct init, long-polling bot
│   │   │   │   ├── FreqTradeTelegramBot.java     # Extends TelegramLongPollingBot
│   │   │   │   ├── TelegramProperties.java       # enabled, token, chat-id, bot-name
│   │   │   │   └── command/                      # One class per Telegram command
│   │   │   │       ├── CommandHandler.java       # Interface
│   │   │   │       ├── AbstractCommandHandler.java # Base with @EventListener
│   │   │   │       ├── StartCommandHandler.java  # /start → State.RUNNING
│   │   │   │       ├── StopCommandHandler.java   # /stop  → State.STOPPED
│   │   │   │       ├── StatusCommandHandler.java # /status → list open trades
│   │   │   │       ├── ProfitCommandHandler.java # /profit → cumulative profit
│   │   │   │       ├── PerformanceCommandHandler.java # /performance
│   │   │   │       └── ForceSellCommandHandler.java   # /forcesell <trade_id>
│   │   │   ├── event/                            # Spring application events
│   │   │   │   ├── CommandEventListener.java     # Dispatches CommandEvent to handlers
│   │   │   │   └── model/
│   │   │   │       └── CommandEvent.java         # Published by Telegram bot
│   │   │   └── type/
│   │   │       └── State.java                    # Enum: RUNNING | SLEEP | STOPPED
│   │   └── resources/
│   │       ├── application.properties.example   # Config template (copy → application.properties)
│   │       └── db/changelog/
│   │           └── db.changelog-master.yaml      # Liquibase schema: creates 'trades' table
│   └── test/
│       ├── java/ch/urbanfox/freqtrade/
│       │   ├── FreqTradeApplicationTests.java    # Integration test (disabled; needs credentials)
│       │   ├── telegram/TelegramServiceTester.java
│       │   ├── tester/StrategyTester.java        # Strategy backtesting
│       │   └── tester/GatherTestDataTester.java  # Collects historical data
│       └── resources/
│           ├── application-test.properties       # Test profile config
│           └── data/ETH_BTC.json                 # Sample candle data for tests
```

---

## Build & Run

### Prerequisites

- Java 11 (exact version required; Spring Boot 2.7.x supports Java 11–17)
- Maven (use the included wrapper — do not rely on system Maven)

### Common Commands

```bash
# Run all tests
./mvnw test

# Build executable JAR
./mvnw package

# Run the application
java -jar target/freqtrade-0.0.1-SNAPSHOT.jar

# Skip tests during build
./mvnw package -DskipTests
```

### Configuration Setup

`src/main/resources/application.properties` is git-ignored. Copy the example and fill in credentials:

```bash
cp src/main/resources/application.properties.example src/main/resources/application.properties
```

Key properties:

| Property | Description |
|---|---|
| `freqtrade.dry-run` | `true` = simulate orders; no real trades |
| `freqtrade.initial-state` | `RUNNING`, `SLEEP`, or `STOPPED` |
| `freqtrade.max-open-trades` | Max concurrent open positions |
| `freqtrade.stake-currency` | Base currency (e.g. `BTC`) |
| `freqtrade.stake-amount` | Amount per trade in stake currency |
| `freqtrade.minimal-roi.<minutes>` | Minimum profit threshold at duration |
| `freqtrade.stop-loss` | Loss fraction to trigger forced sell (e.g. `-0.10`) |
| `freqtrade.pair-whitelist` | Comma-separated pairs, e.g. `ETH/BTC,NEO/BTC` |
| `freqtrade.bittrex.key` | Bittrex API key |
| `freqtrade.bittrex.secret` | Bittrex API secret |
| `freqtrade.telegram.enabled` | `true` / `false` |
| `freqtrade.telegram.token` | Telegram bot token |
| `freqtrade.telegram.chat-id` | Telegram chat ID |

---

## Architecture & Key Concepts

### Trading Loop (`FreqTradeMainRunner`)

The main loop runs every **25 seconds** (Bittrex rate-limit compliance):

1. Load all open trades from the database.
2. If below `max-open-trades`, call `createTrade()`:
   - Checks available balance.
   - Iterates the pair whitelist; fetches 6 hours of 5-minute candles from Bittrex.
   - Runs `AnalyzeService.getBuySignal()` on each pair's `TimeSeries`.
   - On signal: place a limit buy order, persist a `TradeEntity`.
3. For each open trade: check for pending open orders on the exchange.
4. Once an order is filled, evaluate `shouldSell()`:
   - **Stop-loss:** current profit < `stop-loss` threshold → sell immediately.
   - **Minimal ROI:** iterates `minimal-roi` entries (sorted ascending by duration); sells if trade age and profit both meet a threshold.
5. Execute sell via `FreqTradeExchangeService` and update the DB record.

**State machine:**
- `RUNNING` → execute `process()` then sleep 25 s
- `SLEEP` → sleep 1 s (idle, awaiting Telegram `/start`)
- `STOPPED` → exit loop

### Technical Analysis (`analyze/`)

The buy signal is a composite of three ta4j indicators (33-period window over 5-minute candles ≈ 2.75 hours):

| Indicator | Class | Logic |
|---|---|---|
| EMA trend | `UpSwingIndicator` | Price > EMA(33) |
| SAR flip | `SwapIndicator` | SAR transitions from bearish to bullish |
| ADX strength | (inline in `BuySignalIndicator`) | ADX(33) > 0.25 |

All three must be true simultaneously for a buy signal.

### Exchange Service (`exchange/`)

- Wraps KnowM XChange library for Bittrex.
- **Rate limiting:** `RateLimiter.create(0.03)` (≈ 1 request per 33 s) applied to all exchange calls.
- Supports **dry-run mode**: buy/sell methods return a fake order ID and log without executing.
- `BittrexDataConverter` converts raw `BittrexChartData` lists into a ta4j `TimeSeries`.

### Persistence (`trade/`)

- **Database:** HSQLdb (`jdbc:hsqldb:file:freqtrade.db`); file-based by default.
- **Schema management:** Liquibase (`db/changelog/db.changelog-master.yaml`).
- `TradeEntity` fields: `exchange`, `pair`, `isOpen`, `openRate`, `closeRate`, `closeProfit`, `stakeAmount`, `amount`, `openDate`, `closeDate`, `openOrderId`.
- `TradeRepository` is a Spring Data JPA interface; custom queries added as needed.

### Telegram Integration (`telegram/`)

- `FreqTradeTelegramBot` extends `TelegramLongPollingBot` (long-polling, no webhook).
- Incoming messages are published as `CommandEvent` via Spring's `ApplicationEventPublisher`.
- `CommandEventListener` dispatches to the appropriate `CommandHandler` via an async `ThreadPoolTaskExecutor` (4 threads).
- Telegram is **optional**: set `freqtrade.telegram.enabled=false` to disable.

---

## Coding Conventions

### Style

- **Classes:** PascalCase (`FreqTradeMainRunner`, `TradeEntity`)
- **Methods/fields:** camelCase (`getOpenRate`, `stakeAmount`)
- **Constants:** `UPPER_SNAKE_CASE` (`LOGGER`)
- **Packages:** lowercase reverse-domain (`ch.urbanfox.freqtrade.*`)
- Encoding: UTF-8 everywhere.

### Spring Patterns

- All services have a corresponding interface (`XxxService` / `XxxServiceImpl`).
- Constructor injection is preferred; `@Autowired` on fields is used in this codebase — follow the existing pattern when adding new beans.
- Configuration properties use `@ConfigurationProperties` with a type-safe POJO (never `@Value` for structured config).
- Use `@Transactional` on service methods that write to the database.
- Use `@PostConstruct` for bean initialization that requires injected dependencies.

### Financial Arithmetic

- Always use `BigDecimal` for monetary values and rates — never `double` or `float`.
- Use `MathContext` when rounding for display; preserve full precision in the database.

### Logging

- One `private static final Logger LOGGER = LoggerFactory.getLogger(ClassName.class);` per class.
- Use parameterized logging: `LOGGER.info("msg {}", value)` — no string concatenation.
- Use `DEBUG` for verbose/diagnostic output; `INFO` for trade events; `WARN` for recoverable errors.

### Exception Handling

- Checked exceptions (`IOException`, `TelegramApiException`) are caught at the `process()` level in `FreqTradeMainRunner` and logged as warnings — the loop continues.
- Do not swallow exceptions silently; always log at minimum.
- `RuntimeException` in the main loop triggers a Telegram notification and exits.

---

## Testing

### Test Profiles & Configuration

- Tests run under the `test` Spring profile (`@ActiveProfiles("test")`).
- `src/test/resources/application-test.properties` sets:
  - Log level: DEBUG
  - Telegram: disabled
  - Bittrex credentials: dummy values (tests do not hit the real exchange)

### Running Tests

```bash
./mvnw test
```

### Test Files

| File | Purpose |
|---|---|
| `FreqTradeApplicationTests.java` | Spring context integration test; currently `@Disabled` (requires real credentials) |
| `StrategyTester.java` | Backtest the buy signal strategy against historical data |
| `GatherTestDataTester.java` | Fetch and serialize candle data for offline testing |
| `TelegramServiceTester.java` | Manual Telegram integration verification |

### Adding Tests

- Place unit tests alongside their subject class in `src/test/java/ch/urbanfox/freqtrade/`.
- Use JUnit 5 (`@Test`, `@BeforeEach`) and Spring Boot Test (`@SpringBootTest`) where context is needed.
- Mock external dependencies (exchange, Telegram) with `@MockBean` to keep tests offline.

---

## Key Dependencies

| Dependency | Version | Purpose |
|---|---|---|
| `spring-boot-starter-parent` | 2.7.18 | Framework BOM and defaults |
| `spring-boot-starter-data-jpa` | (managed) | JPA/Hibernate ORM |
| `liquibase-core` | (managed) | Database schema migrations |
| `hsqldb` | (managed) | Embedded SQL database |
| `ta4j` | 0.9 | Technical analysis indicators |
| `xchange-core` / `xchange-bittrex` | 4.2.3 | Exchange API (KnowM XChange) |
| `telegrambots` | 3.4 | Telegram Bot API |
| `commons-lang3` | 3.18.0 | Apache utilities (e.g. `Pair`) |
| `hibernate-validator` | 8.0.3.Final | Bean validation |

---

## Files That Are Git-Ignored

Do **not** commit these files:

- `src/main/resources/application.properties` — contains API keys and secrets
- `freqtrade.db.*` — local HSQLdb database files
- `target/` — Maven build output
- IDE metadata (`.idea/`, `.classpath`, `.project`, etc.)

---

## CI

Travis CI (`.travis.yml`) runs `./mvnw test` on every push. The configuration is minimal (`language: java`); Maven defaults handle compilation and test execution.

---

## Common Tasks for AI Assistants

### Adding a New Telegram Command

1. Create `src/main/java/ch/urbanfox/freqtrade/telegram/command/MyCommandHandler.java` extending `AbstractCommandHandler`.
2. Override `getSupportedCommand()` to return the command string (e.g. `"/mycommand"`).
3. Implement `handle(CommandEvent event)`.
4. Spring will auto-detect the new `@Component` via classpath scanning — no manual wiring needed.

### Adding a New Technical Indicator

1. Create a class in `analyze/indicator/` that extends `CachedIndicator<Boolean>` (ta4j 0.9 API).
2. Inject it into `BuySignalIndicator` constructor and add it to the composite signal logic.

### Adding a New Configuration Property

1. Add the field (with getter/setter) to `FreqTradeProperties.java`.
2. Add the corresponding entry to `application.properties.example`.
3. Reference via `@Autowired FreqTradeProperties properties`.

### Modifying the Database Schema

1. Add a **new** Liquibase changeset to `db/changelog/db.changelog-master.yaml` — never edit existing changesets.
2. Update `TradeEntity.java` with the corresponding JPA field(s).

### Enabling Dry-Run Mode

Set `freqtrade.dry-run=true` in `application.properties`. The exchange service will simulate all orders without touching real funds.
