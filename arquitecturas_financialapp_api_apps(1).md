# Arquitecturas usadas en FinancialApp: API y Apps

## 1. Resumen ejecutivo

El proyecto está organizado como un **monorepo** con tres grandes zonas técnicas:

```text
FinancialApp/
├── api/              # Backend REST en Spring Boot
├── apps/
│   ├── ui-core/      # Núcleo compartido de frontend: auth, puertos, clientes HTTP, hooks
│   ├── web/          # Aplicación web React + Vite
│   └── mobile/       # Aplicación mobile React + Vite + Capacitor
├── vault/            # Documentación funcional/técnica, historias y tareas
└── .github/          # CI/CD, lint, tests, build, E2E y release
```

La decisión arquitectónica central es separar el sistema en **un backend modular** y **dos apps cliente** que comparten una capa común (`ui-core`).

En términos simples:

- `api/` concentra reglas de negocio, persistencia, seguridad, integraciones externas y endpoints REST.
- `apps/ui-core/` concentra contratos de frontend, autenticación y cliente HTTP.
- `apps/web/` implementa pantallas web usando `ui-core`.
- `apps/mobile/` implementa pantallas móviles usando `ui-core` y luego se empaqueta con Capacitor.

Esto evita duplicar lógica entre web y mobile, permite trabajar por módulos y deja claro qué responsabilidad tiene cada parte.

---

## 2. Arquitectura general del monorepo

El repositorio no está dividido en proyectos aislados, sino en un **monorepo modular**. Esto significa que backend, web, mobile y librería compartida viven juntos, pero cada uno conserva su propio build, Dockerfile, tests y pipeline.

### Estructura relevante

```text
api/
├── src/main/java/com/ssv
│   ├── company/
│   ├── edgar/
│   ├── investor/
│   ├── market/
│   ├── portfolio/
│   ├── transaction/
│   ├── config/
│   └── shared/

apps/
├── ui-core/
│   └── src/
│       ├── api/
│       └── auth/
├── web/
│   └── src/
│       ├── components/
│       ├── pages/
│       └── lib/
└── mobile/
    └── src/
        ├── auth/
        └── screens/
```

### Decisión tomada

Se eligió una arquitectura donde los módulos se comunican por contratos explícitos:

```text
apps/web ──────┐
               ├──→ apps/ui-core ──→ api/
apps/mobile ───┘
```

`web` y `mobile` no deberían conocer detalles internos del backend. Ambos consumen `ui-core`, y `ui-core` es el que sabe cómo hablar con la API.

### Ventajas

- **Menos duplicación:** web y mobile no tienen que repetir lógica de autenticación, llamadas HTTP o tipos compartidos.
- **Mayor consistencia:** las dos apps consumen los mismos contratos.
- **Mejor paralelización:** una persona puede trabajar en API, otra en `ui-core`, otra en web y otra en mobile.
- **Menor acoplamiento:** si cambia el backend, idealmente se actualiza `ui-core` y las pantallas cambian poco o nada.
- **Mejor CI/CD:** cada módulo puede tener lint, test y build separado.

---

## 3. Arquitectura de la API

La API usa **Java 21 + Spring Boot + Gradle + PostgreSQL + Flyway + Spring Security + JPA**.

No es un backend MVC plano. Está más cerca de una arquitectura **modular por dominio** con inspiración en **Clean Architecture / Hexagonal Architecture**, aunque no es hexagonal pura en todos los puntos.

### 3.1 Organización por dominio

Los paquetes principales son:

```text
com.ssv.company      # Búsqueda de compañías, métricas financieras, filings SEC
com.ssv.edgar        # Cliente e integración con EDGAR
com.ssv.investor     # Usuario/inversor autenticado y provisioning
com.ssv.market       # Precios de mercado, Yahoo Finance, scheduler
com.ssv.portfolio    # Portfolio y posiciones
com.ssv.transaction  # Compra, venta e historial
com.ssv.config       # Seguridad, CORS, cache, tiempo, properties
com.ssv.shared       # Excepciones compartidas y respuestas de error
```

La elección de paquetes por dominio es importante. En vez de agrupar todo por tipo técnico:

```text
controllers/
services/
repositories/
entities/
```

se agrupa por capacidad funcional:

```text
portfolio/
company/
market/
transaction/
```

### Por qué se tomó esta decisión

Porque el sistema financiero crece por funcionalidades: portfolio, trading, research de compañías, watchlist, precios, etc. Si se organiza por dominio, cada módulo queda más autocontenido y es más fácil ubicar qué parte tocar.

### Ventajas

- **Escalabilidad funcional:** agregar `watchlist`, `analytics` o `alerts` no obliga a mezclar todo con servicios globales.
- **Mejor mantenibilidad:** cada dominio tiene sus entidades, casos de uso y adaptadores cerca.
- **Menor riesgo de cambios cruzados:** tocar `portfolio` no debería romper `company` salvo que haya una dependencia explícita.
- **Lectura más clara:** el código refleja el negocio, no solo la tecnología.

---

## 4. Capas dentro de la API

Dentro de varios módulos se repite esta estructura:

```text
module/
├── domain/
├── application/
└── infrastructure/
    ├── persistence/
    └── web/
```

Ejemplo en `company`:

```text
company/
├── application/
│   ├── CompanySearchService.java
│   ├── CompanyMetricsService.java
│   ├── CompanyStore.java
│   ├── FinancialStatementStore.java
│   └── SecFilingStore.java
├── domain/
│   ├── Company.java
│   ├── FinancialStatement.java
│   └── SecFiling.java
└── infrastructure/
    ├── persistence/
    │   ├── CompanyRepository.java
    │   ├── FinancialStatementRepository.java
    │   └── SecFilingRepository.java
    └── web/
        └── CompanyController.java
```

## 4.1 Capa `domain`

La capa `domain` contiene las entidades principales del negocio.

Ejemplos:

- `Company`
- `FinancialStatement`
- `SecFiling`
- `Investor`
- `Portfolio`
- `Position`
- `PortfolioPosition`
- `MarketPrice`
- `Transaction`

Estas clases representan conceptos centrales del sistema.

### Decisión tomada

Se decidió modelar el negocio con entidades propias en vez de trabajar directamente con JSON, mapas o DTOs genéricos.

### Ventajas

- Permite expresar reglas del dominio con tipos fuertes.
- Reduce errores por strings o estructuras informales.
- Facilita tests unitarios sobre reglas financieras.
- Deja una base más sólida para evolucionar el sistema.

### Observación importante

Las entidades del dominio también están anotadas como entidades JPA (`@Entity`). Eso simplifica el desarrollo, pero acopla parcialmente el dominio a la infraestructura de persistencia.

Por eso, la arquitectura es **hexagonal/pragmática**, no hexagonal pura. En una hexagonal estricta, el dominio no debería depender de anotaciones JPA.

Esta decisión tiene sentido en un proyecto académico/productivo inicial porque reduce complejidad, pero si el sistema creciera mucho podría convenir separar:

```text
Domain model puro
JpaEntity de persistencia
Mapper entre ambos
```

---

## 4.2 Capa `application`

La capa `application` contiene los casos de uso y servicios de aplicación.

Ejemplos:

```text
CompanySearchService
CompanyMetricsService
CompanyFinancialDataRefresher
PortfolioService
PortfolioValueService
TransactionService
TransactionHistoryService
MarketPriceService
InvestorProvisioningService
```

Esta capa coordina reglas, repositorios, integraciones y operaciones de negocio.

### Ejemplo conceptual

Para obtener un portfolio:

```text
HTTP request
  → PortfolioController
    → PortfolioService
      → PortfolioRepository / PositionRepository
        → DB
```

Para comprar acciones:

```text
HTTP request
  → TransactionController
    → TransactionService
      → valida reglas
      → actualiza posiciones
      → persiste transacción
```

### Decisión tomada

Se decidió no poner lógica de negocio en los controllers. Los controllers solo reciben HTTP, validan entrada básica y delegan al servicio.

### Ventajas

- Los casos de uso se pueden testear sin levantar toda la web.
- Los controllers quedan chicos y fáciles de leer.
- La lógica no queda atada a HTTP.
- Permite reutilizar servicios desde schedulers, jobs o futuros adaptadores.

---

## 4.3 Puertos internos en la API

En varios lugares se usan interfaces dentro de `application`:

```text
CompanyStore
FinancialStatementStore
SecFilingStore
EdgarClient
MarketDataClient
```

Estas interfaces funcionan como **puertos**.

Por ejemplo:

```text
CompanyStore                # contrato que necesita application
CompanyRepository           # implementación con Spring Data JPA
```

```text
EdgarClient                 # contrato para obtener datos de EDGAR
EdgarHttpClient             # implementación HTTP real
RateLimitedEdgarClient      # decorador con rate limit
```

```text
MarketDataClient            # contrato para precios de mercado
YahooFinanceClient          # implementación contra Yahoo Finance
```

### Decisión tomada

Se decidió que la capa de aplicación dependa de interfaces, no siempre de implementaciones concretas.

### Ventajas

- Se puede testear con fakes sin pegarle a la DB o a APIs externas.
- Se puede cambiar Yahoo Finance por otro proveedor sin reescribir todo el caso de uso.
- Se puede envolver EDGAR con rate limit sin cambiar los servicios consumidores.
- Se reduce el acoplamiento entre negocio e infraestructura.

---

## 4.4 Capa `infrastructure`

La capa `infrastructure` contiene detalles técnicos.

En la API aparecen principalmente dos tipos:

```text
infrastructure/web          # Controllers REST
infrastructure/persistence  # Repositories JPA
infrastructure/client       # Clientes HTTP externos
infrastructure/scheduler    # Tareas programadas
infrastructure/filter       # Filtros de seguridad/provisioning
```

### Web adapters

Ejemplos:

```text
CompanyController
PortfolioController
PortfolioValueController
TransactionController
```

Estos exponen endpoints como:

```text
GET    /companies/search?q=...
GET    /companies/{cik}/metrics
GET    /portfolio
POST   /portfolio/positions
PUT    /portfolio/positions/{positionId}
DELETE /portfolio/positions/{positionId}
GET    /portfolio/value
POST   /portfolio/transactions/buy
POST   /portfolio/transactions/sell
GET    /portfolio/transactions
```

### Persistence adapters

Ejemplos:

```text
CompanyRepository
FinancialStatementRepository
SecFilingRepository
PortfolioRepository
PositionRepository
MarketPriceRepository
TransactionRepository
InvestorRepository
```

Usan Spring Data JPA para acceder a PostgreSQL.

### External clients

Ejemplos:

```text
EdgarHttpClient
RateLimitedEdgarClient
YahooFinanceClient
```

Sirven para consumir fuentes externas:

- EDGAR / SEC para información financiera y filings.
- Yahoo Finance para precios de mercado.

### Scheduler

`MarketPriceScheduler` automatiza la obtención de precios de mercado para símbolos relevantes.

### Ventajas

- Los detalles externos quedan aislados.
- La lógica de negocio no necesita saber cómo se arma un request HTTP a EDGAR.
- La persistencia se puede reemplazar o testear con fakes.
- La API queda preparada para nuevos adaptadores: colas, jobs, otros proveedores de datos, etc.

---

## 5. Seguridad y autenticación en la API

La API usa **Spring Security** como **OAuth2 Resource Server** con JWT.

La idea es que el frontend obtenga un token de Auth0 y lo envíe al backend como Bearer Token.

```text
User
  → Web/Mobile
    → Auth0 login
      → access token
        → API con Authorization: Bearer <token>
          → Spring Security valida JWT
```

Además existe un `InvestorProvisioningFilter` que toma la identidad autenticada y asegura que exista un inversor interno asociado.

### Decisión tomada

Se separa identidad externa de usuario interno:

- Auth0 maneja login, registro, sesión y tokens.
- La API maneja el concepto propio de `Investor`.

### Ventajas

- No se implementa autenticación manual insegura.
- Se delega login/registro a un proveedor especializado.
- El dominio conserva su propia entidad `Investor`.
- Se puede cambiar lógica interna del inversor sin cambiar Auth0.
- Permite proteger endpoints por JWT.

---

## 6. Persistencia y migraciones

La API usa:

- PostgreSQL como base de datos.
- Spring Data JPA como acceso a datos.
- Flyway para versionar migraciones.

### Decisión tomada

Se eligió una DB relacional porque el dominio financiero tiene entidades fuertemente relacionadas:

```text
Investor → Portfolio → Positions
Investor → Transactions
Company → FinancialStatements
Company → SecFilings
MarketPrice → Symbol + fetchedAt
```

### Ventajas

- Integridad referencial.
- Queries consistentes.
- Buen soporte para transacciones.
- Migraciones reproducibles en entornos locales, CI y despliegue.
- Facilita tests de integración con Testcontainers.

---

## 7. Integraciones externas: EDGAR y Yahoo Finance

La API integra dos fuentes externas:

```text
EDGAR / SEC      → compañías, facts financieros, filings
Yahoo Finance    → precios de mercado
```

### EDGAR

Componentes relevantes:

```text
EdgarClient
EdgarHttpClient
RateLimitedEdgarClient
EdgarCompanyFactsParser
EdgarCompanyFilingsParser
SlidingWindowRateLimiter
```

Se observa una decisión importante: **se respeta rate limiting** mediante un decorador (`RateLimitedEdgarClient`) y un rate limiter (`SlidingWindowRateLimiter`).

### Yahoo Finance

Componentes relevantes:

```text
MarketDataClient
YahooFinanceClient
MarketPriceService
MarketPriceScheduler
MarketPriceRepository
```

El scheduler permite actualizar precios de símbolos relevantes sin depender de una acción manual del usuario.

### Ventajas

- La lógica de negocio no queda atada al formato externo.
- Se pueden parsear respuestas externas y convertirlas a modelos propios.
- Se evitan bloqueos o abusos contra EDGAR mediante rate limit.
- Se puede cambiar proveedor externo manteniendo el contrato interno.

---

## 8. Manejo de errores

La API tiene una capa compartida de errores:

```text
shared/exceptions/
├── ApiErrorResponse.java
├── GlobalExceptionHandler.java
├── EdgarRateLimitException.java
└── MarketPriceFetchException.java
```

Además hay excepciones específicas por dominio:

```text
CompanyNotFoundException
PositionNotFoundException
BusinessRuleException
```

### Decisión tomada

Se centraliza el manejo de errores con `@RestControllerAdvice`.

### Ventajas

- Las respuestas de error son más consistentes.
- Los controllers no tienen que repetir `try/catch`.
- Se separa el error de negocio de la representación HTTP.
- Facilita tests de casos negativos.

---

## 9. Testing y calidad en la API

La API tiene una inversión fuerte en calidad:

- JUnit 5.
- Spring Boot Test.
- Spring Security Test.
- Testcontainers con PostgreSQL.
- JaCoCo con cobertura mínima del 75%.
- Spotless.
- Checkstyle.
- PMD.
- SpotBugs.
- SonarCloud.

### Tipos de tests observados

```text
Unit tests de servicios
Tests de dominio
WebMvcTest de controllers
Tests de parsers EDGAR
Tests de rate limiter
Tests de seguridad
Integration tests con PostgreSQL/Testcontainers
```

### Decisión tomada

No se confía solo en tests manuales. El proyecto fuerza calidad desde Gradle y CI.

### Ventajas

- Detecta regresiones temprano.
- Evita estilos inconsistentes.
- Obliga a mantener cobertura mínima.
- Mejora legibilidad.
- Hace más seguro trabajar en paralelo.

---

## 10. Arquitectura de `apps/`

La carpeta `apps/` contiene el frontend del sistema. Está organizada como workspace pnpm:

```text
apps/
├── package.json
├── pnpm-workspace.yaml
├── ui-core/
├── web/
└── mobile/
```

La arquitectura de frontend se basa en tres decisiones:

1. Separar apps deployables (`web`, `mobile`).
2. Extraer lógica compartida a `ui-core`.
3. Usar contratos/puertos para auth y API.

---

## 11. Arquitectura de `ui-core`

`ui-core` es la pieza más importante del frontend. Funciona como una librería compartida consumida por `web` y `mobile`.

Contiene principalmente:

```text
ui-core/src/
├── api/
│   ├── HttpApiAdapter.ts
│   ├── PortfolioPort.ts
│   ├── CompanyPort.ts
│   ├── TradingPort.ts
│   ├── PortfolioProvider.tsx
│   ├── CompanyProvider.tsx
│   ├── TradingProvider.tsx
│   ├── usePortfolio.ts
│   ├── useCompany.ts
│   └── useTrading.ts
└── auth/
    ├── AuthPort.ts
    ├── Auth0Adapter.ts
    ├── AuthProvider.tsx
    ├── TokenStore.ts
    ├── LocalStorageTokenStore.ts
    ├── InMemoryTokenStore.ts
    └── useAuth.ts
```

### 11.1 Puertos de frontend

Los archivos `PortfolioPort`, `CompanyPort`, `TradingPort` y `AuthPort` definen contratos.

Ejemplo conceptual:

```ts
export interface PortfolioPort {
  fetchPortfolio(): Promise<Portfolio>
  getPortfolioTotalValue(): Promise<PortfolioValue>
  addPosition(input: AddPositionInput): Promise<Position>
  modifyPosition(positionId: string, input: ModifyPositionInput): Promise<Position>
  removePosition(positionId: string): Promise<void>
}
```

Esto significa que las pantallas no necesitan saber si los datos vienen de HTTP, mocks, memoria o tests. Solo necesitan un objeto que cumpla el contrato.

### Decisión tomada

Se aplicó un patrón parecido a **Ports and Adapters** también del lado frontend.

```text
Pantalla React
  → Hook
    → Context Provider
      → Port interface
        → HttpApiAdapter real
```

### Ventajas

- Las pantallas son más testeables.
- Se pueden inyectar mocks para E2E o desarrollo local.
- La lógica HTTP queda centralizada.
- Web y mobile usan el mismo cliente.
- Cambiar endpoints impacta en un lugar principal: `HttpApiAdapter`.

---

## 12. `HttpApiAdapter`: adaptador HTTP compartido

`HttpApiAdapter` implementa varios puertos:

```text
PortfolioPort
CompanyPort
TradingPort
```

Responsabilidades:

- Construir requests HTTP.
- Adjuntar token Bearer.
- Serializar bodies JSON.
- Parsear respuestas.
- Convertir errores en `ApiError`.
- Exponer métodos de alto nivel como `fetchPortfolio`, `addPosition`, `searchCompanies`, `buyShares`, etc.

### Decisión tomada

Se decidió no hacer `fetch` directamente desde cada pantalla.

### Ventajas

- Evita duplicar headers, manejo de errores y base URL.
- Centraliza el contrato con la API.
- Facilita mantener web y mobile sincronizados.
- Reduce bugs por endpoints escritos a mano en múltiples lugares.

---

## 13. Arquitectura de autenticación en frontend

`ui-core` define un puerto:

```text
AuthPort
```

Y una implementación real:

```text
Auth0Adapter
```

También define stores de token:

```text
LocalStorageTokenStore
InMemoryTokenStore
```

### Flujo

```text
Web/Mobile
  → AuthProvider
    → AuthPort
      → Auth0Adapter
        → Auth0 SPA SDK
          → token guardado en TokenStore
```

### Decisión tomada

La app no depende directamente de Auth0 en todas sus pantallas. Depende de `AuthPort`.

### Ventajas

- Se puede mockear auth en tests o desarrollo.
- Se evita distribuir lógica de Auth0 por toda la app.
- Si mañana se cambia Auth0 por Firebase/Auth propio, el impacto queda acotado.
- Mobile puede usar variantes específicas de auth sin modificar `ui-core` entero.

---

## 14. Arquitectura de `apps/web`

La web usa:

- React.
- Vite.
- TypeScript.
- React Router.
- Tailwind/shadcn-like components.
- Cypress para E2E.
- `@ssv/ui-core` como dependencia workspace.

### Estructura

```text
apps/web/src/
├── App.tsx
├── components/
│   ├── AuthGuard.tsx
│   ├── PositionRow.tsx
│   └── ui/
├── pages/
│   ├── LoginPage.tsx
│   ├── RegisterPage.tsx
│   ├── CallbackPage.tsx
│   ├── PortfolioPage.tsx
│   ├── CompanySearchPage.tsx
│   └── TradingPage.tsx
└── lib/
```

### Flujo de composición

En `App.tsx`, la web arma los adaptadores concretos:

```text
Auth0Adapter o MockAuth
HttpApiAdapter
PortfolioProvider
CompanyProvider
TradingProvider
Router
Pages
```

Conceptualmente:

```text
App.tsx
  → crea authAdapter
  → crea apiAdapter
  → inyecta providers
  → define rutas
```

### Rutas principales

```text
/login
/register
/auth/callback
/portfolio
/companies
/trading
```

### AuthGuard

La web protege rutas sensibles con `AuthGuard`.

```text
Usuario no autenticado → login
Usuario autenticado    → pantalla protegida
```

### Decisión tomada

La web se enfoca en routing, layout y pantallas. No contiene la lógica central de API/auth; esa vive en `ui-core`.

### Ventajas

- La app web queda más chica.
- Las páginas se concentran en experiencia de usuario.
- La lógica compartida no se duplica con mobile.
- Se puede testear E2E con Cypress sobre flujos reales.

---

## 15. Arquitectura de `apps/mobile`

La mobile usa:

- React.
- Vite.
- TypeScript.
- Capacitor para empaquetar como app Android.
- Appium + uiautomator2 para E2E.
- `@ssv/ui-core` como dependencia workspace.

### Estructura

```text
apps/mobile/src/
├── App.tsx
├── auth/
│   ├── AuthCallbackHandler.tsx
│   └── CreateMobileAuth.tsx
└── screens/
    ├── LoginScreen.tsx
    ├── RegisterAccountScreen.tsx
    └── HomeScreen.tsx
```

### Decisión tomada

Mobile no se desarrolló como app nativa pura. Se desarrolló como app React/Vite empaquetada con Capacitor.

### Por qué se tomó esta decisión

Porque el equipo ya puede reutilizar gran parte del stack web:

- React.
- TypeScript.
- Vite.
- `ui-core`.
- Auth compartida.
- API client compartido.

Capacitor permite llevar esa app al mundo Android sin reescribir todo en Kotlin/Java.

### Ventajas

- Reutilización fuerte de código.
- Menor tiempo de desarrollo.
- Una misma lógica de negocio frontend para web y mobile.
- Posibilidad de generar APK.
- E2E con Appium sobre un entorno parecido al real.

### Mock auth en mobile

`CreateMobileAuth.tsx` permite usar `VITE_USE_MOCK_AUTH=true`.

Esto crea un `MockAuthAdapter` que guarda un token mock en `localStorage`.

### Por qué es útil

- Permite probar flujos sin depender de Auth0.
- Facilita E2E con Appium.
- Evita bloquear al equipo si faltan credenciales.
- Permite validar navegación y pantallas antes de cerrar integración real.

---

## 16. Desarrollo de apps: cómo se construyeron

El desarrollo de apps siguió una lógica incremental:

### 16.1 Primero: contratos compartidos

Se definieron interfaces en `ui-core`:

```text
AuthPort
PortfolioPort
CompanyPort
TradingPort
```

Esto permitió que web y mobile programen contra contratos estables.

### 16.2 Segundo: adaptadores reales

Se implementaron adaptadores:

```text
Auth0Adapter
HttpApiAdapter
LocalStorageTokenStore
```

Estos conectan los contratos con infraestructura real.

### 16.3 Tercero: providers y hooks

Se crearon providers y hooks:

```text
AuthProvider + useAuth
PortfolioProvider + usePortfolio
CompanyProvider + useCompany
TradingProvider + useTrading
```

Esto permite consumir la lógica desde React sin pasar dependencias manualmente por props en toda la app.

### 16.4 Cuarto: pantallas web

La web agregó pantallas para:

- Login.
- Registro.
- Portfolio.
- Búsqueda de compañías.
- Trading.
- Callback de Auth0.

### 16.5 Quinto: pantallas mobile

Mobile agregó pantallas para:

- Login.
- Registro.
- Home.
- Callback auth.

Y se preparó para Android mediante Capacitor.

### 16.6 Sexto: testing y E2E

Web usa Cypress.
Mobile usa Appium con uiautomator2.

La idea es validar flujos reales desde el punto de vista del usuario.

---

## 17. CI/CD y estrategia de calidad

El repositorio tiene workflows de GitHub Actions para:

- Detectar módulos cambiados.
- Ejecutar lint/test por módulo.
- Construir `ui-core` antes de web/mobile.
- Construir Docker images.
- Ejecutar Cypress E2E para web.
- Ejecutar Appium E2E para mobile.
- Publicar releases con semantic-release.
- Validar títulos de PR con Conventional Commits.

### Decisión tomada

El pipeline está pensado por fases:

```text
Detect modules
  → Lint + unit tests
    → Build + Sonar/Docker
      → E2E web/mobile
        → Release / cleanup
```

### Ventajas

- No se ejecuta todo innecesariamente si solo cambia un módulo.
- Web y mobile esperan a que `ui-core` compile.
- Se validan Dockerfiles en CI.
- Se prueban flujos completos antes de release.
- Se ordena el trabajo del equipo mediante PRs semánticos.

---

## 18. Decisiones arquitectónicas principales y justificación

| Decisión | Por qué se tomó | Ventaja principal |
|---|---|---|
| Monorepo | Backend, web, mobile y core evolucionan juntos | Coordinación simple y contratos versionados en un mismo repo |
| Backend modular por dominio | El negocio financiero se divide naturalmente en capacidades | Código más mantenible y fácil de ubicar |
| Capas `domain/application/infrastructure` | Separar negocio, casos de uso y tecnología | Mejor testabilidad y menor acoplamiento |
| Puertos en backend | No atar servicios a proveedores concretos | Fakes en tests y reemplazo de integraciones |
| Spring Boot + JPA + PostgreSQL | Stack robusto para API transaccional | Productividad, seguridad y persistencia confiable |
| Flyway | Versionar schema de DB | Entornos reproducibles |
| Auth0 + Resource Server JWT | Delegar login y validar tokens | Seguridad sin implementar auth casera |
| `ui-core` | Compartir lógica entre web y mobile | Menos duplicación y contratos consistentes |
| Puertos en frontend | Desacoplar pantallas de HTTP/Auth0 | Tests y mocks más simples |
| Vite + React | Desarrollo rápido y moderno | Build simple y buena DX |
| Capacitor | Reutilizar web stack para Android | Mobile más rápido sin reescribir nativo |
| Cypress + Appium | Validar flujos reales | Más confianza end-to-end |
| Docker + Compose | Ejecutar servicios local/CI | Entornos más previsibles |

---

## 19. Fortalezas del diseño actual

1. **Separación clara de responsabilidades**  
   API, web, mobile y core tienen límites claros.

2. **Buena base para trabajo en equipo**  
   La arquitectura permite dividir tareas por módulo.

3. **Testabilidad**  
   El uso de puertos, servicios y fakes facilita pruebas.

4. **Reutilización frontend**  
   `ui-core` evita duplicar lógica entre web y mobile.

5. **Integraciones externas encapsuladas**  
   EDGAR y Yahoo Finance no contaminan todo el dominio.

6. **Seguridad razonable**  
   Auth0 + JWT + Spring Security es una base sólida.

7. **Calidad automatizada**  
   Lint, test, coverage, static analysis, Docker build y E2E.

---

## 20. Puntos a mejorar o cuidar

### 20.1 Hexagonal no completamente pura

Las entidades de dominio están anotadas con JPA. Para el alcance actual está bien, pero si se quiere una arquitectura más limpia habría que separar dominio puro de persistencia.

### 20.2 Repositorios como puertos parciales

Algunos repositorios implementan interfaces de aplicación (`CompanyStore`), lo cual está bien. Pero otros servicios parecen depender directamente de repositorios JPA. Conviene unificar el criterio si se quiere máxima consistencia.

### 20.3 `ui-core` podría crecer demasiado

Si `ui-core` empieza a contener demasiada lógica, puede convertirse en un módulo difícil de mantener. Conviene separarlo internamente por feature:

```text
ui-core/src/features/portfolio
ui-core/src/features/company
ui-core/src/features/trading
ui-core/src/auth
```

### 20.4 Mobile todavía es más simple que web

Mobile tiene menos pantallas y lógica que web. Está bien como primera iteración, pero si crece mucho puede necesitar routing propio, manejo de estado más formal y componentes mobile-specific.

### 20.5 Cuidado con mocks en producción

Los mocks de auth son útiles para desarrollo y E2E, pero deben estar controlados por variables de entorno y nunca habilitarse accidentalmente en producción.

---

## 21. Conclusión

FinancialApp usa una arquitectura bastante sólida para un proyecto académico/productivo: backend modular, capas separadas, contratos internos, integración con proveedores externos, frontend compartido y pipelines de calidad.

La decisión más importante del backend fue organizar por dominios y separar `domain`, `application` e `infrastructure`. Esto permite que las reglas financieras vivan en servicios claros y que los detalles como REST, JPA, EDGAR o Yahoo Finance queden en adaptadores.

La decisión más importante del frontend fue crear `apps/ui-core`. Gracias a eso, web y mobile no duplican autenticación, cliente HTTP ni contratos de API. Web se concentra en rutas y páginas; mobile se concentra en pantallas y empaquetado Android con Capacitor.

En conjunto, la arquitectura trae estas ventajas principales:

- Escala mejor que un MVC plano.
- Permite trabajo paralelo.
- Reduce duplicación entre web y mobile.
- Mejora testabilidad.
- Aísla integraciones externas.
- Facilita CI/CD.
- Deja una base preparada para sumar nuevas features como watchlist, analytics, alertas o comparaciones financieras.

La mejora más importante a futuro sería decidir si se quiere mantener esta arquitectura como **hexagonal pragmática** o avanzar hacia una **hexagonal más estricta**, separando completamente dominio, persistencia y DTOs.
