use tauri::command;
use keyring::Entry;

#[command]
pub fn save_credential(service: &str, key: &str, password: &str) -> Result<(), String> {
    let entry = Entry::new(service, key).map_err(|e| e.to_string())?;
    entry.set_password(password).map_err(|e| e.to_string())?;
    Ok(())
}

#[command]
pub fn get_credential(service: &str, key: &str) -> Result<String, String> {
    let entry = Entry::new(service, key).map_err(|e| e.to_string())?;
    let password = entry.get_password().map_err(|e| e.to_string())?;
    Ok(password)
}

#[command]
pub fn delete_credential(service: &str, key: &str) -> Result<(), String> {
    let entry = Entry::new(service, key).map_err(|e| e.to_string())?;
    entry.delete_credential().map_err(|e| e.to_string())?;
    Ok(())
}
