/**
 * Checks for updates using the Tauri updater plugin.
 * Call on app startup — works only inside Tauri webview.
 *
 * ponytail: one-shot check on launch. No background polling.
 */
export async function checkForUpdate(): Promise<{ available: boolean; version?: string; body?: string } | null> {
  // Not in Tauri context — skip
  if (!window.__TAURI_INTERNALS__) return null

  try {
    const { check } = await import('@tauri-apps/plugin-updater')
    const { relaunch } = await import('@tauri-apps/plugin-process')

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
    return null
  }
}
