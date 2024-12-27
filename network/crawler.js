#!/usr/bin/env node
console.log('Loading package dependencies ...');

import filesystem                                     from 'fs';
import dotenv                                         from 'dotenv';
import { PlaywrightCrawler, ProxyConfiguration, log } from 'crawlee';

dotenv.config();

console.log('Loading crawl frontier ...');

const urlsFilePath  = 'urls.txt';
const adsFilePath   = 'ads.txt';
const networkUrl    = 'network.joinmassive.com:65534';
const crawlDepth    = 3;
const adResources   = [{
                          element: 'img',
                        attribute: 'src'
                      }, {
                          element: 'audio',
                        attribute: 'src'
                      }, {
                          element: 'video',
                        attribute: 'src'
                      }, {
                          element: 'object',
                        attribute: 'data'
                      }, {
                          element: 'embed',
                        attribute: 'src'
                      }, {
                          element: 'iframe',
                        attribute: 'src'
                      }, {
                          element: 'link',
                        attribute: 'href'
                      }, {
                          element: 'script',
                        attribute: 'src'
                      }];
const loginEmail    = encodeURIComponent(process.env.MASSIVE_LOGIN_EMAIL);
const apiToken      = encodeURIComponent(process.env.MASSIVE_API_TOKEN);
const args          = process.argv;
const candidateAds  = new Set();
const isDefined     = (variable) => { return typeof variable != 'undefined'; };
const isThirdParty  = (resourceUrl, documentUrl) => {
                        let isThirdParty;
                            resourceUrl  = new URL(resourceUrl, documentUrl);

                        if (resourceUrl.href.startsWith('http')) {
                          try {
                            isThirdParty = resourceUrl.hostname != new URL(documentUrl).hostname;
                          } catch (error) {
                            isThirdParty = false;

                            console.error('URL parse failure:', error.message);
                          }
                        } else {
                          isThirdParty = false;
                        }

                        return isThirdParty;
                      };
let   frontierUrls;
let   urlCheckpoint;
let   urlCount;

process.on('unhandledRejection', (reason) => {
  console.error('Unhandled rejection:', reason);

  process.exit(1);
});

try {
  frontierUrls = filesystem.readFileSync(urlsFilePath, 'utf-8').split('\n');
} catch (error) {
  console.error('URLs read failure:', error.message);

  process.exit(1);
}

console.log('Parsing command-line arguments ...');

if (args.length < 5) {
  const overrideUrlCheckpoint = args[2];
  const overrideUrlCount      = args[3];
  const maxUrlCount           = frontierUrls.length;

  if (isDefined(overrideUrlCheckpoint) && (isNaN(overrideUrlCheckpoint) || overrideUrlCheckpoint
                                                                         < 0
                                                                        || overrideUrlCheckpoint
                                                                        >= maxUrlCount)) {
    console.error(
      'URL checkpoint must be number greater than or equal to 0 and less than', maxUrlCount
    );

    process.exit(1);
  }

  if (isDefined(overrideUrlCount) && (isNaN(overrideUrlCount) || overrideUrlCount <= 0
                                                              || overrideUrlCount  > maxUrlCount)) {
    console.error('URL count must be number greater than 0 and less than or equal to', maxUrlCount);

    process.exit(1);
  }

  urlCheckpoint = overrideUrlCheckpoint ? overrideUrlCheckpoint * 1 : 0;
  urlCount      = overrideUrlCount      ? overrideUrlCount * 1      : maxUrlCount;
} else {
  console.error('Usage: node crawler.js [checkpoint=0] [url_count]');

  process.exit(1);
}

(async () => {
  const proxyConfiguration = new ProxyConfiguration({
                               proxyUrls: [ `http://${ loginEmail }:${ apiToken }@${ networkUrl }` ]
                             });

  log.setLevel(log.LEVELS.OFF);
  await new PlaywrightCrawler({
    proxyConfiguration,
    async requestHandler({ page, request, enqueueLinks }) {
      const documentUrl  = request.url;
      const currentDepth = request.userData.currentDepth || 0;

      console.log('Fetching URL', documentUrl, 'at depth', currentDepth, '...');

      (await page.evaluate((resources) => {
        const urls = [];

        resources.forEach(({ element, attribute }) => {
          document.querySelectorAll(element).forEach((resource) => {
            const url = resource.getAttribute(attribute);

            if (url) urls.push(url);
          });
        });

        return urls;
      }, adResources)).forEach((url) => {
        if (isThirdParty(url, documentUrl)) candidateAds.add(`${ url } (${ documentUrl })`);
      });

      if (currentDepth < crawlDepth) {
        await enqueueLinks({
                             globs: [ 'http{,s}://**/*' ],
                          strategy: 'all',
          transformRequestFunction: (requestOptions) => {
                                      return {
                                        ...requestOptions,
                                                 userData: { currentDepth: currentDepth + 1 }
                                      };
                                    }
        });
      }
    },
    failedRequestHandler({ request }) {
      console.error(request.url, 'crawl failure:', request.errorMessages.at(-1).split('\n')[0]);
    }
  }).run(frontierUrls.slice(urlCheckpoint, urlCheckpoint + urlCount));

  console.log('Saving candidate ads ...');

  filesystem.appendFile(adsFilePath, Array.from(candidateAds).join('\n'), (error) => {
    if (error) console.error('Ads write failure:', error.message);
  });
})().catch((error) => {
  console.error('Crawler execution failure:', error.message);
});
