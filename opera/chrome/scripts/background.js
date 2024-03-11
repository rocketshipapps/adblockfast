/*
  Copyright 2015– Rocketship <https://rocketshipapps.com/>.

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
const build               = 10;
const previousBuild       = localStorage.build;
const isUpdatingToCurrent = !previousBuild || previousBuild < build;
const path                = isInOpera ? 'chrome/' : '';
const allowlist           = deserializeData(localStorage.allowlist || localStorage.whitelist) || {};
const hosts               = {};
const wereAdsFound        = {};
const openTab             = (url) => { chrome.tabs.create({ url }); };
const blockRequest        = (tabId, host, type) => {
                              let response              = { cancel: false };
                                  wereAdsFound[ tabId ] = true;

                              if ((deserializeData(localStorage.allowlist) || {})[ host ]) {
                                chrome.tabs.get(tabId, () => {
                                  if (!chrome.runtime.lastError) {
                                    chrome.browserAction.setIcon({
                                      tabId,
                                       path: {
                                               '19': `${ path }images/unblocked-ads/19.png`,
                                               '38': `${ path }images/unblocked-ads/38.png`
                                             }
                                    });
                                  }
                                });
                              } else {
                                chrome.tabs.get(tabId, () => {
                                  if (!chrome.runtime.lastError) {
                                    chrome.browserAction.setIcon({
                                      tabId,
                                       path: {
                                               '19': `${ path }images/blocked-ads/19.png`,
                                               '38': `${ path }images/blocked-ads/38.png`
                                             }
                                    });
                                  }
                                });

                                response = {
                                             redirectUrl: type == 'image' ? 'data:image/png;base64,'
                                                                          + 'iVBORw0KGgoAAAANSUhEUg'
                                                                          + 'AAAAEAAAABCAYAAAAfFcSJ'
                                                                          + 'AAAACklEQVR4nGMAAQAABQ'
                                                                          + 'ABDQottAAAAABJRU5ErkJg'
                                                                          + 'gg=='
                                                                          : 'about:blank'
                                           };
                              }

                              return response;
                            };
const allowTab            = (tab) => {
                              const id        = tab.id;
                              const url       = tab.url;
                              const host      = getHost(url);
                              const allowlist = deserializeData(localStorage.allowlist) || {};

                              if (allowlist[ host ]) {
                                delete allowlist[ host ];

                                plausible('Block', { u: `${ baseUrl }${ url }` });
                              } else {
                                allowlist[ host ] = true;

                                plausible('Unblock', { u: `${ baseUrl }${ url }` });
                              }

                              localStorage.allowlist = JSON.stringify(allowlist);

                              chrome.tabs.reload(id);
                            };
const getExperiments      = (callback) => {
                              if (db) {
                                db.ref('experiments').once('value').then((snapshot) => {
                                  callback(snapshot.val());
                                });
                              } else {
                                callback([]);
                              }
                            };
const setExperimentUp     = () => {
                              const experiment = deserializeData(localStorage.experiment);
                              const toastCount = localStorage.toastViewCount;

                              if (experiment) {
                                if (toastCount < 3) {
                                  const toastType = experiment.toastViewType;
                                  const bodyText  = experiment.toastBodyText;

                                  if (toastType && bodyText) {
                                    if (toastType == 'badge') {
                                      const color    = experiment.toastColor;
                                      const title    = `${ experiment.toastTooltip }`;
                                      const mainType = experiment.mainViewType;

                                      if (title) {
                                        chrome.browserAction.getTitle({}, (tooltip) => {
                                          localStorage.tooltip = tooltip;

                                          chrome.browserAction.setTitle({ title });
                                        });
                                      }

                                      if (
                                        mainType && mainType == 'popup'
                                                 && experiment.mainHeadline
                                                 && experiment.mainBodyText
                                                 && experiment.denyButtonLabel
                                                 && experiment.grantButtonLabel
                                                 && experiment.mainFootnote
                                      ) {
                                        chrome.browserAction.getPopup({}, (popup) => {
                                          localStorage.popup = popup;

                                          chrome.browserAction.setPopup({
                                            popup: `${ path }markup/experimental-popup.html`
                                          });
                                        });
                                      }

                                      if (color) {
                                        chrome.browserAction
                                              .getBadgeBackgroundColor({}, (color) => {
                                          localStorage.badgeColor = JSON.stringify(color);

                                          chrome.browserAction.setBadgeBackgroundColor({ color });
                                        });
                                      }

                                      chrome.browserAction.setBadgeText({ text: `${ bodyText }` });
                                      localStorage.toastViewCount++;
                                      saveUser();
                                    } else if (toastType == 'notification') {
                                      const title   = experiment.toastHeadline;
                                      const iconUrl = experiment.toastIconUrl;

                                      if (title && iconUrl) {
                                        chrome.notifications.create({
                                                        type: 'basic',
                                                       title,
                                                     message: bodyText,
                                                     iconUrl,
                                          requireInteraction: !experiment.isToastDismissible
                                        });
                                        localStorage.toastViewCount++;
                                        saveUser();
                                      }
                                    }
                                  }
                                } else if (isUpdatingToCurrent) {
                                  saveUser();
                                }
                              } else if (!deserializeData(toastViewCount)) {
                                const group = localStorage.experimentalGroup;

                                if (group) {
                                  getExperiments((experiments) => {
                                    const experiment = experiments[ group ];

                                    if (experiment) {
                                      localStorage.experiment = JSON.stringify(experiment);

                                      setExperimentUp();
                                    } else if (isUpdatingToCurrent) {
                                      saveUser();
                                    }
                                  });
                                }
                              }
                            };
const saveUser            = () => {
                              if (db && uid && uids && timestamp) {
                                db.ref(`users/${ uid }`).set({
                                                   uids,
                                               platform: browser,
                                                  build,
                                             firstBuild: deserializeData(
                                                           localStorage.firstBuild
                                                         ),
                                      experimentalGroup: deserializeData(
                                                           localStorage.experimentalGroup
                                                         ),
                                         toastViewCount: deserializeData(
                                                           localStorage.toastViewCount
                                                         ),
                                        toastClickCount: deserializeData(
                                                           localStorage.toastClickCount
                                                         ),
                                          mainViewCount: deserializeData(
                                                           localStorage.mainViewCount
                                                         ),
                                   wasDenyButtonPressed: deserializeData(
                                                           localStorage.wasDenyButtonPressed
                                                         ),
                                  wasGrantButtonPressed: deserializeData(
                                                           localStorage.wasGrantButtonPressed
                                                         ),
                                              timestamp
                                });
                              } else {
                                saveError({ message: 'No user ID' });
                              }
                            };
const saveError           = (error) => {
                              if (db && timestamp) {
                                db.ref('errors').push({ message: error.message, timestamp });
                              }
                            };
let   db;
let   user;
let   uid;
let   uids;
let   timestamp;

injectPlausible(`${ path }scripts/vendor/`);

if (!previousBuild) {
  localStorage.firstBuild = build;

  openTab(`${ path }markup/firstrun.html`);
  plausible('Install', { u: `${ baseUrl }v${ build }` });
}

if (!previousBuild || previousBuild < 5) localStorage.uids = JSON.stringify([]);

if (!previousBuild || previousBuild < 7) {
  allowlist[ 'buy.buysellads.com' ] = true;
  allowlist[ 'gs.statcounter.com' ] = true;
  localStorage.allowlist            = JSON.stringify(allowlist);
}

if (!previousBuild || previousBuild < 9) {
  allowlist[ 'amplitude.com' ]           = true;
  allowlist[ 'analytics.amplitude.com' ] = true;
  allowlist[ 'sumo.com' ]                = true;
  allowlist[ 'www.cnet.com' ]            = true;
  allowlist[ 'www.stitcher.com' ]        = true;
  localStorage.allowlist                 = JSON.stringify(allowlist);
}

if (isUpdatingToCurrent) {
  localStorage.build = build;

  if (previousBuild) {
    delete localStorage.whitelist;
    plausible('Update', { u: `${ baseUrl }v${ previousBuild }-to-v${ build }` });
  }
}

if (user) {
  uid = user.uid;

  setExperimentUp();
}

chrome.tabs.query({}, (tabs) => {
  const tabCount = tabs.length;

  for (var i = 0; i < tabCount; i++) {
    const tab             = tabs[i];
          hosts[ tab.id ] = getHost(tab.url);
  }
});

chrome.contextMenus.create({
  contexts: [ 'all' ],
     title: 'Hide or unhide element',
   onclick: (info, tab) => {
              chrome.tabs.sendMessage(tab.id, { wasContextItemSelected: true }, (response) => {
                const url      = tab.url;
                const selector = response.focusedSelector;
                const host     = getHost(url);

                chrome.storage.sync.get('blocklist', (items) => {
                  const blocklist     = items.blocklist || {};
                  const hostBlocklist = blocklist[ host ];

                  if (hostBlocklist) {
                    const index = hostBlocklist.indexOf(selector);

                    if (index + 1) {
                      hostBlocklist.splice(index, 1);

                      plausible('Unhide', { u: `${ baseUrl }${ url }` });
                    } else {
                      hostBlocklist.push(selector);

                      plausible('Hide', { u: `${ baseUrl }${ url }` });
                    }
                  } else {
                    blocklist[ host ] = [];
                                        blocklist[ host ].push(selector);

                    plausible('Hide', { u: `${ baseUrl }${ url }` });
                  }

                  chrome.storage.sync.set({ blocklist });
                });
              });
            }
});

if (localStorage.shouldDeletePersonalData) {
  chrome.browsingData.remove({}, {
          appcache: true,
             cache: true,
      cacheStorage: true,
           cookies: true,
         downloads: true,
       fileSystems: true,
           history: true,
         indexedDB: true,
      localStorage: true,
    serviceWorkers: true,
            webSQL: true
  });
}

chrome.webRequest.onBeforeRequest.addListener((details) => {
  const tabId     = details.tabId;
  const url       = details.url;
  const type      = details.type;
  const childHost = getHost(url);
  const isParent  = type == 'main_frame';

  if (isParent) hosts[ tabId ] = childHost;

  const parentHost = hosts[ tabId ];
  let   response   = { cancel: false };

  if (tabId + 1 && !isParent && parentHost) {
    if (childHost != parentHost) {
      for (var i = domainCount - 1; i + 1; i--) {
        if (domains[i].test(childHost)) {
          response = blockRequest(tabId, parentHost, type);

          break;
        }
      }
    }

    if (
      deserializeData(
        localStorage.wasGrantButtonPressed
      ) && parentHost == 'www.youtube.com'
        && /get_(?:video_(?:metadata|info)|midroll_info)/.test(
             url
           )
    ) {
      response = blockRequest(tabId, parentHost, type);
    }
  }

  return response;
}, { urls: [ '<all_urls>' ]}, [ 'blocking' ]);

chrome.webNavigation.onCommitted.addListener((details) => {
  if (!details.frameId) {
    const tabId = details.tabId;
                  delete wereAdsFound[ tabId ];

    chrome.tabs.get(tabId, () => {
      if (!chrome.runtime.lastError) {
        if ((deserializeData(localStorage.allowlist) || {})[ getHost(details.url) ]) {
          chrome.browserAction.setIcon({
            tabId,
             path: {
                     '19': `${ path }images/unblocked/19.png`,
                     '38': `${ path }images/unblocked/38.png`
                   }
          }, () => {
            if (!localStorage.tooltip) {
              chrome.browserAction.setTitle({ tabId, title: 'Block ads on this site' });
            }
          });
        } else {
          chrome.browserAction.setIcon({
            tabId,
             path: {
                     '19': `${ path }images/blocked/19.png`, '38': `${ path }images/blocked/38.png`
                   }
          }, () => {
            if (!localStorage.tooltip) {
              chrome.browserAction.setTitle({ tabId, title: 'Unblock ads on this site' });
            }
          });
        }
      }
    });
  }
});

chrome.runtime.onMessage.addListener((message, sender, sendResponse) => {
  if (message.shouldSaveUser) {
    saveUser();
    sendResponse({});
  } else {
    const tab = sender.tab;

    if (tab) {
      const parentHost    = getHost(tab.url);
      const isAllowlisted = (deserializeData(localStorage.allowlist) || {})[ parentHost ];

      if (message.shouldInit) {
        chrome.storage.sync.get('blocklist', (items) => {
          sendResponse({
                       parentHost,
                    userSelectors: (items.blocklist || {})[ parentHost ] || [],
                    isAllowlisted,
            wasGrantButtonPressed: deserializeData(localStorage.wasGrantButtonPressed)
          });
        });
      } else {
        const tabId = tab.id;

        chrome.tabs.get(tabId, () => {
          if (!chrome.runtime.lastError && message.wereAdsFound) {
            chrome.browserAction.setIcon({
              tabId,
               path: {
                       '19': `${ path }images/${ isAllowlisted ? 'un' : '' }blocked-ads/19.png`,
                       '38': `${ path }images/${ isAllowlisted ? 'un' : '' }blocked-ads/38.png`
                     }
            });
          }
        });

        sendResponse({});
      }
    } else {
      sendResponse({});
    }
  }

  return true;
});

chrome.browserAction.onClicked.addListener((tab) => {
  const experiment = deserializeData(localStorage.experiment);

  if (experiment) {
    const toastType = experiment.toastViewType;

    if (toastType && toastType == 'badge' && experiment.toastBodyText) {
      const mainType = experiment.mainViewType;

      if (
        mainType && mainType == 'tab'
                 && experiment.mainTitle
                 && experiment.mainHeadline
                 && experiment.mainBodyText
                 && experiment.denyButtonLabel
                 && experiment.grantButtonLabel
                 && experiment.mainFootnote
      ) {
        openTab(`${ path }markup/experimental-tab.html`);
      }

      localStorage.toastClickCount++;
      saveUser();
    } else {
      allowTab(tab);
    }
  } else {
    allowTab(tab);
  }
});

chrome.notifications.onClicked.addListener((id) => {
  openTab(`${ path }markup/experimental-tab.html`);
  chrome.notifications.clear(id);
  localStorage.toastClickCount++;
  saveUser();
});
