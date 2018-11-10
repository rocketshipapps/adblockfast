#!/bin/bash
# Copyright 2015–2018 Rocketship <http://rocketshipapps.com/>
#
# This program is free software: you can redistribute it and/or modify it under
# the terms of the GNU General Public License as published by the Free Software
# Foundation, either version 3 of the License, or (at your option) any later
# version.
#
# This program is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License along with
# this program. If not, see <http://www.gnu.org/licenses/>.
#
# Authors (one per line):
#
#   Brian Kennish <brian@rocketshipapps.com>
emcc -O3 \
     -s WASM=1 \
     -s SIDE_MODULE=1 \
     -s EXPORTED_FUNCTIONS="[\"_rule\", \"_rule_length\"]" \
     -o opera/chrome/premium/youtube.wasm \
     opera/chrome/premium/youtube.c
cd builds
rm -fR adblockfast
cp -R ../opera adblockfast
java -jar htmlcompressor-1.5.3.jar --remove-intertag-spaces \
                                   --remove-quotes \
                                   -o adblockfast/chrome/markup/ \
                                   adblockfast/chrome/markup/
java -jar yuicompressor-2.4.8.jar -o ".css$:.css" \
                                  adblockfast/chrome/stylesheets/*.css
java -jar yuicompressor-2.4.8.jar -o ".js$:.js" adblockfast/chrome/scripts/*.js
cd adblockfast
rm -f chrome/premium/youtube.c
zip -r ../adblockfast chrome -x *.DS_Store
rm -f chrome/manifest.json
