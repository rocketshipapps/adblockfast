/*
  Copyright 2015, 2016 Rocketship <https://rocketshipapps.com/>

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
function deserialize(object) {
  return typeof object == 'string' ? JSON.parse(object) : object;
}

const CURRENT_BUILD = 1;
const PREVIOUS_BUILD = localStorage.build;
const TABS = chrome.tabs;
const BROWSER_ACTION = chrome.browserAction;
const HOSTS = {};
const ADS = {};
const OPERA = navigator.userAgent.indexOf('OPR') + 1;
const PATH = OPERA ? 'chrome/' : '';

if (!PREVIOUS_BUILD) {
  localStorage.firstBuild = CURRENT_BUILD;
  localStorage.build = CURRENT_BUILD;
  localStorage.whitelist = JSON.stringify({});
  TABS.create({url: PATH + 'markup/firstrun.html'});
}

TABS.query({}, function(tabs) {
  const TAB_COUNT = tabs.length;

  for (var i = 0; i < TAB_COUNT; i++) {
    var tab = tabs[i];
    HOSTS[tab.id] = getHost(tab.url);
  }
});

chrome.webRequest.onBeforeRequest.addListener(function(details) {
  const TAB_ID = details.tabId;
  const TYPE = details.type;
  const PARENT = TYPE == 'main_frame';
  const CHILD_HOST = getHost(details.url);
  if (PARENT) HOSTS[TAB_ID] = CHILD_HOST;
  const PARENT_HOST = HOSTS[TAB_ID];
  var blockingResponse = {cancel: false};
  if (TAB_ID + 1 && !PARENT && PARENT_HOST && CHILD_HOST != PARENT_HOST)
      for (var i = DOMAINS_LENGTH - 1; i + 1; i--)
          if (DOMAINS[i].test(CHILD_HOST)) {
            if ((deserialize(localStorage.whitelist) || {})[PARENT_HOST])
                BROWSER_ACTION.setIcon({
                  tabId: TAB_ID,
                  path: {
                    '19': PATH + 'images/unblocked-ads/19.png',
                    '38': PATH + 'images/unblocked-ads/38.png'
                  }
                });
            else {
              blockingResponse = {
                redirectUrl:
                    TYPE == 'image' ?
                        'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAACklEQVR4nGMAAQAABQABDQottAAAAABJRU5ErkJggg=='
                            : 'about:blank'
              };
              BROWSER_ACTION.setIcon({
                tabId: TAB_ID,
                path: {
                  '19': PATH + 'images/blocked-ads/19.png',
                  '38': PATH + 'images/blocked-ads/38.png'
                }
              });
            }

            ADS[TAB_ID] = true;
            break;
          }
  return blockingResponse;
}, {urls: ['http://*/*', 'https://*/*']}, ['blocking']);

chrome.webNavigation.onCommitted.addListener(function(details) {
  if (!details.frameId) {
    const TAB_ID = details.tabId;
    delete ADS[TAB_ID];

    if ((deserialize(localStorage.whitelist) || {})[getHost(details.url)]) {
      BROWSER_ACTION.setIcon({
        tabId: TAB_ID,
        path: {
          '19': PATH + 'images/unblocked/19.png',
          '38': PATH + 'images/unblocked/38.png'
        }
      });
      BROWSER_ACTION.setTitle({tabId: TAB_ID, title: 'Block ads on this site'});
    } else {
      BROWSER_ACTION.setIcon({
        tabId: TAB_ID,
        path: {
          '19': PATH + 'images/blocked/19.png',
          '38': PATH + 'images/blocked/38.png'
        }
      });
      BROWSER_ACTION.setTitle({
        tabId: TAB_ID, title: 'Unblock ads on this site'
      });
    }
  }
});

chrome.extension.onRequest.addListener(function(request, sender, sendResponse) {
  const TAB = sender.tab;

  if (TAB) {
    const PARENT_HOST = getHost(TAB.url);
    const WHITELISTED =
        (deserialize(localStorage.whitelist) || {})[PARENT_HOST];

    if (request.initialized)
        sendResponse({parentHost: PARENT_HOST, whitelisted: WHITELISTED});
    else {
      request.ads && BROWSER_ACTION.setIcon({
        tabId: TAB.id,
        path: {
          '19':
              PATH + 'images/' + (WHITELISTED ? 'un' : '') +
                  'blocked-ads/19.png',
          '38':
              PATH + 'images/' + (WHITELISTED ? 'un' : '') +
                  'blocked-ads/38.png'
        }
      });
      sendResponse({});
    }
  } else sendResponse({});
});

BROWSER_ACTION.onClicked.addListener(function(tab) {
  const WHITELIST = deserialize(localStorage.whitelist) || {};
  const HOST = getHost(tab.url);
  const ID = tab.id;

  if (WHITELIST[HOST]) {
    delete WHITELIST[HOST];
    localStorage.whitelist = JSON.stringify(WHITELIST);
    TABS.reload(ID);
  } else {
    WHITELIST[HOST] = true;
    localStorage.whitelist = JSON.stringify(WHITELIST);
    TABS.reload(ID);
  }
});
