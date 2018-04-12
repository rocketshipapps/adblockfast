/*
  Copyright 2018 Rocketship <https://rocketshipapps.com/>

  This program is free software: you can redistribute it and/or modify it under
  the terms of the GNU General Public License as published by the Free Software
  Foundation, either version 3 of the License, or (at your option) any later
  version.

  This program is distributed in the hope that it will be useful, but WITHOUT
  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

  You should have received a copy of the GNU General Public License along with
  this program. If not, see <http://www.gnu.org/licenses/>.

  Authors (one per line):

    Brian Kennish <brian@rocketshipapps.com>
*/
function tearExperimentDown() {
  const TOAST_VIEW_TYPE = EXPERIMENT.toastViewType;

  if (
    TOAST_VIEW_TYPE && TOAST_VIEW_TYPE == 'badge' && EXPERIMENT.toastBodyText
  ) {
    BROWSER_ACTION.setBadgeText({text: ''});

    if (EXPERIMENT.toastTooltip) {
      BROWSER_ACTION.setTitle({title: localStorage.tooltip});
      delete localStorage.tooltip;
    }

    if (EXPERIMENT.toastColor) {
      BROWSER_ACTION.setBadgeBackgroundColor({
        color: deserialize(localStorage.badgeColor)
      });
      delete localStorage.badgeColor;
    }
  }

  delete localStorage.experiment;
}

const EXPERIMENT = deserialize(localStorage.experiment);

if (EXPERIMENT) {
  onReady(function() {
    document.getElementsByTagName('title')[0].textContent =
        EXPERIMENT.mainTitle;
    document.getElementsByTagName('h1')[0].textContent =
        EXPERIMENT.mainHeadline;
    document.getElementsByTagName('h2')[0].textContent =
        EXPERIMENT.mainBodyText;
    const DENY_BUTTON = document.getElementById('deny');

    DENY_BUTTON.onclick = function() {
      tearExperimentDown();
      localStorage.wasDenyButtonPressed = true;
      EXTENSION.sendRequest({shouldSaveUser: true});
      close();
    };

    DENY_BUTTON.textContent = EXPERIMENT.denyButtonLabel;
    const GRANT_BUTTON = document.getElementById('grant');

    GRANT_BUTTON.onclick = function() {
      tearExperimentDown();
      localStorage.wasGrantButtonPressed = true;
      EXTENSION.sendRequest({shouldSaveUser: true});
      close();
    };

    GRANT_BUTTON.textContent = EXPERIMENT.grantButtonLabel;
    document.getElementById('footnote').textContent = EXPERIMENT.mainFootnote;
    localStorage.mainViewCount++;
    EXTENSION.sendRequest({shouldSaveUser: true});
  });
}
