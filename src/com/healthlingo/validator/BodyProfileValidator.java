package com.healthlingo.validator;

import com.healthlingo.model.UserProfile;

/**
 * MOD-008 BodyProfileValidator (EH-03 / EH-05)
 * 신체 정보(신장·체중) 존재 여부를 검증하여 BMI 계산 및 통계 판정의 사전 조건을 확인한다.
 * invalid 판정 시 호출한 Manager는 계산/판정 로직을 실행하지 않고 안내만 출력해야 한다.
 */
public class BodyProfileValidator {

    private int fails = 0;

    public int getFailCount() { return fails; }

    /** EH-05: BMI 계산 직전 신장 존재 여부 확인 */
    public boolean isValidForBmi(UserProfile p) {
        boolean ok = p != null && p.isHeightRegistered();
        if (!ok) fails++;
        return ok;
    }

    /** EH-03: 신체 관련 통계(체중·BMI 추이) 조회 직전 신장 존재 여부 확인 */
    public boolean isValidForStats(UserProfile p) {
        boolean ok = p != null && p.isHeightRegistered();
        if (!ok) fails++;
        return ok;
    }
}
