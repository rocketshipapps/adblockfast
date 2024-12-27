#!/usr/bin/env node
console.log('Loading package dependencies ...');

import filesystem                                     from 'fs';
import config                                         from 'config';
import dotenv                                         from 'dotenv';
import { PlaywrightCrawler, ProxyConfiguration, log } from 'crawlee';

dotenv.config();

const settings      = config.get('settings');
const loginEmail    = encodeURIComponent(process.env.MASSIVE_LOGIN_EMAIL);
const apiToken      = encodeURIComponent(process.env.MASSIVE_API_TOKEN);
const args          = process.argv;
const candidateAds  = new Set();
const isDefined     = (variable) => { return typeof variable != 'undefined'; };
const isThirdParty  = (resourceUrl, documentUrl) => {
                        let condition;
                            resourceUrl = new URL(resourceUrl, documentUrl);

                        if (resourceUrl.href.startsWith('http')) {
                          try {
                            condition = resourceUrl.hostname != new URL(documentUrl).hostname;
                          } catch (error) {
                            condition = false;

                            console.error('URL parse failure:', error.message);
                          }
                        } else {
                          condition = false;
                        }

                        return condition;
                      };
let   frontierUrls;
let   urlCheckpoint;
let   urlCount;
let   maxUrlCount;

console.log('Loading crawl frontier ...');

process.on('unhandledRejection', (reason) => {
  console.error('Unhandled rejection:', reason);

  process.exit(1);
});

try {
  frontierUrls = filesystem.readFileSync(settings.frontierUrlsFilePath, 'utf-8').split('\n');
  maxUrlCount  = frontierUrls.length;
} catch (error) {
  console.error('URLs read failure:', error.message);

  process.exit(1);
}

console.log('Parsing command-line arguments ...');

if (args.length < 5) {
  const overrideCheckpoint = args[2];
  const overrideCount      = args[3];

  if (isDefined(overrideCheckpoint) && (
    isNaN(overrideCheckpoint) || overrideCheckpoint < 0 || overrideCheckpoint >= maxUrlCount
  )) {
    console.error(
      'URL checkpoint must be number greater than or equal to 0 and less than', maxUrlCount
    );

    process.exit(1);
  }

  if (isDefined(overrideCount) && (
    isNaN(overrideCount) || overrideCount <= 0 || overrideCount > maxUrlCount
  )) {
    console.error('URL count must be number greater than 0 and less than or equal to', maxUrlCount);

    process.exit(1);
  }

  urlCheckpoint = overrideCheckpoint ? overrideCheckpoint * 1 : 0;
  urlCount      = overrideCount ? overrideCount * 1 : maxUrlCount;
} else {
  console.error(`Usage: node crawler.js [checkpoint=0] [url_count=${ maxUrlCount }]`);

  process.exit(1);
}

(async () => {
  const proxyConfiguration = new ProxyConfiguration({
                               proxyUrls: [ 'http://' + loginEmail
                                                      + ':'
                                                      + apiToken
                                                      + '@'
                                                      + settings.networkUrl ]
                             });

  log.setLevel(log.LEVELS.OFF);
  await new PlaywrightCrawler({
    proxyConfiguration,
    async requestHandler({ page, request, enqueueLinks }) {
      const documentUrl = request.url;
      const crawlDepth  = request.userData.crawlDepth || 0;

      console.log('Fetching URL', documentUrl, 'at depth', crawlDepth, '...');

      (await page.evaluate((resources) => {
        const urls = [];

        resources.forEach(({ element, attribute }) => {
          document.querySelectorAll(element).forEach((resource) => {
            const url = resource.getAttribute(attribute);

            if (url) urls.push(url);
          });
        });

        return urls;
      }, settings.adResources)).forEach((url) => {
        if (isThirdParty(url, documentUrl)) candidateAds.add(`${ url } (${ documentUrl })`);
      });

      if (crawlDepth < settings.maxCrawlDepth) {
        await enqueueLinks({
                          selector: 'a[href]',
                             globs: [ 'http{,s}://**/*' ],
                          strategy: 'all',
          transformRequestFunction: (requestOptions) => {
                                      return {
                                        ...requestOptions,
                                                 userData: { crawlDepth: crawlDepth + 1 }
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

  filesystem.appendFile(
    settings.candidateAdsFilePath, Array.from(candidateAds).join('\n'), (error) => {
      if (error) console.error('Ads write failure:', error.message);
    }
  );
})().catch((error) => {
  console.error('Web crawl failure:', error.message);
});
