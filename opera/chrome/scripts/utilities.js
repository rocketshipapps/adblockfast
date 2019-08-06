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
  this program. If not, see <http://www.gnu.org/licenses/>.

  Authors (one per line):

    Brian Kennish <brian@rocketshipapps.com>
*/
function deserialize(object) {
  return typeof object == 'string' ? JSON.parse(object) : object;
}

function onReady(callback) {
  if (document.readyState == 'complete') callback();
  else addEventListener('load', callback);
}

function getHost(url) {
  ANCHOR.href = url;
  return ANCHOR.host;
}

const EXTENSION = chrome.extension;
const BROWSER_ACTION = chrome.browserAction;
const ANCHOR = document.createElement('a');
