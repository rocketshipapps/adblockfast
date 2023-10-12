import React, { useEffect } from "react";

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

  const buttonStyle = {
    backgroundColor: nativeAppStatus === NativeAppStatus.NoApp ? "#4884ea" : "#f5f6f8",
    color: nativeAppStatus === NativeAppStatus.NoApp ? "#fff" : "#000",
  };

  return (
    <div className="body">
      <img
        src={nativeAppStatus === NativeAppStatus.Active ? "img/enabled.svg" : "img/disabled.svg"}
      ></img>
      <button id="downloadButton" style={buttonStyle}>
        {nativeAppStatus === NativeAppStatus.NoApp
          ? "Download Desktop App"
          : `Blocked: ${blockingInfo.matchedRules}`}
      </button>
      <p id="blockedDetails">
        {nativeAppStatus === NativeAppStatus.NoApp
          ? "to enable ad blocking"
          : getHost(blockingInfo.activeTabUrl)}
      </p>
    </div>
  );
};

export default Body;

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
