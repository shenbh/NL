package com.newland.wstdd.mine.managerpage.beanresponse;

import com.newland.wstdd.common.bean.TddActivity;
import com.newland.wstdd.mine.beanresponse.TddUserCertificate;

/**
 * 37.	报名是否需要审核
 * @author Administrator
 *
 */
public class RegistrationCheckRes {
	private TddActivity tddActivity;//activityId 和needPublicSigninfo不能为空，needPublicSigninfo：1 公开 2 关闭
//	private TddUserCertificate  tddUserCertificate;
	public TddActivity getTddActivity() {
		return tddActivity;
	}
	public void setTddActivity(TddActivity tddActivity) {
		this.tddActivity = tddActivity;
	}
//	public TddUserCertificate getTddUserCertificate() {
//		return tddUserCertificate;
//	}
//	public void setTddUserCertificate(TddUserCertificate tddUserCertificate) {
//		this.tddUserCertificate = tddUserCertificate;
//	}
}
