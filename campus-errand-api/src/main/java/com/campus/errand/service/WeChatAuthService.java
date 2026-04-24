package com.campus.errand.service;

public interface WeChatAuthService {

    WeChatSession code2Session(String code);

    class WeChatSession {
        private final String openid;
        private final String unionid;

        public WeChatSession(String openid, String unionid) {
            this.openid = openid;
            this.unionid = unionid;
        }

        public String getOpenid() {
            return openid;
        }

        public String getUnionid() {
            return unionid;
        }
    }
}
