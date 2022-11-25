package com.qianyi.livegoldenf.constants;

public enum WalletCodeEnum {

    SABASPORT("SABASPORT", "gf_sabasport_wallet"),
    ;
    /**
     * 产品代码
     */
    private String vendorCode;
    /**
     * 钱包代码
     */
    private String walletCode;

    WalletCodeEnum(String vendorCode, String walletCode) {
        this.vendorCode = vendorCode;
        this.walletCode = walletCode;
    }

    public String getVendorCode() {
        return vendorCode;
    }

    public String getWalletCode() {
        return walletCode;
    }

    public static String getWalletCodeByVendorCode(String vendorCode) {
        for (WalletCodeEnum walletCodeEnum : WalletCodeEnum.values()) {
            if (walletCodeEnum.getVendorCode().equals(vendorCode)) {
                return walletCodeEnum.getWalletCode();
            }
        }
        //默认为gf_main_balance 单一接口、共享钱包
        return null;
    }
}
