// f=검증 실패 횟수(fails), p=사용자 프로필(UserProfile), w=체중(weight), l=최근 신체기록(latest), b=검증 결과
package com.validator;

import com.model.BodyModels.BodyRecord;
import com.model.BodyModels.UserProfile;

public class BodyProfileValidator {

    private int f = 0;

    public int getFailCount() { return f; }

    public boolean isValidForBmi(UserProfile p, double w) {
        boolean b = p != null && p.isHeightRegistered() && w > 0;
        if (!b) f++;
        return b;
    }

    public boolean isValidForStats(UserProfile p, BodyRecord l) {
        boolean b = p != null && p.isHeightRegistered() && l != null && l.getWeight() > 0;
        if (!b) f++;
        return b;
    }
}
