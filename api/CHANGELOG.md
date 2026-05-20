## 1.0.0 (2026-05-20)

### Features

* add docker-compose fragments and wire E2E pipeline ([cd83ce2](https://github.com/SSV-AseCa/FinancialApp/commit/cd83ce2556c628e4a32ec5256aaef4e77b2860aa))
* set up pnpm workspace with shared ui-core for web and mobile ([583f1f4](https://github.com/SSV-AseCa/FinancialApp/commit/583f1f421550a0d9e3d051c7b5447af3c360218f))

### Bug Fixes

* add .dockerignore, fix nginx healthcheck, fix teardown paths, and containerize Gradle APK build with JDK 21 ([258c5d4](https://github.com/SSV-AseCa/FinancialApp/commit/258c5d4e8512efb9c3fb41121af300e93eb48af5))
* checkout before referencing local composite actions ([2e15723](https://github.com/SSV-AseCa/FinancialApp/commit/2e157230f6984287a376168c82badc9fdbd7ee10))
* extract APK from emulator container onto runner before Appium tests ([f6fe614](https://github.com/SSV-AseCa/FinancialApp/commit/f6fe614c038d8507cea060adeba3f52cc792591e))
* install adb on runner before connecting to Android emulator ([dc71fc6](https://github.com/SSV-AseCa/FinancialApp/commit/dc71fc6387c3aa3d12c806075653ec3b91fa046a))
* migrate all workflow pnpm cache paths to root workspace lockfile ([7ad80cc](https://github.com/SSV-AseCa/FinancialApp/commit/7ad80cc301b0137cad2ce2b2dd55f9cc68bc34f0))
* pin adb and Appium to emulator serial, install Cypress binary explicitly ([e05a297](https://github.com/SSV-AseCa/FinancialApp/commit/e05a297ef8898a0b8f09e2d450ce79472582fe73))
* replace nc healthchecks and wire emulator readiness in E2E ([4ef8dd2](https://github.com/SSV-AseCa/FinancialApp/commit/4ef8dd28882887559c48e1ef3f20e32d3407ecbf))
* resolve APK path relative to wdio config file using __dirname ([cd393b4](https://github.com/SSV-AseCa/FinancialApp/commit/cd393b4ae043dbdc254a7f607108078a832a7377))
* resolve E2E pipeline issues for web and mobile ([d1f65a9](https://github.com/SSV-AseCa/FinancialApp/commit/d1f65a9e1b9550302e0b887f92ceda11f0ce5faf))
* resolve E2E pipeline issues for web and mobile                      - Install from workspace root before Cypress action to avoid lockfile ([cfae001](https://github.com/SSV-AseCa/FinancialApp/commit/cfae00183a7f2a1551964e363bc18870d6b89d56))
