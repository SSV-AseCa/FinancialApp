import { resolve } from 'node:path'

export const config = {
  runner: 'local',
  specs: ['./specs/**/*.spec.ts'],
  maxInstances: 1,
  capabilities: [
    {
      maxInstances: 1,
      platformName: 'Android',
      'appium:automationName': 'UiAutomator2',
      'appium:deviceName': 'Android Emulator',
      'appium:udid': process.env.APPIUM_UDID ?? 'emulator-5554',
      'appium:app': resolve(
          process.cwd(),
          'android/app/build/outputs/apk/debug/app-debug.apk',
      ),
      'appium:noReset': false,
      'appium:chromedriverAutodownload': true,
      'appium:adbExecTimeout': 180000,
      'appium:androidInstallTimeout': 180000,
      'appium:uiautomator2ServerInstallTimeout': 180000,
      'appium:uiautomator2ServerLaunchTimeout': 180000,
      'appium:settingsAppLaunchTimeout': 180000,
    },
  ],
  logLevel: 'warn',
  waitforTimeout: 30000,
  connectionRetryTimeout: 300000,
  connectionRetryCount: 3,
  hostname: '127.0.0.1',
  port: 4723,
  framework: 'mocha',
  reporters: ['spec'],
  mochaOpts: {
    ui: 'bdd',
    timeout: 300000,
  },
}