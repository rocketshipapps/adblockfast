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
                             const type = experiment.toastViewType;

                             if (type && type == 'badge' && experiment.toastBodyText) {
                               if (experiment.toastTooltip) {
                                 chrome.browserAction.setTitle({ title: localStorage.tooltip });
                                 delete localStorage.tooltip;
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
    const denyButton                                              = document.getElementById('deny');
    const grantButton                                             = document.getElementById(
                                                                      'grant'
                                                                    );
          denyButton.textContent                                  = experiment.denyButtonLabel;
          grantButton.textContent                                 = experiment.grantButtonLabel;
          document.getElementsByTagName('title')[ 0 ].textContent = experiment.mainTitle;
          document.getElementsByTagName('h1')[ 0 ].textContent    = experiment.mainHeadline;
          document.getElementsByTagName('h2')[ 0 ].textContent    = experiment.mainBodyText;
          document.getElementById('footnote').textContent         = experiment.mainFootnote;

    localStorage.mainViewCount++;
    chrome.runtime.sendMessage({ shouldSaveUser: true });
    injectPlausible('../scripts/vendor/');
    plausible('pageview', { u: `${ baseUrl }experimental-tab` });
    denyButton.addEventListener('click', () => {
      localStorage.wasDenyButtonPressed = true;

      tearExperimentDown();
      chrome.runtime.sendMessage({ shouldSaveUser: true });
      plausible('Deny', { u: `${ baseUrl }experimental-tab` });
      close();
    });
    grantButton.addEventListener('click', () => {
      localStorage.wasGrantButtonPressed = true;

      tearExperimentDown();
      chrome.runtime.sendMessage({ shouldSaveUser: true });
      plausible('Grant', { u: `${ baseUrl }experimental-tab` });
      close();
    });
  });
}
