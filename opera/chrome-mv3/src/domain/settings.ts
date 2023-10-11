export interface StorageData {
  isBlockingEnabled: boolean;
  isNativeAppAvailable: boolean;
}

export type StorageKeys = keyof StorageData;

export class Settings {
  private static instance: Settings | null = null;

  private constructor() {}

  static getInstance(): Settings {
    if (!Settings.instance) {
      Settings.instance = new Settings();
    }
    return Settings.instance;
  }

  setBlockingEnabled(isEnabled: boolean): Promise<void> {
    return new Promise<void>((resolve, reject) => {
      chrome.storage.sync.set({ isBlockingEnabled: isEnabled }, () => {
        if (chrome.runtime.lastError) {
          reject(chrome.runtime.lastError);
        } else {
          resolve();
        }
      });
    });
  }

  isBlockingEnabled(): Promise<boolean> {
    const keys: StorageKeys[] = ["isBlockingEnabled"];

    return new Promise<boolean>((resolve, reject) => {
      chrome.storage.sync.get(keys, (result: StorageData) => {
        if (chrome.runtime.lastError) {
          reject(chrome.runtime.lastError);
        } else {
          resolve(result.isBlockingEnabled);
        }
      });
    });
  }

  updateRulesets(shouldEnable: boolean, ruleset: string[]): void {
    const rulesetOptions = {
      [shouldEnable ? "enableRulesetIds" : "disableRulesetIds"]: ruleset,
    };
    chrome.declarativeNetRequest.updateEnabledRulesets(rulesetOptions);
  }

  setNativeAppAvailable(isAvailable: boolean): Promise<void> {
    return new Promise<void>((resolve, reject) => {
      chrome.storage.sync.set({ isNativeAppAvailable: isAvailable }, () => {
        if (chrome.runtime.lastError) {
          reject(chrome.runtime.lastError);
        } else {
          resolve();
        }
      });
    });
  }

  isNativeAppAvailable(): Promise<boolean> {
    const keys: StorageKeys[] = ["isNativeAppAvailable"];

    return new Promise<boolean>((resolve, reject) => {
      chrome.storage.sync.get(keys, (result: StorageData) => {
        if (chrome.runtime.lastError) {
          reject(chrome.runtime.lastError);
        } else {
          resolve(result.isNativeAppAvailable);
        }
      });
    });
  }
}
