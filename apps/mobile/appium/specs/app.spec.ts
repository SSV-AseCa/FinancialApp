describe('SSV App', () => {
  it('should display the Capacitor WebView', async () => {
    const webview = await $('android=new UiSelector().className("android.webkit.WebView")')
    await expect(webview).toBeDisplayed()
  })
})
