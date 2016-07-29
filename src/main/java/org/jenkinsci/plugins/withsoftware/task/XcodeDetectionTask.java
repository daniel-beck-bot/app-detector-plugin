package org.jenkinsci.plugins.withsoftware.task;

import hudson.remoting.Callable;
import jenkins.security.MasterToSlaveCallable;
import org.jenkinsci.plugins.withsoftware.SoftwareLabelAtom;
import org.jenkinsci.plugins.withsoftware.util.Utils;

import java.util.HashSet;
import java.util.Set;

public final class XcodeDetectionTask extends MasterToSlaveCallable<Set<String>, Exception> {

  @Override
  public Set<String> call() throws Exception {
    Set<String> xcodeList = new HashSet<String>();

    String[] xcodePathList = Utils
        .runExternalCommand("/usr/bin/mdfind", "kMDItemCFBundleIdentifier == 'com.apple.dt.Xcode'")
        .split("\n");

    for (String xcodePath: xcodePathList) {
      String version = Utils
          .runExternalCommand("env", "DEVELOPER_DIR=" + xcodePath, "/usr/bin/xcodebuild", "-version")
          .split("\n")[0].split(" ")[1];

      xcodeList.add(SoftwareLabelAtom.serialize(new SoftwareLabelAtom("Xcode", version, xcodePath)));
    }

    return xcodeList;
  }
}
