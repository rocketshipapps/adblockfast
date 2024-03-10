import React, { useEffect } from "react";

import { APP_URL } from "../domain/constants";
import { BlockingInfo } from "../domain/types";
import { Settings } from "../domain/settings";
import { NativeAppStatus } from "../domain/native";
import { Whitelist } from "../domain/whitelist";
import { getActiveTabId } from "../domain/utils";

type BodyProps = {
  isBlockingEnabled: boolean;
  blockingInfo: BlockingInfo;
  updateBlockingInfo: () => void;
};

const Body: React.FC<BodyProps> = ({ isBlockingEnabled, blockingInfo, updateBlockingInfo }) => {
  let whitelist: Whitelist = new Whitelist();

  const [nativeAppStatus, setNativeAppStatus] = React.useState<NativeAppStatus>(
    NativeAppStatus.NoApp
  );
  const [whitelistButtonText, setWhitelistButtonText] = React.useState<string>(
    getWhitelistButtonText(false)
  );

  useEffect(() => {
    const getNativeAppStatus = async () => {
      let settings = Settings.getInstance();
      setNativeAppStatus(await settings.getNativeAppStatus());
    };

    getNativeAppStatus();
    updateBlockingInfo();

    whitelist.isWhitelisted(blockingInfo.host).then((result) => {
      setWhitelistButtonText(getWhitelistButtonText(result));
    });
  }, [isBlockingEnabled]);

  const handleDownloadClick = () => {
    chrome.tabs.create({ url: APP_URL });
  };

  const handleWhitelistClick = async () => {
    const isWhitelisted = await whitelist.update(blockingInfo.host);
    setWhitelistButtonText(getWhitelistButtonText(isWhitelisted));
    chrome.tabs.reload(await getActiveTabId());
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

      <div className="row">
        <div id="detailsLabel">
          {nativeAppStatus === NativeAppStatus.NoApp ? "to enable ad blocking" : blockingInfo.host}
        </div>
        {nativeAppStatus === NativeAppStatus.Active && (
          <div id="whitelistButton" onClick={handleWhitelistClick}>
            {whitelistButtonText}
          </div>
        )}
      </div>
    </div>
  );
};

export default Body;

const getWhitelistButtonText = (isWhitelisted: boolean): string => {
  return isWhitelisted ? "Disable ads on this site" : "Enable ads on this site";
};

const getImage = (nativeAppStatus: NativeAppStatus, isBlockingEnabled: boolean): string => {
  const filename =
    nativeAppStatus === NativeAppStatus.Active && isBlockingEnabled ? "enabled" : "disabled";
  return `img/${filename}.svg`;
};
