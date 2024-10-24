# ![Adblock Fast](assets/promo/adblockfast-optimized.png) [![Tweet](assets/promo/tweet-optimized.png)](https://twitter.com/intent/tweet?text=Get%20a%20faster%20ad%20blocker%20for%20Windows,%20Android,%20iOS,%20or%20Chrome:&url=https://adblockfast.com/?ref=GitHub&via=adblockfast)

*A faster ad blocker for Windows, Android, iOS, [and] Chrome<del>, and Opera</del>!*

Try Adblock Fast:

* [Windows](https://desktop.adblockfast.com/?ref=GitHub) (Windows 7 and up)
* [Android](https://play.google.com/store/apps/details?id=com.rocketshipapps.adblockfast) (Android
  5.0 and up with Samsung Internet 4.0 and up)
* [iOS](https://itunes.apple.com/app/adblock-fast/id1032930802?at=1001lwfP) (iOS 9 and up on 64-bit
  devices, iPhone 5s and up and iPad mini 2 and up)
* [Chrome](https://chrome.google.com/webstore/detail/adblock-fast/nneejgmhfoecfeoffakdnolopppbbkfi)
* ~~Opera~~

Just as webpages grew bloated with ads, so too have ad blockers grown bloated with little-used
filtering rules and features that sap their speed and hog your computer or device’s CPU, memory, and
storage. Adblock Fast executes a mere 12 optimized filtering rules to accelerate pages 8x more but
consume 6x less computing resources than other ad blockers do.

Exactly how much faster, you ask, is Adblock Fast<em>?</em> Let’s crunch some numbers.

![Benchmarks](assets/promo/benchmarks-optimized.png)

### 1. Adblock Fast executes 5,000x fewer filtering rules per page.[<sup>1</sup>](#benchmark-notes)

* AdBlock: 65,316 rules
* Adblock Plus: 65,316 rules
* **Adblock Fast: 12 rules**

I.e., the app runs faster.

### 2. Adblock Fast consumes 7x fewer kilobytes of storage.[<sup>2</sup>](#benchmark-notes)

* AdBlock: 843 KB
* Adblock Plus: 543 KB
* **Adblock Fast: 97 KB**

I.e., the app installs faster.

### 3. Adblock Fast accelerates page loading by 8x as many seconds.[<sup>3</sup>](#benchmark-notes)

* No ad blocker: 3.17s (control)
* AdBlock: 2.84s (0.33s faster)
* Adblock Plus: 3.23s (0.06s slower)
* **Adblock Fast: 2.10s (1.07s faster)**

I.e., pages load faster.

### 4. Adblock Fast consumes a 3x lower percentage of CPU.[<sup>4</sup>](#benchmark-notes)

* AdBlock: 19.4%
* Adblock Plus: 20.3%
* **Adblock Fast: 6.6%**

I.e., your computer or device runs faster.

### 5. Adblock Fast consumes 3x fewer megabytes of memory.[<sup>5</sup>](#benchmark-notes)

* AdBlock: 162.5 MB
* Adblock Plus: 158.0 MB
* **Adblock Fast: 58.5 MB**

I.e., your computer or device runs faster still.

#### Benchmark notes

1. Rule counts were retrieved from [EasyList](https://easylist.to/) and
   [Adblock Fast’s ruleset](opera/chrome/scripts/blocking.js) on December 3rd, 2023.
2. File sizes were retrieved from each app’s listing in the
   [Chrome Web Store](https://chromewebstore.google.com/) on September 1st, 2015.
3. Load times were measured in Chrome on September 1st, 2015 by (1) enabling each app, (2) clearing
   the browser’s cache before each trial, (3) loading the homepage of each of
   [Alexa’s top 10 news sites](https://web.archive.org/web/20150902062936/https://www.alexa.com/topsites/category/Top/News)
   3 times, (4) recording the elapsed time shown by the
   [browser’s developer tools](https://developer.chrome.com/docs/devtools/) when the `load` event
   fired, and (5) calculating the mean time for each app.
4. CPU use was measured in Chrome on September 1st, 2015 by (1) enabling each app, (2) clearing the
   browser’s cache before each trial, (3) loading the homepage of each of
   [Alexa’s top 10 news sites](https://web.archive.org/web/20150902062936/https://www.alexa.com/topsites/category/Top/News)
   3 times, (4) recording the peak **CPU** percentage shown by the
   [browser’s task manager](https://developer.chrome.com/docs/devtools/memory-problems/), and (5)
   calculating the mean percentage for each app.
5. Memory use was measured in Chrome on September 1st, 2015 by (1) enabling each app, (2) clearing
   the browser’s cache before each trial, (3) loading the homepage of each of
   [Alexa’s top 10 news sites](https://web.archive.org/web/20150902062936/https://www.alexa.com/topsites/category/Top/News)
   3 times, (4) recording the peak **Memory** size shown by the
   [browser’s task manager](https://developer.chrome.com/docs/devtools/memory-problems/), and (5)
   calculating the mean size for each app.

See the
[raw benchmark data](https://docs.google.com/spreadsheets/u/1/d/1ve_1zzTuwSRy8FMlZJZ8PHu3wZp7ApegMt6fyRLv19U/pubhtml).

## Owner’s manual

Adblock Fast is so fast that even reading the manual takes no time flat. Here’s everything you need
to know.

### In Android

Adblock Fast blocks ads in the Samsung Internet browser 4.0 and up (not in other
apps). To enable Adblock Fast to block in Samsung Internet:

1. press the Home button on your Android device,
2. tap the **Internet** app,
3. tap **⋮** (the toolbar button) > **Extensions** > **Content blockers**, then
4. toggle the **Adblock Fast** switch on.

To unblock ads, go to the **Adblock Fast** app.

### In iOS

Adblock Fast blocks ads in the Safari browser (not in other apps). To enable
Adblock Fast to block in Safari:

1. press the Home button on your iPhone or iPad,
2. tap the **Settings** app,
3. tap **Safari** > (under **<sub><sup>GENERAL</sup></sub>**) **Content
   Blockers**, then
4. toggle the **Adblock Fast** switch on.

To unblock ads, go to the **Adblock Fast** app.

To see the effect of Adblock Fast on a particular page, you may have to clear
Safari’s cache (tap **Settings** > **Safari** > **Clear History and Website
Data**) before reloading the page.

### In Chrome ~~or Opera~~

Adblock Fast installs a button in your browser’s toolbar. To show the button:

1. click the extensions (puzzle) button in your toolbar then
2. click the pin button next to the extension listing.

The button indicates the status of the site and page you’re on:

![Blocked ads](opera/chrome/images/blocked-ads.png) Ads are being blocked on the site and ads were
found on the page.

![Blocked](opera/chrome/images/blocked.png) Ads are being blocked on the site, but no ads were found
on the page.

![Unblocked ads](opera/chrome/images/unblocked-ads.png) Ads aren’t being blocked on the site, but
ads were found on the page.

![Unblocked](opera/chrome/images/unblocked.png) Ads aren’t being blocked on the site and no ads were
found on the page.

If Adblock Fast is interfering with the behavior of the page, you can click the button to unblock
ads. Your choice will be remembered whenever you go back to the site.

If you have any questions or comments, you can follow and message
[us on Facebook](https://www.facebook.com/adblockfast) or
[on Twitter](https://twitter.com/adblockfast).

## (In)frequently asked questions

### Who created Adblock Fast<em>?</em>

Adblock Fast was created and is maintained by [Rocketship](https://rocketshipapps.com/), an
award-winning app studio whose mission is to design and develop the finest desktop, mobile, and web
experiences in the universe.

### What is Adblock Fast<em>?</em>

Adblock Fast is a faster ad blocker for desktop computers and mobile browsers.

### When will Adblock Fast be available on [insert platform here]<em>?</em>

Adblock Fast is available for Windows 7 and up, Android 5.0 and up with Samsung Internet 4.0 and up,
iOS 9 and up on 64-bit devices (iPhone 5s and up and iPad mini 2 and up), and the Chrome ~~and
Opera~~ desktop browser~~s~~. Follow [us on Facebook](https://www.facebook.com/adblockfast) or
[on Twitter](https://twitter.com/adblockfast) for news about additional platform availability.

### Where does Adblock Fast’s filtering ruleset come from<em>?</em>

Adblock Fast’s ruleset is derived from [EasyList](https://easylist.to/) and that of
[Bluhell Firewall](https://web.archive.org/web/20170909224431/https://addons.mozilla.org/en-US/firefox/addon/bluhell-firewall/).

### Why does Adblock Fast require [insert permission here] at install time<em>?</em>

Like all (functional) ad blockers, Adblock Fast prompts you for the permissions required to block ad
requests by intercepting HTTP traffic and to hide ad elements by injecting CSS and JavaScript into
visited pages.

### How do Adblock Fast’s mobile blocking modes work<em>?</em>

Adblock Fast for mobile lets you choose your blocking mode. If you tap the **Fast** button or link
(or have previously installed the app), our default filtering rules are activated. If you tap the
**Faster** button or link, a more comprehensive, up-to-date set of filtering rules are activated;
Faster is an experimental mode that uses what should be an unnoticeable amount of your background
bandwidth to anonymously crawl [common websites](network/urls.txt) (no cookies or any other personal
info are used) and help detect new ad domains to filter.

### How can Adblock Fast for Windows be uninstalled<em>?</em>

Open the Start menu, go to **Settings** > **Apps** > **Apps & features**, choose the
**Adblock Fast** app, then press the **Uninstall** button.

## Contributing

Adblock Fast is open for pull-request business. Follow the steps below to get started.

### For Android

1.  [Fork this repository](https://github.com/rocketshipapps/adblockfast/fork).
2.  Go to your working directory of choice.
3.  Clone the forked repo:

    ```shell
    $ git clone https://github.com/[username]/adblockfast
    ```

4.  Go to the Android Studio IDE.
5.  Go to **File** > **Open…** .
6.  Under your working directory, select the `android` subdirectory.
7.  Go to **Run** > **Run 'app'**.
8.  To test after you make a change, go to **Run** > **Stop 'app'** then **Run** > **Run 'app'**.
9.  To create a release build, go to **Build** > **Generate Signed Bundle / APK…** then follow the
    further steps in the dialog box.
10. Push your changes.
11. Send us pull requests<em>!</em>

### For iOS

1. Fork this repository.
2. Navigate to your working directory of choice.
3. Clone the repo:

        git clone https://github.com/[insert username here]/adblockfast.git

4. Go to the Xcode IDE.
5. Go to **File** > **Open…** .
6. Under your working directory, select the `ios` directory.
7. Go to **Product** > **Run**.
8. To test after you make a change, go to **Product** > **Stop** then
   **Product** > **Run**.
9. Push your changes.
10. Send us pull requests<em>!</em>

### For Chrome

1.  [Fork this repository](https://github.com/rocketshipapps/adblockfast/fork).
2.  Go to your working directory of choice.
3.  Clone the forked repo:

    ```shell
    $ git clone https://github.com/[username]/adblockfast
    ```

4.  Install the build dependency:

    ```shell
    $ sudo npm i -g terser
    ```

5.  Go to the Chrome browser.
6.  Go to **Window** > **Extensions**.
7.  Toggle the **Developer mode** switch on then press the **Load unpacked** button.
8.  Under your working and the `opera` directories, select the `chrome` subdirectory.
9.  To test after you make a change, press **↻** (the reload button) under the extension listing.
10. To create a release build, switch to the repo directory then run the build script:

    ```shell
    $ cd adblockfast
    $ ./build.sh
    ```

11. Push your changes.
12. Send us pull requests<em>!</em>

### For Opera

1.  [Fork this repository](https://github.com/rocketshipapps/adblockfast/fork).
2.  Go to your working directory of choice.
3.  Clone the forked repo:

    ```shell
    $ git clone https://github.com/[username]/adblockfast
    ```

4.  Install the build dependency:

    ```shell
    $ sudo npm i -g terser
    ```

5.  Go to the Opera browser.
6.  Go to **View** > **Show Extensions**.
7.  Toggle the **Developer mode** switch on then press the **Load unpacked** button.
8.  Under your working directory, select the `opera` subdirectory.
9.  To test after you make a change, press **↻** (the reload button) under the extension listing.
10. To create a release build, switch to the repo directory then run the build script:

    ```shell
    $ cd adblockfast
    $ ./build.sh
    ```

11. Push your changes.
12. Send us pull requests<em>!</em>

## License

Copyright 2015– Rocketship Apps, LLC.

This program is free software, excluding the brand features and third-party portions of the program
identified in the [Exceptions](#exceptions) below: you can redistribute it and/or modify it under
the terms of the GNU General Public License as published by the Free Software Foundation, either
version 3 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
[GNU General Public License](https://www.gnu.org/licenses/gpl-3.0.html) for more details.

## Additional permissions for App Store submission

Provided that you are otherwise in compliance with version 3 or a later version of the GNU General
Public License for each covered work that you convey (including, without limitation, making the
Corresponding Source available in compliance with section 6 of the License), you are also granted
the permission to convey, through Apple’s App Store, non-source executable versions of this program
as incorporated into each applicable covered work as executable versions only under
[version 2.0 of the Mozilla Public License](https://www.mozilla.org/en-US/MPL/2.0/).

## Exceptions

The Adblock Fast and Rocketship logos, trademarks, domain names, and other brand features used in
this program cannot be reused without permission and no license is granted thereto.

Further, the following third-party portions of the program and any use thereof are subject to their
own licensing terms:

* [Hudson NY](https://www.myfonts.com/collections/hudson-ny-font-virtuecreative)
* [Avenir Next](https://www.linotype.com/2090/avenir-next.html)
