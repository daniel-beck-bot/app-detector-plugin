package org.jenkinsci.plugins.appdetector;

import hudson.Extension;
import hudson.matrix.Combination;
import hudson.matrix.MatrixConfiguration;
import hudson.model.Action;
import hudson.model.Label;
import hudson.model.ParameterValue;
import hudson.model.ParametersAction;
import hudson.model.Project;
import hudson.model.Queue;
import hudson.model.labels.LabelAssignmentAction;
import hudson.model.labels.LabelAtom;
import hudson.model.queue.SubTask;
import hudson.tasks.BuildWrapper;
import org.jenkinsci.plugins.appdetector.util.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Extension(ordinal = -100)
public class AppDetectorHandler extends Queue.QueueDecisionHandler {
  @Override
  public boolean shouldSchedule(Queue.Task task, List<Action> actions) {
    if (task instanceof Project) {
      List<BuildWrapper> buildWapperList = ((Project)task).getBuildWrappersList();

      for (BuildWrapper bw: buildWapperList) {
        if (bw instanceof AppDetectorBuildWrapper) {
          String xcodeVersion = ((AppDetectorBuildWrapper)bw).getXcodeVersion();
          String unityVersion = ((AppDetectorBuildWrapper)bw).getUnityVersion();

          final Map<String, String> buildVars = new TreeMap<String, String>();

          if (task instanceof MatrixConfiguration) {
            Combination combination = ((MatrixConfiguration)task).getCombination();
            buildVars.putAll(combination);
          }

          buildVars.putAll(getBuildVariablesFromActions(actions));

          xcodeVersion = Utils.expandVariables(buildVars, xcodeVersion);
          unityVersion = Utils.expandVariables(buildVars, unityVersion);

          if (xcodeVersion != null) {
            actions.add(new ApplicationLabelAssignmentAction("Xcode-" + xcodeVersion));
          }
          if (unityVersion != null) {
            actions.add(new ApplicationLabelAssignmentAction("Unity-" + unityVersion));
          }
        }
      }
    }
    return true;
  }

  private Map<String, String> getBuildVariablesFromActions(List<Action> actions) {
    Map<String, String> buildVars = new HashMap<String, String>();
    for (Action action: actions) {
      if (action instanceof ParametersAction) {
        for (ParameterValue param: ((ParametersAction)action).getParameters()) {
          buildVars.put(param.getName(), param.getValue().toString());
        }
      }
    }
    return buildVars;
  }

  private static class ApplicationLabelAssignmentAction implements LabelAssignmentAction {
    private Label label;

    public ApplicationLabelAssignmentAction(String label) {
      this.label = new LabelAtom(label);
    }

    public Label getAssignedLabel(SubTask task) {
      Label taskLabel = task.getAssignedLabel();

      if (taskLabel != null) {
        return label.and(taskLabel);
      }

      return label;
    }

    public String getIconFileName() {
      return null;
    }

    public String getDisplayName() {
      return null;
    }

    public String getUrlName() {
      return null;
    }
  }
}
