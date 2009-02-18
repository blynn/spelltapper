.PHONY: binclasses target red ree

SDKDIR=~/android-sdk-linux_x86-1.1_r1
ADB=$(SDKDIR)/tools/adb
AAPT=$(SDKDIR)/tools/aapt
DX=$(SDKDIR)/tools/dx
APKBUILDER=$(SDKDIR)/tools/apkbuilder
MUCK=com/gmail/benlynn/spelltap/

target: bin/out.apk

src/$(MUCK)/R.java : res/*/*
	$(AAPT) p -m -J src -M AndroidManifest.xml -S res -I $(SDKDIR)/android.jar

binclasses : src/$(MUCK)/*.java src/$(MUCK)/R.java
	-mkdir bin/classes
	javac -encoding ascii -target 1.5 -d bin/classes -bootclasspath $(SDKDIR)/android.jar src/$(MUCK)/*.java
	-rm bin/classes/$(MUCK)/R*.class

bin/classes.dex : binclasses
	$(DX) --dex --output=bin/classes.dex bin/classes

bin/resources.ap_ : src/$(MUCK)/R.java
	$(AAPT) p -f -M AndroidManifest.xml -S res -I $(SDKDIR)/android.jar -F bin/resources.ap_

bin/out.apk : bin/resources.ap_ bin/classes.dex
	$(APKBUILDER) bin/out.apk -z bin/resources.ap_ -f bin/classes.dex -rf src -rj libs

red :
	$(ADB) -d uninstall com.gmail.benlynn.spelltap
	$(ADB) -d install bin/out.apk

ree :
	$(ADB) -e uninstall com.gmail.benlynn.spelltap
	$(ADB) -e install bin/out.apk
