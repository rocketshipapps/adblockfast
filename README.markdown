# ![Adblock Fast](assets/promo/adblockfast.png)

*The world’s fastest ad blocker! Same ad blocking, 8x more accelerated webpage
loading.*

Adblock Fast is available for:

* [Chrome](https://chrome.google.com/webstore/detail/ab-fast/nneejgmhfoecfeoffakdnolopppbbkfi)
* [Opera](https://addons.opera.com/en/extensions/details/adblock-fast/)
* [iOS 9](https://itunes.apple.com/us/app/adblock-fast/id1032930802) (64-bit
  devices, iPhone 5s and up and iPad mini 2 and up)

Just as webpages grew bloated with ads, so too have ad blockers grown bloated
with little-used filtering rules and features that sap their speed and hog your
computer or device’s disk space, CPU cycles, and memory. Adblock Fast runs a
mere 7 optimized filtering rules to accelerate pages 8x more but consume 6x less
system resources than other ad blockers do.

Exactly how much faster, you ask, is Adblock Fast<em>?</em> Let’s crunch some
numbers.

![Benchmarks](assets/promo/benchmarks-optimized.png)

### 1. Adblock Fast runs 7,000x fewer filtering rules per page.

* AdBlock: 49,002 rules
* Adblock Plus: 49,002 rules
* **Adblock Fast: 7 rules**

I.e., the app is more efficient.

### 2. Adblock Fast consumes 7x less kilobytes of disk.

* AdBlock: Size: 843 KB
* Adblock Plus: Size: 543 KB
* **Adblock Fast: 97 KB**

I.e., the app installs faster.

### 3. Adblock Fast accelerates page loading 8x as many seconds.

* No ad blocker: 3.17s (control)
* AdBlock: 2.84s (0.33s faster)
* Adblock Plus: 3.23s (0.06s slower)
* **Adblock Fast: 2.10s (1.07s faster)**

I.e., pages load faster.

### 4. Adblock Fast consumes a 3x lower percentage of CPU.

* AdBlock: 19.4%
* Adblock Plus: 20.3%
* **Adblock Fast: 6.6%**

I.e., other apps run faster.

### 5. Adblock Fast consumes 3x less megabytes of memory.

* AdBlock: 162.5 MB
* Adblock Plus: 158.0 MB
* **Adblock Fast: 58.5 MB**

I.e., other apps run faster still.

## Owner’s manual

Adblock Fast is so fast that even reading the effing manual takes no time flat.
Here’s everything you need to know.

### In Chrome or Opera

Adblock Fast installs a button in your browser’s toolbar. The button indicates
the status of the site and page that you’re on:

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

## Questions, possibly to be asked frequently

### Who created Adblock Fast<em>?</em>

Adblock Fast was created and is maintained by
[Rocketship](http://rocketshipapps.com/), an award-winning app studio based in
Palo Alto and San Francisco. Our aim is to design and develop only the finest
mobile and web experiences, of our own and for clients.

### What are the install permissions Adblock Fast requires for<em>?</em>

Like all (functional) ad blockers, Adblock Fast prompts you for the permissions
required to block ad requests by intercepting your HTTP traffic and to hide ad
resources by injecting CSS into the webpages you go to.

### Where is Adblock Fast’s filtering ruleset from<em>?</em>

Adblock Fast’s ruleset is derived from
[EasyList](https://easylist.adblockplus.org/en/) and that of
[Bluhell Firewall](https://addons.mozilla.org/en-Us/firefox/addon/bluhell-firewall/).
We’re also testing a new, alternative ruleset that we expect to improve ad
blocking yet another order of magnitude.

### When will Adblock Fast be available on [insert platform here]<em>?</em>

Adblock Fast is available on the Chrome and Opera browsers and on iOS 9 (on
64-bit devices, iPhone 5s and up and iPad mini 2 and up). Follow us on
[Twitter](https://twitter.com/adblockfast) for news about additional platforms.

### Why is Adblock Fast called ”AB Fast” in the Chrome Web Store<em>?</em>

”AB Fast” is Adblock Fast<em>!</em> We hope to get the name updated in the Web
Store soon.

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
for a hand getting started.

### In Chrome

1. Fork this repository.
2. Switch to your working directory of choice.
3. Clone the repo locally:

        git clone https://github.com/rocketshipapps/adblockfast.git

4. Open or switch to Chrome.
5. Go to Chrome’s hamburger menu > **More Tools** > **Extensions**.
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

## License

Copyright 2015 Rocketship Apps, LLC

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

## Exceptions

The Adblock Fast and Rocketship logos, trademarks, domain names, and other brand
features used in this program cannot be reused without permission and no license
is granted thereto.

Further, the following third-party portions of the program and any use thereof
are subject to their own license terms:

* [Hudson NY](https://www.myfonts.com/fonts/virtuecreative/hudson-ny/)
* [Avenir Next](http://www.linotype.com/2090/avenirnext.html)
