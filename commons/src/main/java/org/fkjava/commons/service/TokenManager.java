package org.fkjava.commons.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

import org.fkjava.commons.domain.AccessToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service // 鎶婂璞℃斁鍏pring瀹瑰櫒閲岄潰
public class TokenManager {

	private static final Logger LOG = LoggerFactory.getLogger(TokenManager.class);

	@Autowired
	private RedisTemplate<String, AccessToken> accessTokenTemplate;

	public String getToken() {
		// 1.鑾峰彇鏈湴浠ょ墝
		// 闇�瑕侀厤缃竴涓猂edisTemplate鐢ㄤ簬绠＄悊浠ょ墝
		BoundValueOperations<String, AccessToken> ops = accessTokenTemplate.boundValueOps("weixin_access_token");
		AccessToken at = ops.get();

		// 2.妫�鏌ユ湰鍦颁护鐗屾槸鍚﹀瓨鍦ㄣ�佹槸鍚︽湁鏁堬紙鍙互閫氳繃杩囨湡鏃堕棿鑷姩澶勭悊锛�
		if (at == null) {
			// 濡傛灉娌℃湁鑾峰緱鍒嗗竷寮忎簨鍔￠攣锛屽皾璇曞崄娆★紝姣忔闂撮殧1鍒嗛挓銆�
			for (int i = 0; i < 10; i++) {
				LOG.trace("缂撳瓨涓病鏈変护鐗岋紝灏濊瘯鍔犱笂鍒嗗竷寮忛攣");
				// 3.璋冪敤杩滅▼鎺ュ彛鑾峰彇浠ょ墝锛屽苟鍦ㄨ幏鍙栧埌浠ょ墝浠ュ悗锛屾妸浠ょ墝瀛樺偍鍦≧edis閲岄潰
				// 澧炲姞鍒嗗竷寮忛攣锛� 濡傛灉key涓嶅瓨鍦ㄥ垯璁剧疆杩涘幓锛涜�屽鏋渒ey瀛樺湪鍒欑瓑寰�60绉掓墠鑳借缃繘鍘�
				Boolean result = accessTokenTemplate.boundValueOps("weixin_access_token_lock")//
						.setIfAbsent(new AccessToken());
				LOG.trace("澧炲姞鍒嗗竷寮忛攣鐨勭粨鏋滐細{}", result);
				if (result == true) {
					try {
						// 鍒ゆ柇浠ょ墝鏄惁鍦≧edis閲岄潰
						at = ops.get();
						if (at == null) {
							LOG.trace("閲嶆柊鑾峰彇缂撳瓨鐨勪护鐗岋紝涔熸病鏈夊湪鏈湴鑾峰彇鍒帮紝灏濊瘯鑾峰彇杩滅▼浠ょ墝");
							at = getRemoteToken();
							// 鎶婂璞″瓨鍌ㄥ埌Redis閲岄潰
							ops.set(at);
							// 鍦ㄥ璞¤繃鏈熷悗锛孯edis浼氳嚜鍔ㄦ妸瀵硅薄浠庢暟鎹簱閲岄潰鍒犻櫎
							ops.expire(at.getExpiresIn() - 60, TimeUnit.SECONDS);
						} else {
							LOG.trace("鏈閲嶈瘯姝ｅ父鑾峰緱浠ょ墝: {}", at.getAccessToken());
						}
						break;
					} finally {
						LOG.trace("鍒犻櫎鍒嗗竷寮忛攣");
						accessTokenTemplate.delete("weixin_access_token_lock");
						synchronized (TokenManager.class) {
							TokenManager.class.notifyAll();
						}
					}
				} else {
					synchronized (TokenManager.class) {
						try {
							LOG.trace("鍏朵粬绾跨▼閿佸畾浜嗘暟鎹紝绛夊緟閫氱煡銆傚鏋滄病鏈夐�氱煡鍒�1鍒嗗悗閲嶈瘯");
							TokenManager.class.wait(1000 * 60);
						} catch (InterruptedException e) {
							LOG.error("鏃犳硶绛夊緟鍒嗗竷寮忎簨鍔￠攣鐨勯�氱煡锛�" + e.getLocalizedMessage(), e);
						}
					}
				}
			}
		}

		return at.getAccessToken();
	}

	// 鑾峰彇杩滅▼浠ょ墝
	public AccessToken getRemoteToken() {
		// 鍦ㄥ井淇＄殑鍏紬鍙锋病鏈夎璇侀�氳繃涔嬪墠锛屽厛浣跨敤寮�鍙戣�呭伐鍏烽噷闈㈢殑娴嬭瘯鍙锋潵杩涜娴嬭瘯
				String appId = "wxc066b5101f0a3c16";
				String appSecret = "121e6ddec9e0c4528037d681113ce85b";
				String url = "https://api.weixin.qq.com/cgi-bin/token"//
						+ "?grant_type=client_credential"//
						+ "&appid=" + appId//
						+ "&secret=" + appSecret;


		// 1.鍒涘缓HttpClient瀵硅薄
		// 鍦↗ava 11鎵嶅唴缃簡HttpClient锛屽鏋滄槸鏃╂湡JDK闇�瑕佷娇鐢ㄧ涓夋柟鐨刯ar鏂囦欢
		HttpClient client = HttpClient.newBuilder()//
				.version(Version.HTTP_1_1)// 璁剧疆HTTP 1.1鐨勫崗璁増鏈�
				.build();

		// 2.鍒涘缓HttpRequest瀵硅薄
		HttpRequest request = HttpRequest.newBuilder(URI.create(url))//
				.GET()// 浠ET鏂瑰紡鍙戦�佽姹�
				.build();

		// 3.璋冪敤杩滅▼鎺ュ彛锛岃繑鍥濲SON
		// BodyHandlers閲岄潰鍖呭惈浜嗚澶氬唴缃殑璇锋眰浣撱�佸搷搴斾綋鐨勫鐞嗙▼搴忥紝ofString鎰忔�濇槸浣跨敤String鏂瑰紡杩斿洖
		// Charset.forName("UTF-8")鎸囧畾瀛楃缂栫爜
		try {
			HttpResponse<String> response = client.send(request, BodyHandlers.ofString(Charset.forName("UTF-8")));

			// 4.鎶婅繑鍥炵殑JSON杞崲涓篔ava瀵硅薄
			String json = response.body();// 鍝嶅簲浣�
			LOG.trace("鑾峰彇浠ょ墝鐨勮繑鍥烇細\n{}", json);

			if (json.indexOf("errcode") > 0) {
				// 鍑虹幇浜嗛棶棰�
				throw new RuntimeException("鑾峰彇浠ょ墝鍑虹幇闂锛�" + json);
			}
			ObjectMapper mapper = new ObjectMapper();
			AccessToken at = mapper.readValue(json, AccessToken.class);

			// 杩斿洖浠ょ墝
//			return at.getAccessToken();
			return at;
		} catch (Exception e) {
			// 涓嶅鐞嗗紓甯革紝鐩存帴鍖呭紓甯稿皝瑁呬互鍚庡啀鎶涘嚭鍘�
			throw new RuntimeException("鑾峰彇浠ょ墝鍑虹幇闂锛�" + e.getLocalizedMessage(), e);
		}
	}
}
