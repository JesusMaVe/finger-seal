import { invoke } from '@tauri-apps/api/core';

const KEYRING_SERVICE = 'finger-seal';

export async function savePassword(connectionId: number, password: string): Promise<void> {
  await invoke('save_credential', {
    service: KEYRING_SERVICE,
    key: String(connectionId),
    password,
  });
}

export async function getPassword(connectionId: number): Promise<string | null> {
  try {
    return await invoke<string>('get_credential', {
      service: KEYRING_SERVICE,
      key: String(connectionId),
    });
  } catch {
    return null;
  }
}

export async function deletePassword(connectionId: number): Promise<void> {
  await invoke('delete_credential', {
    service: KEYRING_SERVICE,
    key: String(connectionId),
  });
}
