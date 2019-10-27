/*
  Copyright 2015–2019 Rocketship <https://rocketshipapps.com/>

  This program is free software: you can redistribute it and/or modify it under
  the terms of the GNU General Public License as published by the Free Software
  Foundation, either version 3 of the License, or (at your option) any later
  version.

  This program is distributed in the hope that it will be useful, but WITHOUT
  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

  You should have received a copy of the GNU General Public License along with
  this program. If not, see https://www.gnu.org/licenses/.

  Authors (one per line):

    Brian Kennish <brian@rocketshipapps.com>
*/
function populate(style, selector) {
  const SHEET = style.sheet;
  if (SHEET) SHEET.insertRule(selector + ' { display: none !important }', 0);
  else setTimeout(function() { populate(style, selector); }, 0);
}

function hideSponsoredPosts(newsFeed, isWhitelisted) {
  var wereSponsoredPostsFound;

  if (newsFeed.nodeType == 1)
      for (const POST of newsFeed.querySelectorAll('[role="article"]')) {
        var subheading = '';
        const SUBHEADING_CONTAINER = POST.getElementsByClassName('_3nlk')[0];
        if (SUBHEADING_CONTAINER) subheading = SUBHEADING_CONTAINER.textContent;
        else for (
          const CHARACTER of
              POST.getElementsByClassName('c_wrj_nh4q_ v_wrj_nhu6i')
        ) if (getComputedStyle(CHARACTER).display != 'none')
            subheading += CHARACTER.getAttribute('data-content');

        if (subheading == 'Sponsored') {
          if (!isWhitelisted) POST.style.display = 'none';
          wereSponsoredPostsFound = true;
        }
      }

  return wereSponsoredPostsFound;
}

function hidePromotedTweets(timeline, isWhitelisted) {
  var werePromotedTweetsFound;

  if (timeline.nodeType == 1)
      for (const TWEET of timeline.querySelectorAll('[data-testid="trend"]')) {
        const METADATA =
            TWEET.querySelector('[data-testid="metadata"] .r-1qd0xha');

        if (METADATA && METADATA.textContent.slice(0, 8) == 'Promoted') {
          if (!isWhitelisted) TWEET.parentElement.style.display = 'none';
          werePromotedTweetsFound = true;
        }
      }

  return werePromotedTweetsFound;
}

EXTENSION.sendRequest({shouldInitialize: true}, function(response) {
  const PARENT_HOST = response.parentHost;
  const WAS_GRANT_BUTTON_PRESSED = response.wasGrantButtonPressed;
  const IS_WHITELISTED = response.isWhitelisted;
  var wereAdsFound;

  if (PARENT_HOST) {
    var selector = SELECTORS[PARENT_HOST];
    selector =
        '#ad, .ad, .ad-container, .ad-top, .ads, .adv, .advertisement, .bottom-ad, .searchCenterBottomAds, .searchCenterTopAds, .side-ad'
            + (selector ? ', ' + selector : '');
    if (WAS_GRANT_BUTTON_PRESSED && PARENT_HOST == 'twitter.com')
        selector +=
            ', .css-1dbjc4n.r-my5ep6.r-qklmqi.r-1adg3ll > .css-1dbjc4n.r-1loqt21.r-o7ynqc.r-1j63xyz > [class="css-1dbjc4n"], [class="css-1dbjc4n r-e84r5y r-1or9b2r"], .css-1dbjc4n.r-my5ep6.r-qklmqi.r-1adg3ll > [class="css-1dbjc4n"], [aria-label="Who to follow"] [data-testid="UserCell"]:first-child, .css-1dbjc4n.r-my5ep6.r-qklmqi.r-1adg3ll > .css-1dbjc4n.r-1wtj0ep.r-1sp51qo, [class="css-1dbjc4n r-1jgb5lz r-1ye8kvj r-13qz1uu"] [data-testid="UserCell"]';

    if (selector) {
      if (!IS_WHITELISTED) {
        const STYLE = document.createElement('style');
        (document.head || document.documentElement).insertBefore(STYLE, null);
        populate(STYLE, selector);
      }

      onReady(function() {
        if (WAS_GRANT_BUTTON_PRESSED)
            if (PARENT_HOST == 'www.facebook.com') {
              const NEWS_FEED = document.getElementById('content');

              if (NEWS_FEED) {
                (new MutationObserver((mutations) => {
                  for (const MUTATION of mutations)
                      if (hideSponsoredPosts(MUTATION.target, IS_WHITELISTED))
                          wereAdsFound = true;
                })).observe(NEWS_FEED, {childList: true, subtree: true});

                if (hideSponsoredPosts(NEWS_FEED, IS_WHITELISTED))
                    wereAdsFound = true;
              }
            } else if (PARENT_HOST == 'twitter.com') {
              const TIMELINE = document.body;

              if (TIMELINE) {
                (new MutationObserver((mutations) => {
                  for (const MUTATION of mutations)
                      if (hidePromotedTweets(MUTATION.target, IS_WHITELISTED))
                          wereAdsFound = true;
                })).observe(TIMELINE, {childList: true, subtree: true});

                if (hidePromotedTweets(TIMELINE, IS_WHITELISTED))
                    wereAdsFound = true;
              }
            }

        if (document.querySelectorAll(selector).length) wereAdsFound = true;
      });
    }

    onReady(function() {
      const IFRAMES = document.getElementsByTagName('iframe');
      const IFRAME_COUNT = IFRAMES.length;

      for (var i = 0; i < IFRAME_COUNT; i++) {
        var iframe = IFRAMES[i];
        var childHost = getHost(iframe.src);

        if (childHost != PARENT_HOST)
            for (var j = DOMAIN_COUNT - 1; j + 1; j--)
                if (DOMAINS[j].test(childHost)) {
                  if (!IS_WHITELISTED) {
                    var className = iframe.className;
                    iframe.className =
                        (className ? className + ' ' : '') +
                            'adblockfast-collapsed';
                  }

                  break;
                }
      }

      const IMAGES = document.getElementsByTagName('img');
      const IMAGE_COUNT = IMAGES.length;

      for (i = 0; i < IMAGE_COUNT; i++) {
        var image = IMAGES[i];
        var childHost = getHost(image.src);

        if (childHost != PARENT_HOST)
            for (var j = DOMAIN_COUNT - 1; j + 1; j--)
                if (DOMAINS[j].test(childHost)) {
                  if (!IS_WHITELISTED) {
                    var className = image.className;
                    image.className =
                        (className ? className + ' ' : '') +
                            'adblockfast-collapsed';
                  }

                  break;
                }
      }
    });
  }

  onReady(function() { EXTENSION.sendRequest({wereAdsFound: wereAdsFound}); });
});
