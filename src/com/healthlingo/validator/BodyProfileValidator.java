package com.healthlingo.validator;

import com.healthlingo.model.BodyRecord;
import com.healthlingo.model.UserProfile;

public class BodyProfileValidator {
    private int fails = 0;
    public int getFailCount() { return fails; }

    public boolean isValidForBmi(UserProfile p, double weight) {
        boolean ok = p != null && p.isHeightRegistered() && weight > 0;
        if (!ok) fails++;
        return ok;
    }
    public boolean isValidForStats(UserProfile p, BodyRecord latest) {
        boolean ok = p != null && p.isHeightRegistered() && latest != null && latest.getWeight() > 0;
        if (!ok) fails++;
        return ok;
    }
}
