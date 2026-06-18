/// <reference types="@wdio/types" />

import { resolve } from 'node:path'
import 'dotenv/config'

console.log('AUTH0_TEST_REALM:', process.env.AUTH0_TEST_REALM)

type AndroidAppiumCapability = WebdriverIO.Capabilities & {
  platformName: 'Android'
  'appium:automationName': 'UiAutomator2'
  'appium:deviceName': string
  'appium:udid': string
  'appium:app': string
  'appium:noReset': boolean
  'appium:chromedriverAutodownload': boolean
  'appium:adbExecTimeout': number
  'appium:androidInstallTimeout': number
  'appium:uiautomator2ServerInstallTimeout': number
  'appium:uiautomator2ServerLaunchTimeout': number
  'appium:settingsAppLaunchTimeout': number
}

const androidCapability: AndroidAppiumCapability = {
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
}

export const config: WebdriverIO.Config = {
  runner: 'local',
  specs: ['./specs/**/*.spec.ts'],
  maxInstances: 1,
  capabilities: [androidCapability],
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