测试命令：
adb root
adb shell am broadcast -a android.intent.action.BOOT_COMPLETED -c android.intent.category.HOME -n com.yjzn.lightdimcontroller/.BootBroadcastReceiver