# Changelog:

### Versions:
* **1.1.1** - @OnBackground and @OnUIThread now can be used with static methods. Solved several method search problems, too.
* **1.1.0** - New @SaveInstance annotation to automatically save and restore variables on configuration changes.
* **1.0.4** - Fixed bug in @InjectViews annotation which didn't recognize "List" as a valid container.
* **1.0.3** - Fixed bug in ``@OnLongClick`` annotation.
* **1.0.2** - Fixed bug that prevented anything but an Activity from injection. Disabled type checking for auto-generated methods, so users won't have to type ``@CompileStatic(TypeCheckingMethod.SKIP)``.
* **1.0.1** - SwissKnife is now compatible with Android 4.0 - 4.1.
* **1.0.0** - Initial release.
