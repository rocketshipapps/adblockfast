import { Settings } from "./domain/settings";
import { pingNativeApp } from "./domain/native";
import { Whitelist } from "./domain/whitelist";
import { getHost } from "./domain/utils";

chrome.runtime.onInstalled.addListener(async () => {
  const enabledRulesets = await chrome.declarativeNetRequest.getEnabledRulesets();
  console.log(`Enabled rulesets: ${enabledRulesets}`);

  const staticRuleCount = await chrome.declarativeNetRequest.getAvailableStaticRuleCount();
  console.log(`Available static rule count: ${staticRuleCount}`); // TODO: 329,996 rules?

  Settings.getInstance().init();
});

chrome.declarativeNetRequest.setExtensionActionOptions({
  displayActionCountAsBadgeText: true,
});

chrome.tabs.onUpdated.addListener((updatedTabId, changeInfo, tab) => {
  if (changeInfo.status == "complete") {
    const getIconPaths = (isBlockingEnabled: boolean) => {
      const iconPath = isBlockingEnabled ? "blocked-ads" : "blocked";

      return {
        19: `img/${iconPath}/19.png`,
        38: `img/${iconPath}/38.png`,
      };
    };

    Settings.getInstance()
      .isBlockingEnabled()
      .then((isBlockingEnabled: boolean) => {
        chrome.action.setIcon({
          path: getIconPaths(isBlockingEnabled),
          tabId: updatedTabId,
        });
      });
  }
});

const PING_INTERVAL = 5000;
setInterval(pingNativeApp, PING_INTERVAL);
