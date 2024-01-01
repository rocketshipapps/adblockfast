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
const build               = 9;
const previousBuild       = localStorage.build;
const isUpdatingToCurrent = !previousBuild || previousBuild < build;
const path                = isInOpera ? 'chrome/' : '';
const allowlist           = deserializeData(localStorage.allowlist || localStorage.whitelist) || {};
const hosts               = {};
const wereAdsFound        = {};
const openTab             = (url) => { chrome.tabs.create({ url }); };
const blockRequest        = (tabId, parentHost, type) => {
                              let blockingResponse      = { cancel: false };
                                  wereAdsFound[ tabId ] = true;

                              if ((deserializeData(localStorage.allowlist) || {})[ parentHost ]) {
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

                                blockingResponse = {
                                                     redirectUrl: type == 'image'
                                                                ? 'data:image/png;base64,'
                                                                + 'iVBORw0KGgoAAAANSUhEUgAAAAEAAAAB'
                                                                + 'CAYAAAAfFcSJAAAACklEQVR4nGMAAQAA'
                                                                + 'BQABDQottAAAAABJRU5ErkJggg=='
                                                                : 'about:blank'
                                                   };
                              }

                              return blockingResponse;
                            };
const allowTab            = (tab) => {
                              const id        = tab.id;
                              const url       = tab.url;
                              const host      = getHost(url);
                              const allowlist = deserializeData(localStorage.allowlist) || {};

                              if (allowlist[ host ]) {
                                                         delete allowlist[ host ];
                                localStorage.allowlist = JSON.stringify(allowlist);

                                chrome.tabs.reload(id);
                                plausible('Block', { u: `${ baseUrl }${ url }` });
                              } else {
                                     allowlist[ host ] = true;
                                localStorage.allowlist = JSON.stringify(allowlist);

                                chrome.tabs.reload(id);
                                plausible('Unblock', { u: `${ baseUrl }${ url }` });
                              }
                            };
const getExperiments      = (callback) => {
                              if (database) {
                                database.ref('experiments').once('value').then((snapshot) => {
                                  callback(snapshot.val());
                                });
                              } else {
                                callback([]);
                              }
                            };
const setExperimentUp     = () => {
                              const experiment     = deserializeData(localStorage.experiment);
                              const toastViewCount = localStorage.toastViewCount;

                              if (experiment) {
                                if (toastViewCount < 3) {
                                  const toastViewType = experiment.toastViewType;
                                  const toastBodyText = experiment.toastBodyText;

                                  if (toastViewType && toastBodyText) {
                                    if (toastViewType == 'badge') {
                                      const toastColor   = experiment.toastColor;
                                      const toastTooltip = experiment.toastTooltip;
                                      const mainViewType = experiment.mainViewType;

                                      if (toastTooltip) {
                                        chrome.browserAction.getTitle({}, (tooltip) => {
                                          localStorage.tooltip = tooltip;

                                          chrome.browserAction
                                                .setTitle({ title: `${ toastTooltip }` });
                                        });
                                      }

                                      if (mainViewType && mainViewType == 'popup'
                                                       && experiment.mainHeadline
                                                       && experiment.mainBodyText
                                                       && experiment.denyButtonLabel
                                                       && experiment.grantButtonLabel
                                                       && experiment.mainFootnote) {
                                        chrome.browserAction.getPopup({}, (popup) => {
                                          localStorage.popup = popup;

                                          chrome.browserAction.setPopup({
                                            popup: `${ path }markup/experimental-popup.html`
                                          });
                                        });
                                      }

                                      if (toastColor) {
                                        chrome.browserAction
                                              .getBadgeBackgroundColor({}, (color) => {
                                          localStorage.badgeColor = JSON.stringify(color);

                                          chrome.browserAction
                                                .setBadgeBackgroundColor({ color: toastColor });
                                        });
                                      }

                                      chrome.browserAction
                                            .setBadgeText({ text: `${ toastBodyText }` });
                                      localStorage.toastViewCount++;
                                      saveUser();
                                    } else if (toastViewType == 'notification') {
                                      const toastHeadline = experiment.toastHeadline;
                                      const toastIconUrl  = experiment.toastIconUrl;

                                      if (toastHeadline && toastIconUrl) {
                                        chrome.notifications.create({
                                                        type: 'basic',
                                                       title: toastHeadline,
                                                     message: toastBodyText,
                                                     iconUrl: toastIconUrl,
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
                                const experimentalGroup = localStorage.experimentalGroup;

                                if (experimentalGroup) {
                                  getExperiments((experiments) => {
                                    const experiment = experiments[ experimentalGroup ];

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
                              if (database && uid && uids && timestamp) {
                                database.ref(`users/${ uid }`).set({
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
                              if (database && timestamp) {
                                database.ref('errors').push({ message: error.message, timestamp });
                              }
                            };
let   database;
let   user;
let   uid;
let   uids;
let   timestamp;

injectPlausible('scripts/vendor/');

if (!previousBuild) {
  localStorage.firstBuild = build;
  localStorage.allowlist  = JSON.stringify({});

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

chrome.webRequest.onBeforeRequest.addListener((details) => {
  const tabId     = details.tabId;
  const url       = details.url;
  const type      = details.type;
  const childHost = getHost(url);
  const isParent  = type == 'main_frame';

  if (isParent) hosts[ tabId ] = childHost;

  const parentHost       = hosts[ tabId ];
  let   blockingResponse = { cancel: false };

  if (tabId + 1 && !isParent && parentHost) {
    if (childHost != parentHost) {
      for (var i = domainCount - 1; i + 1; i--) {
        if (domains[i].test(childHost)) {
          blockingResponse = blockRequest(tabId, parentHost, type);

          break;
        }
      }
    }

    if (deserializeData(
          localStorage.wasGrantButtonPressed
        ) && parentHost == 'www.youtube.com'
          && /get_(?:video_(?:metadata|info)|midroll_info)/.test(
               url
             )) {
      blockingResponse = blockRequest(tabId, parentHost, type);
    }
  }

  return blockingResponse;
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

      if (message.shouldInitialize) {
        sendResponse({
                     parentHost,
                  isAllowlisted,
          wasGrantButtonPressed: deserializeData(localStorage.wasGrantButtonPressed)
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
});

chrome.browserAction.onClicked.addListener((tab) => {
  const experiment = deserializeData(localStorage.experiment);

  if (experiment) {
    const toastViewType = experiment.toastViewType;

    if (toastViewType && toastViewType == 'badge' && experiment.toastBodyText) {
      const mainViewType = experiment.mainViewType;

      if (mainViewType && mainViewType == 'tab'
                       && experiment.mainTitle
                       && experiment.mainHeadline
                       && experiment.mainBodyText
                       && experiment.denyButtonLabel
                       && experiment.grantButtonLabel
                       && experiment.mainFootnote) {
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
