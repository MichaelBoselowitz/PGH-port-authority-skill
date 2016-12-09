package com.maya.portAuthority.util;

/**
 * Contains session scoped settings.
 */
public class SkillContext {
    private boolean needsMoreHelp = true;

    public boolean needsMoreHelp() {
        return needsMoreHelp;
    }

    public void setNeedsMoreHelp(boolean needsMoreHelp) {
        this.needsMoreHelp = needsMoreHelp;
    }
}
