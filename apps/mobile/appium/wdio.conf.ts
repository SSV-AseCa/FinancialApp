import type { Options } from '@wdio/types'

export const config: Options.Testrunner = {
  runner: 'local',
  specs: ['./specs/**/*.spec.ts'],
  maxInstances: 1,
  capabilities: [
    {
      platformName: 'Android',
      'appium:automationName': 'UiAutomator2',
      // In CI the emulator container exposes ADB on the default port.
      // Locally, point this at your connected device or running emulator.
      'appium:deviceName': 'Android Emulator',
      'appium:udid': 'localhost:5555',
      'appium:app': './app-debug.apk',
      'appium:noReset': false,
    },
  ],
  logLevel: 'warn',
  waitforTimeout: 30000,
  connectionRetryTimeout: 120000,
  connectionRetryCount: 3,
  // Appium server is started externally (globally via `pnpm add -g appium`).
  // Set hostname/port if the server runs somewhere other than localhost:4723.
  hostname: '127.0.0.1',
  port: 4723,
  framework: 'mocha',
  reporters: ['spec'],
  mochaOpts: {
    ui: 'bdd',
    timeout: 60000,
  },
}
