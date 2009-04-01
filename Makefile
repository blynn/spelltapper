.PHONY: target release red ree

SDKDIR=~/android-sdk-linux_x86-1.1_r1
ADB=$(SDKDIR)/tools/adb
AAPT=$(SDKDIR)/tools/aapt
DX=$(SDKDIR)/tools/dx
APKBUILDER=$(SDKDIR)/tools/apkbuilder
MUCK=com/benlynn/spelltapper/

target: bin/out.apk

release: bin/spelltapper.apk

src/$(MUCK)/R.java : res/*/* AndroidManifest.xml
	$(AAPT) p -m -J src -M AndroidManifest.xml -S res -I $(SDKDIR)/android.jar

bin/classes/$(MUCK)/SpellTap.java : src/$(MUCK)/*.java src/$(MUCK)/R.java
	install -d bin/classes
	javac -encoding ascii -target 1.5 -d bin/classes -bootclasspath $(SDKDIR)/android.jar src/$(MUCK)/*.java
	-rm bin/classes/$(MUCK)/R*.class

bin/classes.dex : bin/classes/$(MUCK)/SpellTap.java
	$(DX) --dex --output=bin/classes.dex bin/classes

bin/resources.ap_ : src/$(MUCK)/R.java AndroidManifest.xml
	$(AAPT) p -f -M AndroidManifest.xml -S res -I $(SDKDIR)/android.jar -F bin/resources.ap_

bin/out.apk : bin/resources.ap_ bin/classes.dex
	$(APKBUILDER) bin/out.apk -z bin/resources.ap_ -f bin/classes.dex -rf src

bin/spelltapper.apk : bin/resources.ap_ bin/classes.dex
	$(APKBUILDER) $@ -u -z bin/resources.ap_ -f bin/classes.dex -rf src
	jarsigner -keystore ~/android.keystore $@ android
	scp bin/spelltapper.apk tl1.stanford.edu:www/spelltapper/

red : bin/out.apk
	$(ADB) -d uninstall com.benlynn.spelltapper
	$(ADB) -d install bin/out.apk

ree : bin/out.apk
	$(ADB) -e uninstall com.benlynn.spelltapper
	$(ADB) -e install bin/out.apk
