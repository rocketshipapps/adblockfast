import React, { useEffect } from "react";

import { getCurrentStateText } from "../domain/utils";
import { BlockingInfo } from "../domain/types";

type BodyProps = {
  isBlockingEnabled: boolean;
  blockingInfo: BlockingInfo;
  updateBlockingInfo: () => void;
};

const Body: React.FC<BodyProps> = ({ isBlockingEnabled, blockingInfo, updateBlockingInfo }) => {
  useEffect(() => {
    updateBlockingInfo();
  }, [isBlockingEnabled]);

  const active = true;

  const buttonStyle = {
    backgroundColor: active ? "#f5f6f8" : "#4884ea",
    color: active ? "#000" : "#fff",
  };

  return (
    <div className="body">
      <img src={active ? "img/enabled.svg" : "img/disabled.svg"}></img>
      <button id="downloadButton" style={buttonStyle}>
        {active ? `Blocked: ${blockingInfo.matchedRules}` : "Download Desktop App"}
      </button>
      <p id="blockedDetails">
        {active ? getHost(blockingInfo.activeTabUrl) : "to enable ad blocking"}
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
