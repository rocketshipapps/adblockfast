import { Settings } from "./settings";

const NATIVE_APP_ADDRESS: string = "http://127.0.0.1:5578/heartbeat";

export enum NativeAppStatus {
  Active = "active",
  Paused = "paused",
  NoApp = "none",
}

export function pingNativeApp() {
  let settings = Settings.getInstance();

  fetch(NATIVE_APP_ADDRESS)
    .then((response) => response.json())
    .then(async (data) => {
      const previousStatus: NativeAppStatus = await settings.getNativeAppStatus();
      const currentStatus: NativeAppStatus = data.status.toLowerCase();
      if (previousStatus !== currentStatus) {
        console.log(`Status changed to ${currentStatus}`);
        if (currentStatus === NativeAppStatus.Active) {
          settings.activate();
        } else {
          settings.pause();
        }
      }
    })
    .catch(async (error) => {
      const previousStatus: NativeAppStatus = await settings.getNativeAppStatus();
      if (previousStatus !== NativeAppStatus.NoApp) {
        console.error(`Ping to ${NATIVE_APP_ADDRESS} - Error: ${error.message}`);
        settings.disable();
      }
    });
}
