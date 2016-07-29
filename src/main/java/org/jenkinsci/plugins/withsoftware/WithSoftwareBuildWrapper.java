package org.jenkinsci.plugins.withsoftware;

import hudson.Extension;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Node;
import hudson.model.labels.LabelAtom;
import hudson.tasks.BuildWrapper;
import hudson.tasks.BuildWrapperDescriptor;
import hudson.util.ComboBoxModel;
import hudson.util.ListBoxModel;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.withsoftware.util.Utils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Extension
public class WithSoftwareBuildWrapper extends BuildWrapper {

  private String xcodeVersion;
  private String unityVersion;

  public WithSoftwareBuildWrapper() {
    super();
  }

  @DataBoundConstructor
  public WithSoftwareBuildWrapper(String xcodeVersion, String unityVersion) {
    this.xcodeVersion = xcodeVersion;
    this.unityVersion = unityVersion;
  }

  @Override
  public Environment setUp(AbstractBuild build, final Launcher launcher, BuildListener listener) {
    Node node = build.getBuiltOn();
    SoftwareLabelSet labels = Utils.getSoftwareLabels(node);

    final SoftwareLabelAtom xcodeLabel = labels.getSoftwareLabel("Xcode", xcodeVersion);
    final SoftwareLabelAtom unityLabel = labels.getSoftwareLabel("Unity", unityVersion);

    if (xcodeVersion != null && xcodeLabel == null) {
    }

    if (unityVersion != null && unityLabel == null) {
    }

    return new Environment() {
      @Override
      public void buildEnvVars(Map<String, String> env) {
        if (xcodeLabel != null) {
          env.put("DEVELOPER_DIR", xcodeLabel.getHome());
        }

        if (unityLabel != null) {
          env.put("UNITY_HOME", unityLabel.getHome());
        }
      }
    };
  }

  public String getXcodeVersion() {
    return xcodeVersion;
  }

  public String getUnityVersion() {
    return unityVersion;
  }

  @Extension
  public static final class DescriptorImpl extends BuildWrapperDescriptor {

    public DescriptorImpl() {
      super(WithSoftwareBuildWrapper.class);
      load();
    }

    @Override
    public String getDisplayName() {
      return Messages.JOB_DESCRIPTION();
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
      save();
      return true;
    }

    @Override
    public BuildWrapper newInstance(StaplerRequest req, JSONObject json) throws FormException {
      String xcodeVersion = Util.fixEmptyAndTrim(json.getString("xcodeVersion"));
      String unityVersion = Util.fixEmptyAndTrim(json.getString("unityVersion"));

      return new WithSoftwareBuildWrapper(xcodeVersion, unityVersion);
    }

    @Override
    public boolean isApplicable(AbstractProject<?, ?> item) {
      return true;
    }

    public ComboBoxModel doFillXcodeVersionItems() {
      SoftwareLabelSet labels = Utils.getSoftwareLabels();
      return new ComboBoxModel(labels.getXcodeVersions());
    }

    public ComboBoxModel doFillUnityVersionItems() {
      SoftwareLabelSet labels = Utils.getSoftwareLabels();
      return new ComboBoxModel(labels.getUnityVersions());
    }
  }
}
