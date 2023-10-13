import React, { useEffect } from "react";

import { APP_URL } from "../domain/constants";
import { BlockingInfo } from "../domain/types";
import { Settings } from "../domain/settings";
import { NativeAppStatus } from "../domain/native";

type BodyProps = {
  isBlockingEnabled: boolean;
  blockingInfo: BlockingInfo;
  updateBlockingInfo: () => void;
};

const Body: React.FC<BodyProps> = ({ isBlockingEnabled, blockingInfo, updateBlockingInfo }) => {
  const [nativeAppStatus, setNativeAppStatus] = React.useState<NativeAppStatus>(
    NativeAppStatus.NoApp
  );

  useEffect(() => {
    const getNativeAppStatus = async () => {
      let settings = Settings.getInstance();
      setNativeAppStatus(await settings.getNativeAppStatus());
    };

    getNativeAppStatus();
    updateBlockingInfo();
  }, [isBlockingEnabled]);

  const handleDownloadClick = () => {
    chrome.tabs.create({ url: APP_URL });
  };

  return (
    <div className="body">
      <img src={getImage(nativeAppStatus, isBlockingEnabled)}></img>

      {nativeAppStatus === NativeAppStatus.NoApp ? (
        <button id="downloadButton" onClick={handleDownloadClick}>
          Download Desktop App
        </button>
      ) : (
        <button id="infoLabel">Blocked: {blockingInfo.matchedRules}</button>
      )}

      <div id="detailsLabel">
        {nativeAppStatus === NativeAppStatus.NoApp
          ? "to enable ad blocking"
          : getHost(blockingInfo.activeTabUrl)}
      </div>
    </div>
  );
};

export default Body;

const getImage = (nativeAppStatus: NativeAppStatus, isBlockingEnabled: boolean): string => {
  const filename =
    nativeAppStatus === NativeAppStatus.Active && isBlockingEnabled ? "enabled" : "disabled";
  return `img/${filename}.svg`;
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
