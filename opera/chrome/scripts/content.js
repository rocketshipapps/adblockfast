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
const populateStylesheet = (style, selector) => {
                             const sheet = style.sheet;

                             if (sheet) {
                               sheet.insertRule(`${ selector } { display: none !important }`, 0);
                             } else {
                               setTimeout(() => { populateStylesheet(style, selector); }, 0);
                             }
                           };
const hideSponsoredPosts = (newsFeed, isAllowlisted) => {
                             let wereSponsoredPostsFound;

                             if (newsFeed.nodeType == 1) {
                               const posts     = newsFeed.querySelectorAll('[role="article"]');
                               const postCount = posts.length;

                               for (var i = 0; i < postCount; i++) {
                                 const post                = posts[i];
                                 const subheadingContainer = post.getElementsByClassName(
                                                               '_3nlk'
                                                             )[0];
                                 let   subheading          = '';

                                 if (subheadingContainer) {
                                   subheading = subheadingContainer.textContent;
                                 } else {
                                   const characters     = post.getElementsByClassName(
                                                            'c_wrj_nh4q_ v_wrj_nhu6i'
                                                          );
                                   const characterCount = characters.length;

                                   for (var j = 0; j < characterCount; j++) {
                                     const character = characters[j];

                                     if (getComputedStyle(character).display != 'none') {
                                       subheading += character.getAttribute('data-content');
                                     }
                                   }
                                 }

                                 if (subheading == 'Sponsored') {
                                   if (!isAllowlisted) post.style.display = 'none';

                                   wereSponsoredPostsFound = true;
                                 }
                               }
                             }

                             return wereSponsoredPostsFound;
                           };
const hidePromotedTweets = (timeline, isAllowlisted) => {
                             let werePromotedTweetsFound;

                             if (timeline.nodeType == 1) {
                               const tweets     = timeline.querySelectorAll(
                                                    '[data-testid="trend"]'
                                                  );
                               const tweetCount = tweets.length;

                               for (var i = 0; i < tweetCount; i++) {
                                 const tweet    = tweets[i];
                                 const metadata = tweet.querySelector(
                                                    '[data-testid="metadata"] .r-1qd0xha'
                                                  );

                                 if (metadata && metadata.textContent.slice(0, 8) == 'Promoted') {
                                   if (!isAllowlisted) tweet.parentElement.style.display = 'none';

                                   werePromotedTweetsFound = true;
                                 }
                               }
                             }

                             return werePromotedTweetsFound;
                           };
let   focusedElement;

chrome.runtime.sendMessage({ shouldInitialize: true }, (response) => {
  const parentHost            = response.parentHost;
  const isAllowlisted         = response.isAllowlisted;
  const wasGrantButtonPressed = response.wasGrantButtonPressed;
  let   wereAdsFound;

  if (parentHost) {
    let selector = selectors[ parentHost ];
        selector = '#ad, .ad, .ad-container, .ad-top, .adsbygoogle, .adv, .advertisement, '
                 + '.advertorial, .bottom-ad, [id^=div-gpt-ad-], .fs_ads, .m-ad, '
                 + '.searchCenterBottomAds, .searchCenterTopAds, .side-ad'
                 + (selector ? `, ${ selector }` : '');

    if (wasGrantButtonPressed && parentHost == 'twitter.com') {
      selector += ', .css-1dbjc4n.r-my5ep6.r-qklmqi.r-1adg3ll > '
                  + '.css-1dbjc4n.r-1loqt21.r-o7ynqc.r-1j63xyz > '
                  + '[class="css-1dbjc4n"], '
                + '[class="css-1dbjc4n r-e84r5y r-1or9b2r"], '
                + '.css-1dbjc4n.r-my5ep6.r-qklmqi.r-1adg3ll > '
                  + '[class="css-1dbjc4n"], '
                + '[aria-label="Who to follow"] '
                  + '[data-testid="UserCell"]:first-child, '
                + '.css-1dbjc4n.r-my5ep6.r-qklmqi.r-1adg3ll > '
                  + '.css-1dbjc4n.r-1wtj0ep.r-1sp51qo, '
                + '[class="css-1dbjc4n r-1jgb5lz r-1ye8kvj r-13qz1uu"] '
                  + '[data-testid="UserCell"]';
    }

    if (selector) {
      if (!isAllowlisted) {
        const style = document.createElement('style');

        (document.head || document.documentElement).insertBefore(style, null);
        populateStylesheet(style, selector);
      }

      onPageReady(() => {
        if (wasGrantButtonPressed) {
          if (parentHost == 'www.facebook.com') {
            const newsFeed = document.getElementById('content');

            if (newsFeed) {
              (new MutationObserver((mutations) => {
                const mutationCount = mutations.length;

                for (var i = 0; i < mutationCount; i++) {
                  if (hideSponsoredPosts(mutations[i].target, isAllowlisted)) wereAdsFound = true;
                }
              })).observe(newsFeed, { childList: true, subtree: true });

              if (hideSponsoredPosts(newsFeed, isAllowlisted)) wereAdsFound = true;
            }
          } else if (parentHost == 'twitter.com') {
            const timeline = document.body;

            if (timeline) {
              (new MutationObserver((mutations) => {
                const mutationCount = mutations.length;

                for (var i = 0; i < mutationCount; i++) {
                  if (hidePromotedTweets(mutations[i].target, isAllowlisted)) wereAdsFound = true;
                }
              })).observe(timeline, { childList: true, subtree: true });

              if (hidePromotedTweets(timeline, isAllowlisted)) wereAdsFound = true;
            }
          }
        }

        if (document.querySelectorAll(selector).length) wereAdsFound = true;
      });
    }

    onPageReady(() => {
      const iframes     = document.getElementsByTagName('iframe');
      const images      = document.getElementsByTagName('img');
      const iframeCount = iframes.length;
      const imageCount  = images.length;

      for (var i = 0; i < iframeCount; i++) {
        const iframe    = iframes[i];
        const childHost = getHost(iframe.src);

        if (childHost != parentHost) {
          for (var j = domainCount - 1; j + 1; j--) {
            if (domains[j].test(childHost)) {
              if (!isAllowlisted) {
                const className        = iframe.className;
                      iframe.className = (className ? `${ className } ` : '')
                                       + 'adblockfast-collapsed';
              }

              break;
            }
          }
        }
      }

      for (i = 0; i < imageCount; i++) {
        const image     = images[i];
        const childHost = getHost(image.src);

        if (childHost != parentHost) {
          for (var j = domainCount - 1; j + 1; j--) {
            if (domains[j].test(childHost)) {
              if (!isAllowlisted) {
                const className       = image.className;
                      image.className = (className ? `${ className } ` : '')
                                      + 'adblockfast-collapsed';
              }

              break;
            }
          }
        }
      }
    });
  }

  onPageReady(() => { chrome.extension.sendRequest({ wereAdsFound: wereAdsFound }); });
});

addEventListener('contextmenu', (event) => { focusedElement = event.target; });

chrome.runtime.onMessage.addListener((message, sender, sendResponse) => {
  if (message.wasContextItemSelected) {
    let element = focusedElement;

    if (element) {
      let tag      = element.tagName;
      let selector;

      while (tag) {
        let sibling = element.previousSibling;
        let index   = 1;

        while (sibling) {
          if (sibling.nodeType == 1 && sibling.tagName == tag) index++;

          sibling = sibling.previousSibling;
        }

        selector = `${ tag.toLowerCase() }:nth-of-type(${ index })`
                 + (selector ? ` > ${ selector }` : '');
        element  = element.parentNode;
        tag      = element.tagName;
      }

      sendResponse({ activeSelector: selector });
    }
  }
});
