/**
 * Checks for updates using the Tauri updater plugin.
 * Call this on app startup to notify the user of available updates.
 *
 * ponytail: simple fire-and-forget check. No background polling.
 * Upgrade: add periodic check + silent download if needed.
 */
export async function checkForUpdate(): Promise<{ available: boolean; version?: string; body?: string } | null> {
  try {
    const { check } = await import('@tauri-apps/plugin-updater')
    const { relaunch } = await import('@tauri-apps/api/process')

    const update = await check()
    if (!update?.available) return { available: false }

    const confirmed = window.confirm(
      `Update ${update.version} available!\n${update.body ?? ''}\n\nDownload and install now?`
    )
    if (!confirmed) return { available: true, version: update.version }

    await update.downloadAndInstall()
    await relaunch()
    return { available: true, version: update.version }
  } catch {
    // Not running in Tauri (browser dev) or updater not configured
    return null
  }
}
