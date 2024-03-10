import { NativeAppStatus } from "./native";

export interface StorageData {
  isBlockingEnabled: boolean;
  nativeAppStatus: NativeAppStatus;
  whitelist: string[];
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

  activate(): void {
    this.setBlockingEnabled(true);
    this.setNativeAppStatus(NativeAppStatus.Active);
    this.updateRulesets(true);
  }

  pause(): void {
    this.setBlockingEnabled(false);
    this.setNativeAppStatus(NativeAppStatus.Paused);
    this.updateRulesets(false);
  }

  disable(): void {
    this.setBlockingEnabled(false);
    this.setNativeAppStatus(NativeAppStatus.NoApp);
    this.updateRulesets(false);
  }

  init(): void {
    this.disable();
    this.setWhitelist([]);
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

  updateRulesets(shouldEnable: boolean, ruleset: string[] = ["default"]): void {
    const rulesetOptions = {
      [shouldEnable ? "enableRulesetIds" : "disableRulesetIds"]: ruleset,
    };
    chrome.declarativeNetRequest.updateEnabledRulesets(rulesetOptions);
  }

  setNativeAppStatus(status: NativeAppStatus): Promise<void> {
    return new Promise<void>((resolve, reject) => {
      chrome.storage.sync.set({ nativeAppStatus: status }, () => {
        if (chrome.runtime.lastError) {
          reject(chrome.runtime.lastError);
        } else {
          resolve();
        }
      });
    });
  }

  getNativeAppStatus(): Promise<NativeAppStatus> {
    const keys: StorageKeys[] = ["nativeAppStatus"];

    return new Promise<NativeAppStatus>((resolve, reject) => {
      chrome.storage.sync.get(keys, (result: StorageData) => {
        if (chrome.runtime.lastError) {
          reject(chrome.runtime.lastError);
        } else {
          resolve(result.nativeAppStatus);
        }
      });
    });
  }

  setWhitelist(whitelist: string[]): Promise<void> {
    return new Promise<void>((resolve, reject) => {
      chrome.storage.sync.set({ whitelist: whitelist }, () => {
        if (chrome.runtime.lastError) {
          reject(chrome.runtime.lastError);
        } else {
          resolve();
        }
      });
    });
  }

  getWhitelist(): Promise<string[]> {
    const keys: StorageKeys[] = ["whitelist"];

    return new Promise<string[]>((resolve, reject) => {
      chrome.storage.sync.get(keys, (result) => {
        if (chrome.runtime.lastError) {
          reject(chrome.runtime.lastError);
        } else {
          resolve(result.whitelist || []);
        }
      });
    });
  }
}
