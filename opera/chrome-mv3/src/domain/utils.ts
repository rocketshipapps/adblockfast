import { APP_NAME } from "./constants";

const getCurrentStateText = (isEnabled) => {
  return `${APP_NAME} is ${isEnabled ? "ON" : "OFF"}`;
};

const getHost = (url: string) => {
  let host: string = "";

  try {
    const parsedUrl = new URL(url);
    host = parsedUrl.hostname;

    // Check if the host starts with "www." and remove it if present
    if (host.startsWith("www.")) {
      host = host.substring(4);
    }
  } catch (error) {
    // If an error occurs during URL parsing, host remains undefined
  } finally {
    host = host || "for this tab";
  }

  return host;
};

async function getActiveTab(): Promise<chrome.tabs.Tab | null> {
  return new Promise((resolve, reject) => {
    chrome.tabs.query({ active: true, currentWindow: true }, (tabs) => {
      if (chrome.runtime.lastError) {
        reject(chrome.runtime.lastError);
      } else if (tabs.length > 0) {
        resolve(tabs[0]);
      } else {
        resolve(null);
      }
    });
  });
}

async function getActiveTabId(): Promise<number | null> {
  const activeTab = await getActiveTab();
  return activeTab ? activeTab.id : null;
}

export { getCurrentStateText, getHost, getActiveTab, getActiveTabId };
