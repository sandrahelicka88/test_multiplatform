package com.example.shared_code

import platform.UIKit.UIDevice


actual fun platformName(): String {
    return UIDevice.currentDevice.systemName() +
            " " +
            UIDevice.currentDevice.systemVersion
}