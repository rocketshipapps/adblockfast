/*
  Copyright 2018– Rocketship <https://rocketshipapps.com/>.

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
const experiment         = deserializeData(localStorage.experiment);
const tearExperimentDown = () => {
                             const toastType = experiment.toastViewType;

                             if (toastType && toastType == 'badge' && experiment.toastBodyText) {
                               const mainType = experiment.mainViewType;

                               if (experiment.toastTooltip) {
                                 chrome.browserAction.setTitle({ title: localStorage.tooltip });
                                 delete localStorage.tooltip;
                               }

                               if (
                                 mainType && mainType == 'popup'
                                          && experiment.mainHeadline
                                          && experiment.mainBodyText
                                          && experiment.denyButtonLabel
                                          && experiment.grantButtonLabel
                                          && experiment.mainFootnote
                               ) {
                                 chrome.browserAction.setPopup({ popup: localStorage.popup });
                                 delete localStorage.popup;
                               }

                               if (experiment.toastColor) {
                                 chrome.browserAction.setBadgeBackgroundColor({
                                   color: deserializeData(localStorage.badgeColor)
                                 });
                                 delete localStorage.badgeColor;
                               }

                               chrome.browserAction.setBadgeText({ text: '' });
                             }

                             delete localStorage.experiment;
                           };

if (experiment) {
  onPageReady(() => {
    const denyButton                                           = document.getElementById('deny');
    const grantButton                                          = document.getElementById('grant');
          denyButton.textContent                               = experiment.denyButtonLabel;
          grantButton.textContent                              = experiment.grantButtonLabel;
          document.getElementsByTagName('h1')[ 0 ].textContent = experiment.mainHeadline;
          document.getElementsByTagName('h2')[ 0 ].textContent = experiment.mainBodyText;
          document.getElementById('footnote').textContent      = experiment.mainFootnote;

    localStorage.toastClickCount++;
    localStorage.mainViewCount++;
    chrome.runtime.sendMessage({ shouldSaveUser: true });
    injectPlausible('../scripts/vendor/');
    plausible('pageview', { u: `${ baseUrl }experimental-popup` });
    denyButton.addEventListener('click', () => {
      localStorage.wasDenyButtonPressed = true;

      tearExperimentDown();
      chrome.runtime.sendMessage({ shouldSaveUser: true });
      plausible('Deny', { u: `${ baseUrl }experimental-popup` });
      close();
    });
    grantButton.addEventListener('click', () => {
      localStorage.wasGrantButtonPressed = true;

      tearExperimentDown();
      chrome.runtime.sendMessage({ shouldSaveUser: true });
      plausible('Grant', { u: `${ baseUrl }experimental-popup` });
      close();
    });
  });
}
