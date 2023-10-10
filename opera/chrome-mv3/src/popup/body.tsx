import React, { useEffect } from 'react';

import { getCurrentStateText } from '../domain/utils';
import { BlockingInfo } from '../domain/types';

type BodyProps = {
  isBlockingEnabled: boolean;
  blockingInfo: BlockingInfo;
  updateBlockingInfo: () => void;
};

const Body: React.FC<BodyProps> = ({
  isBlockingEnabled,
  blockingInfo,
  updateBlockingInfo,
}) => {
  useEffect(() => {
    updateBlockingInfo();
  }, [isBlockingEnabled]);

  return (
    <div className="body">
      <p id="blockedInfo">
        {getBlockedInfoText(isBlockingEnabled, blockingInfo.matchedRules)}
      </p>
      {isBlockingEnabled && (
        <p id="blockedDetails">{getHost(blockingInfo.activeTabUrl)}</p>
      )}
    </div>
  );
};

export default Body;

const getBlockedInfoText = (
  isBlockingEnabled: boolean,
  matchedRules: number,
) => {
  if (!isBlockingEnabled) {
    return getCurrentStateText(false);
  }

  return matchedRules > 0 ? `Blocked: ${matchedRules}` : 'No matched rules';
};

const getHost = (url: string) => {
  let host: string = '';

  try {
    const parsedUrl = new URL(url);
    host = parsedUrl.hostname;

    // Check if the host starts with "www." and remove it if present
    if (host.startsWith('www.')) {
      host = host.substring(4);
    }
  } catch (error) {
    // If an error occurs during URL parsing, host remains undefined
  } finally {
    // Set host to 'this tab' if it's empty
    host = host || 'this tab';
  }

  return 'for ' + host;
};
