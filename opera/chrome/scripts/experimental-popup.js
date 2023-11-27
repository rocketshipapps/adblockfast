/*
  Copyright 2018– Rocketship <https://rocketshipapps.com/>

  This program is free software: you can redistribute it and/or modify it under the terms of the GNU
  General Public License as published by the Free Software Foundation, either version 3 of the
  License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  General Public License for more details.

  You should have received a copy of the GNU General Public License along with this program. If not,
  see https://www.gnu.org/licenses/.

  Authors (one per line):

    Brian Kennish <brian@rocketshipapps.com>
*/
function tearExperimentDown() {
  const TOAST_VIEW_TYPE = EXPERIMENT.toastViewType;

  if (TOAST_VIEW_TYPE && TOAST_VIEW_TYPE == 'badge' && EXPERIMENT.toastBodyText) {
    const MAIN_VIEW_TYPE = EXPERIMENT.mainViewType;

    if (
      MAIN_VIEW_TYPE && MAIN_VIEW_TYPE == 'popup' && EXPERIMENT.mainHeadline &&
          EXPERIMENT.mainBodyText && EXPERIMENT.denyButtonLabel && EXPERIMENT.grantButtonLabel &&
              EXPERIMENT.mainFootnote
    ) {
      chrome.browserAction.setPopup({ popup: localStorage.popup });
      delete localStorage.popup;
    }

    chrome.browserAction.setBadgeText({ text: '' });

    if (EXPERIMENT.toastTooltip) {
      chrome.browserAction.setTitle({ title: localStorage.tooltip });
      delete localStorage.tooltip;
    }

    if (EXPERIMENT.toastColor) {
      chrome.browserAction.setBadgeBackgroundColor({ color: deserialize(localStorage.badgeColor) });
      delete localStorage.badgeColor;
    }
  }

  delete localStorage.experiment;
}

const EXPERIMENT = deserialize(localStorage.experiment);

if (EXPERIMENT) {
  onReady(function() {
    localStorage.toastClickCount++;
    chrome.extension.sendRequest({ shouldSaveUser: true });
    document.getElementsByTagName('h1')[ 0 ].textContent = EXPERIMENT.mainHeadline;
    document.getElementsByTagName('h2')[ 0 ].textContent = EXPERIMENT.mainBodyText;
    const DENY_BUTTON = document.getElementById('deny');

    DENY_BUTTON.onclick = function() {
      tearExperimentDown();
      localStorage.wasDenyButtonPressed = true;
      chrome.extension.sendRequest({ shouldSaveUser: true });
      close();
    };

    DENY_BUTTON.textContent = EXPERIMENT.denyButtonLabel;
    const GRANT_BUTTON = document.getElementById('grant');

    GRANT_BUTTON.onclick = function() {
      tearExperimentDown();
      localStorage.wasGrantButtonPressed = true;
      chrome.extension.sendRequest({ shouldSaveUser: true });
      close();
    };

    GRANT_BUTTON.textContent = EXPERIMENT.grantButtonLabel;
    document.getElementById('footnote').textContent = EXPERIMENT.mainFootnote;
    localStorage.mainViewCount++;
    chrome.extension.sendRequest({ shouldSaveUser: true });
  });
}
