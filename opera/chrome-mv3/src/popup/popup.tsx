import React, { useEffect, useState } from "react";
import { createRoot } from "react-dom/client";

import Header from "./header";
import Body from "./body";

import { Settings } from "../domain/settings";
import { BlockingInfo } from "../domain/types";

import { ToastContainer, toast } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";

import "./popup.css";

const App: React.FC = () => {
  const [isBlockingEnabled, setBlockingEnabled] = useState<boolean>(true);
  const [blockingInfo, setBlockingInfo] = useState<BlockingInfo>(new BlockingInfo());

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

  const showToastMessage = (msg: string) => {
    toast.info(msg, {
      position: toast.POSITION.BOTTOM_CENTER,
    });
  };

  return (
    <>
      <Header
        isBlockingEnabled={isBlockingEnabled}
        setBlockingEnabled={setBlockingEnabled}
        updateBlockingInfo={updateBlockingInfo}
        showToastMessage={showToastMessage}
      />
      <Body
        isBlockingEnabled={isBlockingEnabled}
        blockingInfo={blockingInfo}
        updateBlockingInfo={updateBlockingInfo}
      />
      <ToastContainer />
    </>
  );
};

const root = document.createElement("div");
document.body.appendChild(root);

const reactRoot = createRoot(root);
reactRoot.render(<App />);
