import { Settings } from "./settings";

const NATIVE_APP_ADDRESS: string = "http://127.0.0.1:5578/heartbeat";

export enum NativeAppStatus {
  Active = "active",
  Paused = "paused",
  NoApp = "none",
}

const makeUrl = async (settings: Settings) => {
  const value = (await settings.isBlockingEnabled()) ? "true" : "false";
  return `${NATIVE_APP_ADDRESS}?is_blocking=${value}`;
};

export async function pingNativeApp() {
  let settings = Settings.getInstance();

  fetch(await makeUrl(settings))
    .then((response) => response.json())
    .then(async (data) => {
      const previousStatus: NativeAppStatus =
        await settings.getNativeAppStatus();
      const currentStatus: NativeAppStatus = data.status.toLowerCase();
      if (previousStatus !== currentStatus) {
        if (currentStatus === NativeAppStatus.Active) {
          settings.activate();
        } else {
          settings.pause();
        }
      }
    })
    .catch(async (_) => {
      const previousStatus: NativeAppStatus =
        await settings.getNativeAppStatus();
      if (previousStatus !== NativeAppStatus.NoApp) {
        settings.disable();
      }
    });
}
