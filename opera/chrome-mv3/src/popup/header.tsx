import React from "react";

import { getCurrentStateText } from "../domain/utils";
import { Settings } from "../domain/settings";
import { getActiveTabId } from "../domain/utils";
import { NativeAppStatus } from "../domain/native";

type HeaderProps = {
  isBlockingEnabled: boolean;
  setBlockingEnabled: React.Dispatch<React.SetStateAction<boolean>>;
  updateBlockingInfo: () => void;
};

const Header: React.FC<HeaderProps> = ({
  isBlockingEnabled,
  setBlockingEnabled,
  updateBlockingInfo,
}) => {
  const handleSwitchChange = async () => {
    const settings = Settings.getInstance();
    const nativeAppStatus = await settings.getNativeAppStatus();

    if (nativeAppStatus === NativeAppStatus.NoApp) {
      // TODO: Show toast "Download, install, and launch desktop app to enable blocking"
      return;
    }

    if (nativeAppStatus === NativeAppStatus.Paused) {
      // TODO: Show toast "Activate desktop app to enable blocking"
      return;
    }

    const isEnabled: boolean = !isBlockingEnabled;

    setBlockingEnabled(isEnabled);

    settings.setBlockingEnabled(isEnabled);
    settings.updateRulesets(isEnabled, ["default"]);

    const currentTabId = await getActiveTabId();
    chrome.tabs.onUpdated.addListener((updatedTabId, changeInfo) => {
      if (updatedTabId === currentTabId && changeInfo.status === "complete") {
        updateBlockingInfo();
      }
    });

    chrome.tabs.reload(currentTabId);
  };

  return (
    <div className="header">
      <img className="logo" src="img/adblockfast.svg" alt="Logo" />
      <label className="switch">
        <input
          type="checkbox"
          id="toggleSwitch"
          checked={isBlockingEnabled}
          onChange={handleSwitchChange}
        />
        <span className="slider round"></span>
        <span className="tooltiptext" id="toggleStatus">
          {getCurrentStateText(isBlockingEnabled)}
        </span>
      </label>
    </div>
  );
};

export default Header;
