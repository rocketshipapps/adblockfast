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
function injectPlausible(path) {
  SCRIPT.src = path + 'plausible.js';
  SCRIPT.setAttribute('data-api', 'https://plausible.io/api/event');
  SCRIPT.setAttribute('data-domain', DOMAIN);
  document.body.prepend(SCRIPT);
}

function plausible() {
  plausible.q = plausible.q || [];
  plausible.q.push(arguments);
}

function deserialize(object) { return typeof object == 'string' ? JSON.parse(object) : object; }

function onReady(callback) {
  if (document.readyState == 'complete') {
    callback();
  } else {
    addEventListener('load', callback);
  }
}

function getHost(url) {
  ANCHOR.href = url;

  return ANCHOR.host;
}

const IS_IN_OPERA = navigator.userAgent.indexOf('OPR') + 1;
const BROWSER = IS_IN_OPERA ? 'opera' : 'chrome';
const DOMAIN = BROWSER + '.adblockfast.com';
const BASE_URL = 'https://' + DOMAIN + '/';
const SCRIPT = document.createElement('script');
const ANCHOR = document.createElement('a');
