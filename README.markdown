# ![Adblock Fast](assets/promo/adblockfast.png)

*A new, faster ad blocker for iOS, Android, Chrome, and Opera.*

Try Adblock Fast:

* [iOS](https://itunes.apple.com/app/adblock-fast/id1032930802) (iOS 9 and up on
  64-bit devices, iPhone 5s and up and iPad mini 2 and up)
* [Android](https://play.google.com/store/apps/details?id=com.rocketshipapps.adblockfast)
  (Android 5.0 and up with Samsung Internet 4.0 and up)
* [Chrome](https://chrome.google.com/webstore/detail/adblock-fast/nneejgmhfoecfeoffakdnolopppbbkfi)
* [Opera](https://addons.opera.com/en/extensions/details/adblock-fast/)

Just as webpages grew bloated with ads, so too have ad blockers grown bloated
with little-used filtering rules and features that sap their speed and hog your
computer or device’s disk space, CPU cycles, and memory. Adblock Fast runs a
mere 7 optimized filtering rules to accelerate pages 8x more but consume 6x less
system resources than other ad blockers do.

Exactly how much faster, you ask, is Adblock Fast<em>?</em> Let’s crunch some
numbers.

![Benchmarks](assets/promo/benchmarks-optimized.png)

### 1. Adblock Fast runs 7,000x fewer filtering rules per page.[<sup>1</sup>](#benchmark-notes)

* AdBlock: 49,002 rules
* Adblock Plus: 49,002 rules
* **Adblock Fast: 7 rules**

I.e., the app is more efficient.

### 2. Adblock Fast consumes 7x less kilobytes of disk.[<sup>2</sup>](#benchmark-notes)

* AdBlock: 843 KB
* Adblock Plus: 543 KB
* **Adblock Fast: 97 KB**

I.e., the app installs faster.

### 3. Adblock Fast accelerates page loading 8x as many seconds.[<sup>3</sup>](#benchmark-notes)

* No ad blocker: 3.17s (control)
* AdBlock: 2.84s (0.33s faster)
* Adblock Plus: 3.23s (0.06s slower)
* **Adblock Fast: 2.10s (1.07s faster)**

I.e., pages load faster.

### 4. Adblock Fast consumes a 3x lower percentage of CPU.[<sup>4</sup>](#benchmark-notes)

* AdBlock: 19.4%
* Adblock Plus: 20.3%
* **Adblock Fast: 6.6%**

I.e., other apps run faster.

### 5. Adblock Fast consumes 3x less megabytes of memory.[<sup>5</sup>](#benchmark-notes)

* AdBlock: 162.5 MB
* Adblock Plus: 158.0 MB
* **Adblock Fast: 58.5 MB**

I.e., other apps run faster still.

#### Benchmark notes

1. Rule counts were retrieved from
   [EasyList](https://easylist.github.io/) and
   [Adblock Fast’s ruleset](opera/chrome/scripts/blocking.js) on September 1st,
   2015.
2. Filesizes were retrieved from each browser extension’s listing in the
   [Chrome Web Store](https://chrome.google.com/webstore) on September 1st,
   2015.
3. Load times were measured in the Chrome browser on September 1st, 2015 by (1)
   enabling each extension, (2) clearing the browser’s cache before each trial,
   (3) loading the homepage of each of
   [Alexa’s top 10 news websites](http://www.alexa.com/topsites/category/Top/News)
   over three trials, (4) recording the elapsed time shown by the
   [Developer Tools](https://developer.chrome.com/devtools) when the `load`
   event fired, and (5) calculating the mean time for each extension.
4. CPU use was measured in the Chrome browser on September 1st, 2015 by (1)
   enabling each extension, (2) clearing the browser’s cache before each trial,
   (3) loading the homepage of each of
   [Alexa’s top 10 news websites](http://www.alexa.com/topsites/category/Top/News)
   over three trials, (4) recording the peak **CPU** percentage shown by the
   [Task Manager](https://developer.chrome.com/devtools/docs/javascript-memory-profiling),
   and (5) calculating the mean percentage for each extension.
5. Memory use was measured in the Chrome browser on September 1st, 2015 by (1)
   enabling each extension, (2) clearing the browser’s cache before each trial,
   (3) loading the homepage of each of
   [Alexa’s top 10 news websites](http://www.alexa.com/topsites/category/Top/News)
   over three trials, (4) recording the peak **Memory** size shown by the
   [Task Manager](https://developer.chrome.com/devtools/docs/javascript-memory-profiling),
   and (5) calculating the mean size for each extension.

See the
[raw benchmark data](https://docs.google.com/spreadsheets/u/1/d/1ve_1zzTuwSRy8FMlZJZ8PHu3wZp7ApegMt6fyRLv19U/pubhtml).

## Owner’s manual

Adblock Fast is so fast that even reading the manual takes no time flat. Here’s
everything you need to know.

### In iOS

Adblock Fast blocks ads in Safari not in other apps. To allow Adblock Fast to
block in Safari:

1. press the Home button on your iPhone or iPad,
2. tap the **Settings** app,
3. tap **Safari** > (under <small>GENERAL</small>) **Content Blockers**, then
4. toggle the **Adblock Fast** switch on.

To unblock ads, navigate to the **Adblock Fast** app.

To see the effect of Adblock Fast on a particular page, you may have to clear
Safari’s cache (tap **Settings** > **Safari** > **Clear History and Website
Data**) before reloading the page.

### In Chrome or Opera

Adblock Fast installs a button in your browser’s toolbar. The button indicates
the status of the site and page you’re on:

![Blocked ads](opera/chrome/images/blocked-ads.png) Ads are being blocked on the
site and ads were found on the page.

![Blocked](opera/chrome/images/blocked.png) Ads are being blocked on the site,
but no ads were found on the page.

![Unblocked ads](opera/chrome/images/unblocked-ads.png) Ads aren’t being blocked
on the site, but ads were found on the page.

![Unblocked](opera/chrome/images/unblocked.png) Ads aren’t being blocked on the
site and no ads were found on the page.

If Adblock Fast is interfering with the behavior of the page (or if you’re a
masochist), you can click the button to unblock ads. Your setting will be
remembered whenever you go back to the site.

## Questions (possibly to be asked frequently)

### Who created Adblock Fast<em>?</em>

Adblock Fast was created and is maintained by
[Rocketship](https://rocketshipapps.com/), an award-winning app studio based in
Palo Alto and San Francisco. Our mission is to design and develop the finest
mobile and web experiences, of our own and for clients.

### What is Adblock Fast<em>?</em>

Adblock Fast is a new, faster ad blocker for desktop browsers and mobile
devices.

### Where is Adblock Fast’s filtering ruleset from<em>?</em>

Adblock Fast’s ruleset is derived from
[EasyList](https://easylist.github.io/) and that of
[Bluhell Firewall](https://addons.mozilla.org/en-US/firefox/addon/bluhell-firewall/).
We’re also testing an alternative ruleset that we expect to improve ad blocking
yet another order of magnitude.

### When will Adblock Fast be available on [insert platform here]<em>?</em>

Adblock Fast is available for the Chrome and Opera desktop browsers, for iOS 9
and up (on 64-bit devices, iPhone 5s and up and iPad mini 2 and up), and for
Android 5.0 and up (with Samsung Internet 4.0 and up). Follow
[us on Facebook](https://www.facebook.com/adblockfast) or
[on Twitter](https://twitter.com/adblockfast) for news about additional platform
availability.

### Why does Adblock Fast require [insert permission here] at install time<em>?</em>

Like all (functional) ad blockers, Adblock Fast prompts you for the permissions
required to block ad requests by intercepting your HTTP traffic and to hide ad
resources by injecting CSS into the webpages you go to.

### How does Adblock Fast make money<em>?</em>

Adblock Fast doesn’t, nor do we intend for Adblock Fast to ever, make any money.
We operate an ([aforementioned](#who-created-adblock-fast)) app studio that’s
bootstrapped and quite profitable. Yesterday, you probably didn’t know we
existed. Today, you do and, someday, perhaps you’ll be creating the next killer
app and will consider hiring us. (Hmm, maybe doing good work and shipping good
products ought to replace advertising<em>!</em>) Unlike
[other ad blockers](https://news.ycombinator.com/item?id=5995140), we don’t have
to sell out to support our project.

## Contributing (and so can you<em>!</em>)

Adblock Fast is open for pull-request business<em>!</em> Follow the steps below
to get started.

### In iOS

1. Fork this repository.
2. Switch to your working directory of choice.
3. Clone the repo locally:

        git clone https://github.com/rocketshipapps/adblockfast.git

4. Open or switch to Xcode.
5. Go to **File** > **Open…** .
6. Find your working directory.
7. Select `ios`.
8. Go to **Product** > **Run**.
9. To test after you make a change, go to **Product** > **Stop** then
   **Product** > **Run**.
10. Push your changes.
11. Send us pull requests<em>!</em>

### In Chrome

1. Fork this repository.
2. Switch to your working directory of choice.
3. Clone the repo locally:

        git clone https://github.com/rocketshipapps/adblockfast.git

4. Open or switch to Chrome.
5. Go to **Window** > **Extensions**.
6. Check the **Developer mode** box then press the **Load unpacked extension…**
   button.
7. Find your working directory.
8. Under `opera`, select `chrome`.
9. To test after you make a change, under the extension listing, press the
   **Reload** link.
10. Push your changes.
11. Send us pull requests<em>!</em>

### In Opera

1. Fork this repository.
2. Switch to your working directory of choice.
3. Clone the repo locally:

        git clone https://github.com/rocketshipapps/adblockfast.git

4. Open or switch to Opera.
5. Go to **View** > **Show Extensions**.
6. Press the **Developer Mode** button then the **Load Unpacked Extension…**
   button.
7. Find your working directory.
8. Select `opera`.
9. To test after you make a change, under the extension listing, press the
   **Reload** button.
10. Push your changes.
11. Send us pull requests<em>!</em>

## License

Copyright 2015, 2016 Rocketship Apps, LLC

This program is free software, excluding the brand features and third-party
portions of the program identified in the [Exceptions](#exceptions) below: you
can redistribute it and/or modify it under the terms of the GNU General Public
License as published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE. See the
[GNU General Public License](https://www.gnu.org/licenses/gpl.html) for more
details.

## Additional permissions for App Store submission

Provided that you are otherwise in compliance with version 3 or a later version
of the GNU General Public License for each covered work that you convey
(including, without limitation, making the Corresponding Source available in
compliance with section 6 of the License), you are also granted the permission
to convey, through Apple’s App Store, non-source executable versions of this
program as incorporated into each applicable covered work as executable versions
only under
[version 2.0 of the Mozilla Public License](https://www.mozilla.org/en-US/MPL/2.0/).

## Exceptions

The Adblock Fast and Rocketship logos, trademarks, domain names, and other brand
features used in this program cannot be reused without permission and no license
is granted thereto.

Further, the following third-party portions of the program and any use thereof
are subject to their own license terms:

* [Hudson NY](https://www.myfonts.com/fonts/virtuecreative/hudson-ny/)
* [Avenir Next](https://www.linotype.com/2090/avenir-next.html)
