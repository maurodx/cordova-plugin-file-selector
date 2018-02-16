# Cordova FileSelector Plugin

Source forked from https://github.com/don/cordova-filechooser

- Requires Cordova >= 2.8.0
- Support for Cordova >= 7.0.0

## Installing the plugin

### With Cordova CLI

        $ cordova plugin add http://github.com/cinthiaro/cordova-plugin-fileselector.git

### With Plugman

        $ plugman --platform android --project /path/to/project \
                --plugin http://github.com/cinthiaro/cordova-filechooser.git

## Using the plugin

        fileChooser.open(successCallback, failureCallback);

The success callback returns the object file with the following properties: name, uri, mime_type and extension.

```
        fileChooser.open(function(file) {
                alert("Name: " + file.name +
                      " URI: " + file.uri +
                      " Mime type: " + file.mime_type +
                      " Extension: " + file.extension);
        });

```

Supported Platforms:
- Android
	
Screenshot

![Screenshot](filechooser.png "Screenshot")

TODO rename `open` to pick, select, or choose.
