import { test, expect } from '@playwright/test'

test.describe('Finger Seal — Smoke Tests', () => {

  test('app loads and shows the dashboard', async ({ page }) => {
    await page.goto('/')
    await expect(page).toHaveTitle(/Finger Seal|finger seal/i)
    await expect(page.getByText(/dashboard|connections|sql/i).first()).toBeVisible()
  })

  test('sidebar navigation is present', async ({ page }) => {
    await page.goto('/')
    // The sidebar should contain navigation items
    const sidebar = page.locator('nav, aside, [class*="sidebar"], [class*="Sidebar"]').first()
    await expect(sidebar).toBeVisible()
  })

  test('clicking through views does not throw', async ({ page }) => {
    const errors: string[] = []
    page.on('pageerror', (err) => errors.push(err.message))

    await page.goto('/')
    await page.waitForLoadState('networkidle')

    // Click each sidebar link / button
    const links = page.locator('a, button, [role="button"]')
    const count = await links.count()
    // Click up to 10 interactive elements to smoke-test navigation
    for (let i = 0; i < Math.min(count, 10); i++) {
      const btn = links.nth(i)
      if (await btn.isVisible() && await btn.isEnabled()) {
        await btn.click()
        await page.waitForTimeout(300)
      }
    }

    expect(errors).toHaveLength(0)
  })

  test('theme and font assets load', async ({ page }) => {
    await page.goto('/')
    // Google Fonts — Inter and JetBrains Mono
    const fontsLoaded = await page.evaluate(() =>
      document.fonts.check('12px Inter') && document.fonts.check('12px "JetBrains Mono"')
    )
    expect(fontsLoaded).toBe(true)

    // Material Symbols should be in the page
    const symbols = page.locator('.material-symbols-outlined')
    await expect(symbols.first()).toBeVisible()
  })
})
