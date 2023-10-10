import { APP_NAME } from './constants';

const getCurrentStateText = (isEnabled) => {
  return `${APP_NAME} is ${isEnabled ? 'ON' : 'OFF'}`;
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

export { getCurrentStateText, getActiveTab, getActiveTabId };
