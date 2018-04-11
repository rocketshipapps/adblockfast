/*
  Copyright 2015–2018 Rocketship <https://rocketshipapps.com/>

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

function spawn(tab) { TABS.create({url: tab}); }

function saveError(error) {
  database.ref('errors').push({
    message: error.message,
    timestamp: timestamp
  });
}

function saveUser() {
  if (uid) database.ref('users/' + uid).set({
    uids: uids,
    platform: BROWSER,
    build: BUILD,
    firstBuild: localStorage.firstBuild,
    experimentalGroup: localStorage.experimentalGroup,
    toastViewCount: localStorage.toastViewCount,
    toastClickCount: localStorage.toastClickCount,
    mainViewCount: localStorage.mainViewCount,
    mainClickCount: localStorage.mainClickCount,
    timestamp: timestamp
  });
  else saveError({message: 'No user ID'});
}

function getExperiments(callback) {
  database.ref('experiments').once('value').then(function(snapshot) {
    callback(snapshot.val());
  });
}

function setExperimentUp() {
  const EXPERIMENT = deserialize(localStorage.experiment);

  if (EXPERIMENT) {
    const TOAST_VIEW_TYPE = EXPERIMENT.toastViewType;
    const TOAST_BODY = EXPERIMENT.toastBody;

    if (TOAST_VIEW_TYPE && TOAST_BODY) {
      const MAIN_VIEW_TYPE = EXPERIMENT.mainViewType;
      const MAIN_CONTENT_URL = EXPERIMENT.mainContentUrl;
      const HAS_MAIN_VIEW = MAIN_VIEW_TYPE && MAIN_CONTENT_URL;

      if (TOAST_VIEW_TYPE == 'badge') {
        const TOAST_COLOR = EXPERIMENT.toastColor;

        TOAST_COLOR &&
            BROWSER_ACTION.getBadgeBackgroundColor({}, function(color) {
              localStorage.badgeColor = JSON.stringify(color);
              BROWSER_ACTION.setBadgeBackgroundColor({color: TOAST_COLOR});
            });

        const TOAST_TOOLTIP = EXPERIMENT.toastTooltip;

        TOAST_TOOLTIP &&
            BROWSER_ACTION.getTitle({}, function(tooltip) {
              localStorage.tooltip = tooltip;
              BROWSER_ACTION.setTitle({title: TOAST_TOOLTIP + ''});
            });

        BROWSER_ACTION.setBadgeText({text: TOAST_BODY + ''});
        localStorage.toastViewCount++;
        saveUser();

        HAS_MAIN_VIEW && MAIN_VIEW_TYPE == 'popup' &&
            BROWSER_ACTION.getPopup({}, function(popup) {
              localStorage.popup = popup;
              localStorage.experimentalPopup = MAIN_CONTENT_URL;
              BROWSER_ACTION.setPopup({
                popup: PATH + 'markup/experimental-popup.html',
              });
            });
      } else if (TOAST_VIEW_TYPE == 'notification') {
        const TOAST_HEADING = EXPERIMENT.toastHeading;

        if (TOAST_HEADING) {
          NOTIFICATIONS.create({
            type: 'basic',
            title: TOAST_HEADING,
            message: TOAST_BODY,
            iconUrl: PATH + 'images/192.png',
            requireInteraction: !EXPERIMENT.isToastDismissible
          });
          localStorage.toastViewCount++;
          saveUser();
          if (HAS_MAIN_VIEW && MAIN_VIEW_TYPE == 'tab')
              localStorage.tab = MAIN_CONTENT_URL;
        }
      }
    }
  } else if (!localStorage.toastViewCount) {
    const EXPERIMENTAL_GROUP = localStorage.experimentalGroup;

    if (EXPERIMENTAL_GROUP) {
      getExperiments(function(experiments) {
        const EXPERIMENT = experiments[EXPERIMENTAL_GROUP];

        if (EXPERIMENT) {
          localStorage.experiment = JSON.stringify(EXPERIMENT);
          setExperimentUp();
        }
      });
    }
  }
}

function whitelist(tab) {
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
}

const BUILD = 5;
const PREVIOUS_BUILD = localStorage.build;
const RUNTIME = chrome.runtime;
const TABS = chrome.tabs;
const BROWSER_ACTION = chrome.browserAction;
const NOTIFICATIONS = chrome.notifications;
const HOSTS = {};
const WERE_ADS_FOUND = {};
const IS_IN_OPERA = navigator.userAgent.indexOf('OPR') + 1;
const BROWSER = IS_IN_OPERA ? 'opera' : 'chrome';
const PATH = IS_IN_OPERA ? 'chrome/' : '';
var authentication;
var database;
var user;
var uid;
var uids;
var timestamp;

if (!PREVIOUS_BUILD) {
  localStorage.firstBuild = BUILD;
  localStorage.whitelist = JSON.stringify({});
  spawn(PATH + 'markup/firstrun.html');
}

if (!PREVIOUS_BUILD || PREVIOUS_BUILD < BUILD) {
  localStorage.build = BUILD;
  localStorage.uids = JSON.stringify([]);
}

firebase.initializeApp({
  apiKey: 'AIzaSyCBqw-meqiUNJ78g9acszr23TevYZ8MmVY',
  authDomain: 'adblock-fast.firebaseapp.com',
  databaseURL: 'https://adblock-fast.firebaseio.com',
  messagingSenderId: '109831748909',
  projectId: 'adblock-fast',
  storageBucket: 'adblock-fast.appspot.com'
}).auth().onAuthStateChanged(function(user) {
  if (user && !uid) {
    uid = user.uid;
    uids = deserialize(localStorage.uids);

    if (!uids.includes(uid)) {
      uids.push(uid);
      localStorage.uids = JSON.stringify(uids);
      localStorage.experimentalGroup = 0;
      localStorage.toastViewCount = 0;
      localStorage.toastClickCount = 0;
      localStorage.mainViewCount = 0;
      localStorage.mainClickCount = 0;

      database.ref('groups/' + BROWSER).once('value').then(function(snapshot) {
        const GROUPS = snapshot.val();

        GROUPS && getExperiments(function(experiments) {
          if (experiments) {
            const GROUP_COUNT = GROUPS.count;
            const FIRST_GROUP = GROUPS.first;
            var experiment;
            var iterator = 0;

            do {
              localStorage.experimentalGroup =
                  GROUP_COUNT !== null && FIRST_GROUP !== null ?
                    Math.floor(Math.random() * GROUP_COUNT) + FIRST_GROUP : 0;
              experiment = experiments[localStorage.experimentalGroup];
              iterator++;

              if (iterator > 99) {
                localStorage.experimentalGroup = 0;
                experiment = undefined;
                break;
              }
            } while (
              experiment &&
                  (BUILD == localStorage.firstBuild || !experiment.isActive)
            );

            if (experiment) {
              localStorage.experiment = JSON.stringify(experiment);
              setExperimentUp();
            }

            saveUser();
          }
        });
      });

      saveUser();
    }
  }
});

authentication = firebase.auth();
user = authentication.currentUser;
database = firebase.database();
timestamp = firebase.database.ServerValue.TIMESTAMP;
if (user) uid = user.uid;
else authentication.signInAnonymously().catch(saveError);
setExperimentUp();

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
  const IS_PARENT = TYPE == 'main_frame';
  const CHILD_HOST = getHost(details.url);
  if (IS_PARENT) HOSTS[TAB_ID] = CHILD_HOST;
  const PARENT_HOST = HOSTS[TAB_ID];
  var blockingResponse = {cancel: false};

  if (TAB_ID + 1 && !IS_PARENT && PARENT_HOST && CHILD_HOST != PARENT_HOST)
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

            WERE_ADS_FOUND[TAB_ID] = true;
            break;
          }

  return blockingResponse;
}, {urls: ['http://*/*', 'https://*/*']}, ['blocking']);

chrome.webNavigation.onCommitted.addListener(function(details) {
  if (!details.frameId) {
    const TAB_ID = details.tabId;
    delete WERE_ADS_FOUND[TAB_ID];

    if ((deserialize(localStorage.whitelist) || {})[getHost(details.url)])
        BROWSER_ACTION.setIcon({
          tabId: TAB_ID,
          path: {
            '19': PATH + 'images/unblocked/19.png',
            '38': PATH + 'images/unblocked/38.png'
          }
        }, function() {
          RUNTIME.lastError || localStorage.tooltip ||
              BROWSER_ACTION.setTitle({
                tabId: TAB_ID, title: 'Block ads on this site'
              });
        });
    else BROWSER_ACTION.setIcon({
      tabId: TAB_ID,
      path: {
        '19': PATH + 'images/blocked/19.png',
        '38': PATH + 'images/blocked/38.png'
      }
    }, function() {
      RUNTIME.lastError || localStorage.tooltip ||
          BROWSER_ACTION.setTitle({
            tabId: TAB_ID, title: 'Unblock ads on this site'
          });
    });
  }
});

chrome.extension.onRequest.addListener(function(request, sender, sendResponse) {
  const TAB = sender.tab;

  if (TAB) {
    const PARENT_HOST = getHost(TAB.url);
    const IS_WHITELISTED =
        (deserialize(localStorage.whitelist) || {})[PARENT_HOST];

    if (request.shouldInitialize)
        sendResponse({parentHost: PARENT_HOST, isWhitelisted: IS_WHITELISTED});
    else {
      request.wereAdsFound &&
          BROWSER_ACTION.setIcon({
            tabId: TAB.id,
            path: {
              '19':
                  PATH + 'images/' + (IS_WHITELISTED ? 'un' : '') +
                      'blocked-ads/19.png',
              '38':
                  PATH + 'images/' + (IS_WHITELISTED ? 'un' : '') +
                      'blocked-ads/38.png'
            }
          });
      sendResponse({});
    }
  } else sendResponse({});
});

BROWSER_ACTION.onClicked.addListener(function(tab) {
  const EXPERIMENT = deserialize(localStorage.experiment);

  if (EXPERIMENT) {
    const TOAST_VIEW_TYPE = EXPERIMENT.toastViewType;

    if (TOAST_VIEW_TYPE && TOAST_VIEW_TYPE == 'badge' && EXPERIMENT.toastBody) {
      const MAIN_VIEW_TYPE = EXPERIMENT.mainViewType;
      const MAIN_CONTENT_URL = EXPERIMENT.mainContentUrl;

      if (MAIN_VIEW_TYPE && MAIN_CONTENT_URL)
          if (MAIN_VIEW_TYPE == 'popup') {
            BROWSER_ACTION.setPopup({popup: localStorage.popup});
            delete localStorage.popup;
            delete localStorage.experimentalPopup;
          } else if (MAIN_VIEW_TYPE == 'tab') spawn(MAIN_CONTENT_URL);

      delete localStorage.experiment;
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

      localStorage.toastClickCount++;
      saveUser();
    } else whitelist(tab);
  } else whitelist(tab);
});

NOTIFICATIONS.onClicked.addListener(function(id) {
  const TAB = localStorage.tab;

  if (TAB) {
    spawn(TAB);
    delete localStorage.tab;
    delete localStorage.experiment;
    NOTIFICATIONS.clear(id);
    localStorage.toastClickCount++;
    saveUser();
  }
});
