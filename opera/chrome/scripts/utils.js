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
const isInOpera       = navigator.userAgent.indexOf('OPR') + 1;
const browser         = isInOpera ? 'opera' : 'chrome';
const domain          = `${ browser }.adblockfast.com`;
const baseUrl         = `https://${ domain }/`;
const script          = document.createElement('script');
const anchor          = document.createElement('a');
const deserializeData = (data) => { return typeof data == 'string' ? JSON.parse(data) : data; };
const getHost         = (url) => {
                          anchor.href = url;

                          return anchor.host;
                        };
const injectPlausible = (path) => {
                          script.src = `${ path }plausible.js`;

                          script.setAttribute(
                            'data-api', 'https://plausible.adblockfast.com/api/event'
                          );
                          script.setAttribute('data-domain', domain);
                          document.body.prepend(script);
                        };
const onPageReady     = (callback) => {
                          if (document.readyState == 'complete') {
                            callback();
                          } else {
                            addEventListener('load', callback);
                          }
                        };
      plausible       = (...args) => {
                          plausible.q = plausible.q || [];

                          plausible.q.push(args);
                        };
