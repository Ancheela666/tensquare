package com.tensquare.encrypt.rsa;

/**
 * rsa加解密用的公钥和私钥
 * @author Administrator
 *
 */
public class RsaKeys {

	//生成秘钥对的方法可以参考这篇帖子
	//https://www.cnblogs.com/yucy/p/8962823.html

	//服务器公钥
	private static final String serverPubKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAvG/+Y7hPc0F4t4Vp9ZDL" +
			"lgJvpILybwu/Crsm/WH4RrRYNXEiLqvxHkPFXfgo89VO87s6v91n1VIJHksO/K9g" +
			"O1jmaLXllkBm1yHh6F2uW22AZB48cqBGCsxb6pSc8+w3xZUGMFx8wKk1zsk8tlvG" +
			"rb92Z3u7QB9KqFngtF621y9Y6O0x7fGsUuXTt1rQclwnF81wn3MPH3HswII/mDwC" +
			"BlV+zrrvMFBhlBfcFKrYUgaq4A5n2nj83vYL1rAvEvBupDdlVYl3miR9f+vn/9wF" +
			"S8Bv9Q+P3e2/97T08pnElgeFCaLu4sXWCPu/EiI3uEDrObkdfb35PU6UFSeRdbeo" +
			"FQIDAQAB";

	//服务器私钥(经过pkcs8格式处理)
	private static final String serverPrvKeyPkcs8 = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC8b/5juE9zQXi3" +
			"hWn1kMuWAm+kgvJvC78Kuyb9YfhGtFg1cSIuq/EeQ8Vd+Cjz1U7zuzq/3WfVUgke" +
			"Sw78r2A7WOZoteWWQGbXIeHoXa5bbYBkHjxyoEYKzFvqlJzz7DfFlQYwXHzAqTXO" +
			"yTy2W8atv3Zne7tAH0qoWeC0XrbXL1jo7THt8axS5dO3WtByXCcXzXCfcw8fcezA" +
			"gj+YPAIGVX7Ouu8wUGGUF9wUqthSBqrgDmfaePze9gvWsC8S8G6kN2VViXeaJH1/" +
			"6+f/3AVLwG/1D4/d7b/3tPTymcSWB4UJou7ixdYI+78SIje4QOs5uR19vfk9TpQV" +
			"J5F1t6gVAgMBAAECggEAbwU8x9vLU4h7awS7jOamS9dbl31fAHXXNKdXLWoPMPkI" +
			"D1R8YysCgGZFXjvbRO2j+o2Q64Gyr0gJ/0MRrWZWOCEEfyTMA14mORHJW3H2S/+T" +
			"6Wi3DIHJQotsjCTK5BuEMHDAmLWk+o23GgIONxKTV8K6K/YGOJ03C0EI1rwG1XgX" +
			"VTl4buIpxz+gSRG00VJ/DLvhJdEy8H7agwyTohei8lw2QODZtLhvcDfm+7UM1YQ5" +
			"nqKSTzfgbc0QjRh8x83Sm8svPR64O90uxtQQuqV0Pees9+rTyWUSC0LdzLNmuujp" +
			"L/qcctnnMtijEVAelva552Orqmxr2AbJt4MkRKp3cQKBgQD2QxTmTcnr1/SrmJBr" +
			"LjHJbxTTa9waUbHDHXAHsRjPLJIKs2ocaeU6vUBK/M+7xQ36pFPdFZcLjUCjf80A" +
			"ZH4OShrAk/6D8WDvOVnjIwDsy22YNzdfBIs3H3GnWpn2SG160W0Q+BkOEbEmTCpS" +
			"XC99eis59EEqLfQp+VdtaIOK7wKBgQDD441dXYTpw051Q1lM+uvNV0jYYiuSHFF5" +
			"Q8NveL0P786xrfO/I8Fyk6TkUPr1cVtZd3N2JqX0yD6HOJ71tsiNG4MfztfMayyq" +
			"HzILjerGy+7yy4D3uRqkUUuYztyXO1TmpmdFcSuIyi54/Tcluvmv9I7L+SMg+mJ9" +
			"2dn7Ni+NOwKBgQDQt1AqVXmy6uiS2w5j2NYXjI8RFZSJtf28ieGek0mcpYDtktx3" +
			"UoUXALlSFO0Xjs7nRIlTku3eu4wyRqoAECwKfmIoIAaR2221s1zstokdKNktLnc+" +
			"kAJR6NOR7Kca5o3rjz4qKKxLkVEcTWd9QGZ7qaSjTNJCJoqPXKXts6vxfwKBgGl7" +
			"8NT1Fiy3UlmNAdqVSENUHFMBUksk2q69UdGGJ/EcBqbhcF9eRr6HSQT1op5nA3UD" +
			"APb/yuAssJcqY1cXrZlm1k5bmRNoJy4ZDF7ydhnFGZA/C9zaHcGcUWQhCOKbPZXS" +
			"x8u6LGJF1Y6rdHXp4Kjivb6TE1qf2kfA90PyotqfAoGAJFv52KE3liUT0lST7V5E" +
			"0vlBnbi9JLUnQHGWP6hEf+K1dEkST4m33Tbv+6DUqP/gC5wVNPoZLQMfP5I+NaL1" +
			"WgDqZGm5bxfOhGOwedeVJl1CYONhlKO0eF3L9Y+78bY8A2Vsb25fG/oL1ZiJaTlO" +
			"DEfxOSIngbcXxcbtqW/nlDc=";

	public static String getServerPubKey() {
		return serverPubKey;
	}

	public static String getServerPrvKeyPkcs8() {
		return serverPrvKeyPkcs8;
	}
	
}
