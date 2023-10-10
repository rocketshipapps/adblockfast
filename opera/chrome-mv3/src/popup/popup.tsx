import React, { useEffect, useState } from 'react';
import { createRoot } from 'react-dom/client';

import './popup.css';

import Header from './header';
import Body from './body';
import Banner from './banner';

import { APP_URL } from '../domain/constants';
import { Settings } from '../domain/settings';
import { BlockingInfo } from '../domain/types';

const App: React.FC = () => {
  const [isBlockingEnabled, setBlockingEnabled] = useState<boolean>(true);
  const [blockingInfo, setBlockingInfo] = useState<BlockingInfo>(
    new BlockingInfo(),
  );

  const updateBlockingInfo = async () => {
    setBlockingInfo(await BlockingInfo.fetch());
  };

  useEffect(() => {
    const fetchSettings = async () => {
      const isEnabled = await Settings.getInstance().isBlockingEnabled();
      setBlockingEnabled(isEnabled);
    };

    fetchSettings();
  }, []);

  return (
    <>
      <Header
        isBlockingEnabled={isBlockingEnabled}
        setBlockingEnabled={setBlockingEnabled}
        updateBlockingInfo={updateBlockingInfo}
      />
      <Body
        isBlockingEnabled={isBlockingEnabled}
        blockingInfo={blockingInfo}
        updateBlockingInfo={updateBlockingInfo}
      />
      {isBlockingEnabled && <Banner installDesktopUrl={APP_URL} />}
    </>
  );
};

const root = document.createElement('div');
document.body.appendChild(root);

const reactRoot = createRoot(root);
reactRoot.render(<App />);
