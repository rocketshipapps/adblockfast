import { NativeAppStatus } from "./native";

export interface StorageData {
  isBlockingEnabled: boolean;
  nativeAppStatus: NativeAppStatus;
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
    this.updateRulesets(true, ["default"]);
  }

  pause(): void {
    this.setBlockingEnabled(false);
    this.setNativeAppStatus(NativeAppStatus.Paused);
    this.updateRulesets(false, ["default"]);
  }

  disable(): void {
    this.setBlockingEnabled(false);
    this.setNativeAppStatus(NativeAppStatus.NoApp);
    this.updateRulesets(false, ["default"]);
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
}
