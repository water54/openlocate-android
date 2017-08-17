
![OpenLocate](http://imageshack.com/a/img922/4800/Pihgqn.png)


OpenLocate is an open source location tracking SDK built for android and iOS platforms.  

- [Features](#features)
- [Requirements](#requirements)
- [Communication](#communication)
- [Installation](#installation)
- [Usage](#usage)
- [FAQ](#faq)
- [Credits](#credits)
- [License](#license)

## Features

- [x] Optimized to reduce battery consumption
- [ ] Elegant request queueing
- [ ] Offline first - local caching, send to server as per defined network reporting frequency 
- [ ] Customizable local location recording frequency
- [ ] Customizable network reporting frequency
- [ ] Control required accuracy for recorded locations
- [ ] Define your own backend links where locations will be reported
- [ ] Offline mode - for network failures, etc
- [ ] Restart mechanisms post device reboots
- [ ] Nearby places
- [ ] Compress (Gzip) payload


## Requirements

- Android - Min SDK version 19

## Communication

- If you **need help**, use [Stack Overflow](https://stackoverflow.com). (Tag 'OpenLocate') 
- If you **found a bug**, open an issue.
- If you **have a feature request**, open an issue.
- If you **want to contribute**, submit a pull request.

## Installation

### Adding to your project

Add the below line to your app's `build.gradle` inside the `dependencies` section:
    
```groovy
compile 'com.openlocate:openlocate-android:0.1.0'
```

## Usage

### Start tracking of location

1. Start location tracking by providing your configuration as a object which extends  `Configuration` abstract class. The `Configuration` abstract should implement `getUrl()` as `String` and optionally `getHeaders()` as `HashMap<String, String>`. To send location to SafeGraph servers, use `SafeGraphConfiguration`, which also extends `Configuration`, class as mentioned below.

```java
UUID uuid = UUID.fromString("<YOUR_UUID>");
Configuration config = new SafeGraphConfiguration(uuid, "<YOUR_TOKEN>");
try {
  OpenLocate.getInstance(context).startTracking(config);
} catch (Exception e) {
  Log.e("OpenLocate", e.getMessage())
}
```


### Stop tracking of location

To stop the tracking call `stopTracking` method on the `OpenLocate`. Get the instance by calling `getInstance`.

```java
OpenLocate.getInstance(context).stopTracking()
```

### Fields collected by the SDK

Following fields are collected by the SDK for the ingestion API

1. `latitude` - Latitude of the device
2. `longitude` - Longitude of the device
3. `utc_timestamp` - Timestamp of the recorded location in epoch
4. `horizontal_accuracy` - The accuracy of the location being recorded
5. `id_type` - 'aaid' for identifying android advertising type
6. `ad_id` - Advertising identifier
7. `ad_opt_out` - Limited ad tracking enabled flag

## FAQ

@todo

## Credits

@todo

## License

MIT License

Copyright (c) 2017 OpenLocate

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.